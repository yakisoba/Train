package com.blogspot.yakisobayuki.dtn;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Databaseにアクセスするためのクラス
 *
 */
public class DatabaseAccess {
	private Context context;
	private SharedPreferences pref;

	private EventsData mEvents = null;
	private SQLiteDatabase mDB = null;
	/** DB名とバージョン */
	private static final String DATABASE_NAME = "schedule.db";
	private static final int DATABASE_VERSION = 3;

	/** スケジュール用のテーブル */
	private static final String NEXT_SCHEDULE_TABLE = "Next_Schedule";
	private static final String LAST_TRAIN_TABLE = "Last_Train";
	private static final String NEXT_ARR_DEP_TABLE = "Next_Arrival_Departure";
	private static final String NEXT_ROUTE_TABLE = "Next_Schedule_Route";
	private static final String LAST_ARR_DEP_TABLE = "Last_Arrival_Departure";
	private static final String LAST_ROUTE_TABLE = "LastTrain_Route";
	
	private ScheduleData scheduledata;
	private List<CalenderData> mCalenderData;
	private String mCurrentAddress;
	private List<Route>[][] mNextSchedule;
	private List<Route>[] mLastTrain;
	private String[][] NextDeparture;
	private String[] LastDeparture;

	final String TAGtest = "TrainTransferTest";

	/** コンストラクタ **/
	public DatabaseAccess(Context context) {
		this.context = context;
		mEvents = new EventsData(context);
		mDB = mEvents.getWritableDatabase();
	}

	private class EventsData extends SQLiteOpenHelper {
		public EventsData(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			// 次スケジュールのテーブル
			db.execSQL("CREATE TABLE " + NEXT_SCHEDULE_TABLE + " (" // テーブル作成
					+ "eventID" + " INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
					+ "eventName" + " TEXT, " // イベント名
					+ "Locate" + " TEXT, " // 場所
					+ "StartTime" + " TEXT);" // 開始時間
			);

			// 終電のテーブル
			db.execSQL("CREATE TABLE " + LAST_TRAIN_TABLE + " (" // テーブル作成
					+ "eventID" + " INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
					+ "HomeStation" + " TEXT);" // 場所
			);

			// 発着場所/時間
			db.execSQL("CREATE TABLE " + NEXT_ARR_DEP_TABLE + " (" // テーブル作成
					+ "eventID" + " INTEGER, " //
					+ "routeID" + " INTEGER PRIMARY KEY AUTOINCREMENT," // ID
					+ "SLocate" + " TEXT," // 出発場所
					+ "ELocate" + " TEXT," // 到着場所
					+ "STime" + " TEXT," // 出発時間
					+ "ETime" + " TEXT);" // 到着時間
			);

			// ルート
			db.execSQL("CREATE TABLE " + NEXT_ROUTE_TABLE + " (" // テーブル作成
					+ "routeID" + " INTEGER," // ID
					+ "TransID" + " INTEGER," // ID
					+ "Transfer" + " TEXT," // 移動方法(徒歩、JR～～線など)
					+ "SLocate" + " TEXT," // 出発場所
					+ "ELocate" + " TEXT," // 到着場所
					+ "STime" + " TEXT," // 出発時間
					+ "ETime" + " TEXT);" // 到着時間
			);

			// 発着場所/時間
			db.execSQL("CREATE TABLE " + LAST_ARR_DEP_TABLE + " (" // テーブル作成
					+ "eventID" + " INTEGER, " //
					+ "routeID" + " INTEGER PRIMARY KEY AUTOINCREMENT," // ID
					+ "SLocate" + " TEXT," // 出発場所
					+ "ELocate" + " TEXT," // 到着場所
					+ "STime" + " TEXT," // 出発時間
					+ "ETime" + " TEXT);" // 到着時間
			);

			// ルート
			db.execSQL("CREATE TABLE " + LAST_ROUTE_TABLE + " (" // テーブル作成
					+ "routeID" + " INTEGER," // ID
					+ "TransID" + " INTEGER," // ID
					+ "Transfer" + " TEXT," // 移動方法(徒歩、JR～～線など)
					+ "SLocate" + " TEXT," // 出発場所
					+ "ELocate" + " TEXT," // 到着場所
					+ "STime" + " TEXT," // 出発時間
					+ "ETime" + " TEXT);" // 到着時間
			);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// データベースのバージョンが変化したときに実行する。
			// とりあえず作ったテーブルを一旦削除して作り直す。
			db.execSQL("DROP TABLE IF EXISTS " + NEXT_SCHEDULE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + LAST_TRAIN_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + NEXT_ARR_DEP_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + NEXT_ROUTE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + LAST_ARR_DEP_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + LAST_ROUTE_TABLE);
			onCreate(db);
		}
	}

	private boolean DB2NextSchedule(ScheduleData sdata) {
		try {
			// カレンダーのデータからNext_Scheduleテーブルを一旦クリアしたのち格納
			for (CalenderData cdata : sdata.getCalendar()) {

				// 次のスケジュールのテーブルを格納
				ContentValues values1 = new ContentValues();
				values1.put("eventName", cdata.getEvent());
				values1.put("Locate", cdata.getLocation());
				values1.put("StartTime", cdata.getStartTime());
				mDB.insert(NEXT_SCHEDULE_TABLE, null, values1);

				for (int i = 0; i < sdata.getNextDeparture()[sdata.getCalendar()
						.indexOf(cdata)].length; i++) {
					// 全体の出発場所/時間と到着場所/時間を格納
					ContentValues values2 = new ContentValues();
					values2.put("eventID", sdata.getCalendar().indexOf(cdata) + 1);
					values2.put("SLocate", sdata.getCurrentAddress());
					values2.put("ELocate", cdata.getLocation());
					values2.put("STime", sdata.getNextDeparture()[sdata
							.getCalendar().indexOf(cdata)][i]);
					values2.put("ETime", sdata.getNextDeparture()[sdata
							.getCalendar().indexOf(cdata)][i]); // 未定
					mDB.insert(NEXT_ARR_DEP_TABLE, null, values2);

					for (Route route : sdata.getNextRouteData()[sdata
							.getCalendar().indexOf(cdata)][i]) {
						// 乗り換えの手段、出発場所/時間、到着場所/時間を格納
						ContentValues values3 = new ContentValues();
						values3.put("routeID",
								(sdata.getCalendar().indexOf(cdata) * 3) + i + 1);
						values3.put("TransID", sdata.getNextRouteData()[sdata
								.getCalendar().indexOf(cdata)][i]
								.indexOf(route));
						values3.put("Transfer", route.getTrans());
						values3.put("SLocate", route.getDLocate());
						values3.put("ELocate", route.getALocate());
						values3.put("STime", route.getDTime());
						values3.put("ETime", route.getATime());
						mDB.insert(NEXT_ROUTE_TABLE, null, values3);

					}
				}
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private boolean DB2LastTrain(ScheduleData sdata) {
		try {
			// カレンダーのデータからNext_Scheduleテーブルを一旦クリアしたのち格納
			// 次のスケジュールのテーブルを格納
			ContentValues values1 = new ContentValues();
			values1.put("HomeStation", homestation());
			mDB.insert(LAST_TRAIN_TABLE, null, values1);

			for (int i = 0; i < sdata.getLastDeparture().length; i++) {
				if (sdata.getLastDeparture()[i] == null) {
					break;
				}

				// 全体の出発場所/時間と到着場所/時間を格納
				ContentValues values2 = new ContentValues();
				values2.put("eventID", 1);
				values2.put("SLocate", sdata.getCurrentAddress());
				values2.put("ELocate", homestation());
				values2.put("STime", sdata.getLastDeparture()[i]);
				values2.put("ETime", sdata.getLastDeparture()[i]); // 未定
				mDB.insert(LAST_ARR_DEP_TABLE, null, values2);

				for (Route route : sdata.getLastRouteData()[i]) {
					// 乗り換えの手段、出発場所/時間、到着場所/時間を格納
					ContentValues values3 = new ContentValues();
					values3.put("routeID", i +1);
					values3.put("TransID",	sdata.getLastRouteData()[i].indexOf(route));
					values3.put("Transfer", route.getTrans());
					values3.put("SLocate", route.getDLocate());
					values3.put("ELocate", route.getALocate());
					values3.put("STime", route.getDTime());
					values3.put("ETime", route.getATime());
					mDB.insert(LAST_ROUTE_TABLE, null, values3);
				}
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * スケジュールデータ登録処理
	 *
	 * @param data
	 *            スケジュールデータ(次の予定/終電の出着駅・出着時間など)
	 * @return 登録成功(true)/登録失敗(false)
	 */
	public boolean setSchedule(ScheduleData sdata) {
		boolean check = false;

		// 書き込む前にDBの中身を消して、次のスケジュールデータをDBに登録
		mDB.delete(NEXT_SCHEDULE_TABLE, null, null);
		mDB.execSQL("update sqlite_sequence set seq=0 where name='"
				+ NEXT_SCHEDULE_TABLE + "';");

		mDB.delete(NEXT_ARR_DEP_TABLE, null, null);
		mDB.execSQL("update sqlite_sequence set seq=0 where name='"
				+ NEXT_ARR_DEP_TABLE + "';");

		mDB.delete(NEXT_ROUTE_TABLE, null, null);
		check = DB2NextSchedule(sdata);

		// 書き込む前にDBの中身を消して、終電のデータをDBに登録
		mDB.delete(LAST_TRAIN_TABLE, null, null);
		mDB.execSQL("update sqlite_sequence set seq=0 where name='"
				+ LAST_TRAIN_TABLE + "';");

		mDB.delete(LAST_ARR_DEP_TABLE, null, null);
		mDB.execSQL("update sqlite_sequence set seq=0 where name='"
				+ LAST_ARR_DEP_TABLE + "';");

		mDB.delete(LAST_ROUTE_TABLE, null, null);
		check = DB2LastTrain(sdata);

		mDB.close();

		return check;
	}

	/**
	 * スケジュールデータ取得処理
	 *
	 * @return スケジュールデータ(次の予定/終電の出着駅・出着時間など) 取得失敗時にはnullを返す
	 */
	@SuppressWarnings("unchecked")
	public void getSchedule() {
		scheduledata = new ScheduleData();
		mCalenderData = scheduledata.getCalendar();
		mCurrentAddress = scheduledata.getCurrentAddress();
		mNextSchedule = scheduledata.getNextRouteData();
		mLastTrain = scheduledata.getLastRouteData();
		NextDeparture = scheduledata.getNextDeparture();
		LastDeparture = scheduledata.getLastDeparture();

		mNextSchedule = new ArrayList[2][3];
		mLastTrain = new ArrayList[3];
		
		String sql;
		SQLiteCursor cursor;

		// 次のスケジュール情報取得
		try {
			sql = "select * from " + NEXT_SCHEDULE_TABLE;
			cursor = (SQLiteCursor) mDB.rawQuery(sql, null);

			if(cursor != null){
				while (cursor.moveToNext()) {
					CalenderData cd = new CalenderData();
					int EventId = cursor.getInt(cursor.getColumnIndex("eventID"));
					cd.setSchedule(cursor.getString(cursor.getColumnIndex("eventName")), 
										cursor.getString(cursor.getColumnIndex("Locate")),
										cursor.getString(cursor.getColumnIndex("StartTime")));
					mCalenderData.add(cd);

					String sql2 = "select * from " + NEXT_ARR_DEP_TABLE + " where eventID = " + EventId + ";";
					SQLiteCursor cursor2 = (SQLiteCursor) mDB.rawQuery(sql2,null);
					
					if(cursor2 != null){
						while (cursor2.moveToNext()) {
							int RouteId = cursor2.getInt(cursor2.getColumnIndex("routeID"));
							mCurrentAddress = cursor2.getString(cursor2.getColumnIndex("SLocate"));
							NextDeparture[0][cursor2.getPosition()] = cursor2.getString(cursor2.getColumnIndex("STime"));
							
							String sql3 = "select * from " + NEXT_ROUTE_TABLE	+ " where routeID = " + RouteId + ";";
							SQLiteCursor cursor3 = (SQLiteCursor) mDB.rawQuery(sql3, null);
							
							if(cursor3 != null){
								mNextSchedule[0][cursor2.getPosition()] = new ArrayList<Route>();
								while (cursor3.moveToNext()) {
									Route r = new Route();
									r.setRoute(cursor3.getString(cursor3.getColumnIndex("Transfer")),
											cursor3.getString(cursor3.getColumnIndex("STime")),
											cursor3.getString(cursor3.getColumnIndex("SLocate")),
											cursor3.getString(cursor3.getColumnIndex("ETime")),
											cursor3.getString(cursor3.getColumnIndex("ELocate")));
									mNextSchedule[0][cursor2.getPosition()].add(r);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			mDB.close();
		}
		
		/* カレンダーデータ設定 */
		scheduledata.setCalendar(mCalenderData);
		/* 現在値住所設定 */
		scheduledata.setCurrentAddress(mCurrentAddress);
		/* 次のスケジュールのルート情報設定 */
		scheduledata.setNextRouteData(mNextSchedule, NextDeparture);

		/* 終電ルート情報取得 */
		try {
			sql = "select * from " + LAST_TRAIN_TABLE;
			cursor = (SQLiteCursor) mDB.rawQuery(sql, null);

			if(cursor != null){
				while (cursor.moveToNext()) {
					int EventId = cursor.getInt(cursor.getColumnIndex("eventID"));
					
					String sql2 = "select * from " + LAST_ARR_DEP_TABLE + " where eventID = " + EventId + ";";
					SQLiteCursor cursor2 = (SQLiteCursor) mDB.rawQuery(sql2, null);
					
					if(cursor2 != null){
						while (cursor2.moveToNext()) {
							int RouteId = cursor2.getInt(cursor2.getColumnIndex("routeID"));
							LastDeparture[cursor2.getPosition()] = cursor2.getString(cursor2.getColumnIndex("STime"));
							
							String sql3 = "select * from " + LAST_ROUTE_TABLE + " where routeID = " + RouteId + ";";
							SQLiteCursor cursor3 = (SQLiteCursor) mDB.rawQuery(sql3, null);
							
							if(cursor3 != null){
								mLastTrain[cursor2.getPosition()] = new ArrayList<Route>();
								while (cursor3.moveToNext()) {
									Route r = new Route();
									r.setRoute(cursor3.getString(cursor3.getColumnIndex("Transfer")),
											cursor3.getString(cursor3.getColumnIndex("STime")),
											cursor3.getString(cursor3.getColumnIndex("SLocate")),
											cursor3.getString(cursor3.getColumnIndex("ETime")),
											cursor3.getString(cursor3.getColumnIndex("ELocate")));
									mLastTrain[cursor2.getPosition()].add(r);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			mDB.close();
		}

		/* 終電ルート情報設定 */
		scheduledata.setLastRouteData(mLastTrain, LastDeparture);
	}

	private String homestation() {
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		String station = pref.getString("set_home_station", null);
		return station;
	}
	
	public void close(){
		mDB.close();
	}
	
	public String toStringAddress(){
		try {
			return scheduledata.getCurrentAddress();
		} catch (NullPointerException e) {
			Log.d(TAGtest,"DBA:" + e.toString());
			return null;
		} catch (Exception e) {
			Log.d(TAGtest,"DBA:" + e.toString());
			return null;
		}
	}
	
	public CalenderData toCalendar(int index){
		try {
			return scheduledata.getCalendar().get(index);
		} catch (NullPointerException e) {
			Log.d(TAGtest,"cal:"+ e.toString());
			return null;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAGtest,"cal:"+ e.toString());
			return null;
		} catch (Exception e) {
			Log.d(TAGtest,"cal:"+ e.toString());
			return null;
		}
	}
	
	public List<Route> toNextRoute(int cindex,int rindex){
		try {
			return scheduledata.getNextRouteData()[cindex][rindex];
		} catch (NullPointerException e) {
			Log.d(TAGtest,"nr:"+ e.toString());
			return null;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAGtest,"nr:"+ e.toString());
			return null;
		} catch (Exception e) {
			Log.d(TAGtest,"nr:"+ e.toString());
			return null;
		}
	}
	
	public List<Route> toLastRoute(int rindex){
		try {
			return scheduledata.getLastRouteData()[rindex];
		} catch (NullPointerException e) {
			Log.d(TAGtest,"lr:"+ e.toString());
			return null;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAGtest,"lr:"+ e.toString());
			return null;
		} catch (Exception e) {
			Log.d(TAGtest,"lr:"+ e.toString());
			return null;
		}
	}

	public String[][] toStringNextTimeLocation(int cindex, int rindex) {
		try {
			String[][] time_location = new String[2][2];
			List<Route> list = new ArrayList<Route>();
			list = toNextRoute(cindex,rindex);
			
			for(Route route:list){
				// 電車の出発時間が取得できて、まだ出発時間とかが未取得
				if(route.getDTime() != null && time_location[0][0] == null){
					time_location[0][0] = route.getDTime() + "発";
					time_location[0][1] = route.getDLocate();
				}
			
				if(route.getDTime() != null){
					time_location[1][0] = route.getATime() + "着";
					time_location[1][1] = route.getALocate();
				}
			}

			return time_location;
			
		} catch (NullPointerException e) {
			Log.d(TAGtest,"ntl:"+ e.toString());
			return null;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAGtest,"ntl:"+ e.toString());
			return null;
		} catch (Exception e) {
			Log.d(TAGtest,"ntl:"+ e.toString());
			return null;
		}
	}

	public String[][] toStringLastTimeLocation(int rindex) {
		try {
			String[][] time_location = new String[2][2];
			List<Route> list = new ArrayList<Route>();
			list = toLastRoute(rindex);
			
			for(Route route:list){
				// 電車の出発時間が取得できて、まだ出発時間とかが未取得
				if(route.getDTime() != null && time_location[0][0] == null){
					time_location[0][0] = route.getDTime() + "発";
					time_location[0][1] = route.getDLocate();
				}
			
				if(route.getDTime() != null){
					time_location[1][0] = route.getATime() + "着";
					time_location[1][1] = route.getALocate();
				}
			}

			return time_location;
			
		} catch (NullPointerException e) {
			Log.d(TAGtest,"ltl:"+ e.toString());
			return null;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAGtest,"ltl:"+ e.toString());
			return null;
		} catch (Exception e) {
			Log.d(TAGtest,"ltl:"+ e.toString());
			return null;
		}
	}
	
	public Date toNextData(int rindex) {
		try {
			String time = scheduledata.getNextDeparture()[0][rindex];
			Date date = new Date();
			Date date_tmp = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(time.substring(0,2)));
			calendar.set(Calendar.MINUTE,Integer.parseInt(time.substring(3,5)));
			date = calendar.getTime();
			
			// 今日より前だったら
			if(date.getTime( ) < date_tmp.getTime( )){
				calendar.setTime(date);
				calendar.add(Calendar.DATE, 1);
				date = calendar.getTime();
			}
			return date;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public Date toLastData(int rindex) {
		try {
			String time = scheduledata.getLastDeparture()[rindex];
			Date date = new Date();
			Date date_tmp = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(time.substring(0,time.indexOf(":"))));
			calendar.set(Calendar.MINUTE,Integer.parseInt(time.substring(time.indexOf(":")+1,time.length())));
			date = calendar.getTime();
			
			// 今日より前だったら
			if(date.getTime( ) < date_tmp.getTime( )){
				calendar.setTime(date);
				calendar.add(Calendar.DATE, 1);
				date = calendar.getTime();
			}
			return date;
			
		} catch (Exception e) {
			return null;
		}
	}
}
