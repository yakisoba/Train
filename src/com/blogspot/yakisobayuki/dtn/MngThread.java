package com.blogspot.yakisobayuki.dtn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MngThread extends BroadcastReceiver {
	private String mCurrentAddress = null;
	private String mHomeStation = null;
	private List<CalenderData> mCalendarData = null;
	private List<Route>[][] mNextRouteData = null;
	private List<Route>[] mLastRouteData = null;
	private String[][] next_departure = null;
	private String[] last_departure = null;
	private ScheduleData mScheduleData = null;

	private SharedPreferences pref;

	final int TYPE_NEXT_SCHEDULE = 0;
	final int TYPE_LAST_TRAIN = 1;
	final String TAG = "TrainTransfer";
	final String TAGtest = "TrainTransferTest";
	final boolean TOAST_FLG = true;
	public boolean toast_err = false;

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent) {
		if (TOAST_FLG) {
			Toast.makeText(context, R.string.refresh_start, Toast.LENGTH_LONG)
					.show();
		}

		// DBから一旦読込んでローカルに情報を保存
		mScheduleData = new ScheduleData();

		Long Nexttime = 0L;
		Long Lasttime = 0L;

		// 現在位置要求
		GPSAccess gps = new GPSAccess();
		mCurrentAddress = gps.getCurrentAddress(context);
		if (mCurrentAddress != null) {
			// スケジュールデータを更新
			mScheduleData.setCurrentAddress(mCurrentAddress);
		} else {
			if (TOAST_FLG) {
				Toast.makeText(context, R.string.address_error,
						Toast.LENGTH_LONG).show();
				toast_err = false;
			}
		}

		// GoogleCalendarからスケジュール要求
		CalendarAccess calendar = new CalendarAccess();
		mCalendarData = calendar.getCalenderSchedule(context);
		if (!mCalendarData.isEmpty()) {
			// スケジュールデータを更新
			mScheduleData.setCalendar(mCalendarData);
		} else {
			if (TOAST_FLG) {
				Toast.makeText(context, R.string.event_error, Toast.LENGTH_LONG)
						.show();
				toast_err = false;
			}
		}

		// 現在地とスケジュールデータから経路を選択
		mNextRouteData = new ArrayList[2][3];
		mLastRouteData = new ArrayList[3];
		next_departure = new String[2][3];
		last_departure = new String[3];

		RouteSearch nextroutesearch = new RouteSearch(mCurrentAddress);
		for (CalenderData cdata : mCalendarData) {

			if (mCurrentAddress != null) {
				mNextRouteData[mCalendarData.indexOf(cdata)] = nextroutesearch
						.getNextRoute(cdata);
				next_departure[mCalendarData.indexOf(cdata)] = nextroutesearch
						.getDeparture(TYPE_NEXT_SCHEDULE);

				if (mNextRouteData[mCalendarData.indexOf(cdata)] != null) {
					// スケジュールデータを更新
					mScheduleData.setNextRouteData(mNextRouteData,next_departure);
				} else {
					if (TOAST_FLG) {
						Toast.makeText(context, R.string.route_error,
								Toast.LENGTH_LONG).show();
						toast_err = false;
					}
				}
			}
		}

		pref = PreferenceManager.getDefaultSharedPreferences(context);
		mHomeStation = pref.getString("set_home_station", null);

		if (mHomeStation != null && mCurrentAddress != null) {
			// 終電検索
			RouteSearch lastroutesearch = new RouteSearch(mCurrentAddress);
			mLastRouteData = lastroutesearch.getLastRoute(mHomeStation);
			last_departure = lastroutesearch.getDeparture(TYPE_LAST_TRAIN);

			if (mLastRouteData != null) {
				// スケジュールデータを更新
				mScheduleData.setLastRouteData(mLastRouteData, last_departure);
			} else {
				if (TOAST_FLG) {
					Toast.makeText(context, R.string.lasttrain_error,
							Toast.LENGTH_LONG).show();
					toast_err = false;
				}
			}
		} else if (mHomeStation == null) {
			if (TOAST_FLG) {
				Toast.makeText(context, R.string.station_error,
						Toast.LENGTH_LONG).show();
				toast_err = false;
			}
		}

		// データベース更新
		DatabaseAccess databaseAccess = new DatabaseAccess(context);
		databaseAccess.setSchedule(mScheduleData);

		if (mScheduleData != null) {

			Calendar calendar1 = Calendar.getInstance();
			String nowtime = calendar1.get(Calendar.YEAR) + "/"
					+ calendar1.get(Calendar.MONTH) + "/"
					+ calendar1.get(Calendar.DAY_OF_MONTH) + " "
					+ calendar1.get(Calendar.HOUR_OF_DAY) + ":"
					+ calendar1.get(Calendar.MINUTE) + ":"
					+ calendar1.get(Calendar.SECOND);

			if (mScheduleData.getNextRouteData() != null) {
				String scheduletime = calendar1.get(Calendar.YEAR)
						+ "/"
						+ calendar1.get(Calendar.MONTH)
						+ "/"
						+ calendar1.get(Calendar.DAY_OF_MONTH)
						+ " "
						+ mScheduleData.getNextRouteData()[0][0].get(0)
								.getATime();
				Nexttime = getTimeDiff(nowtime, scheduletime);

				if (Nexttime > 0) {
					ALMsetting(context, Nexttime);
				}
			}

			if (mScheduleData.getLastRouteData() != null) {
				String lastscheduletime = calendar1.get(Calendar.YEAR) + "/"
						+ calendar1.get(Calendar.MONTH) + "/"
						+ calendar1.get(Calendar.DAY_OF_MONTH) + " "
						+ mScheduleData.getLastRouteData()[0].get(0).getATime();
				Lasttime = getTimeDiff(nowtime, lastscheduletime);

				// アラーム登録をする
				if (Lasttime > 0) {
					ALMsetting(context, Lasttime);
				}
			}
		}

		if (TOAST_FLG && !toast_err) {
			Toast.makeText(context, R.string.refresh_end, Toast.LENGTH_LONG)
					.show();
		}

		if (intent.getStringExtra("return") != null) {
			if (intent.getStringExtra("return").equals("MainActivity")) {
				Intent ViewActivity = new Intent(context, MainActivity.class);
				ViewActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(ViewActivity);
			}
		}
	}

	public long getTimeDiff(String date1, String date2) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		long time = -1;
		Date dtday1 = null;
		Date dtday2 = null;

		try {
			dtday1 = formatter.parse(date1); // 文字列 → 日付
			dtday2 = formatter.parse(date2); // 文字列 → 日付
		} catch (ParseException e) { // 書式エラー
			return time;
		} catch (Exception e) {
			return time;
		}
		time = (dtday2.getTime() - dtday1.getTime()) / 1000;
		return time;
	}

	/**
	 * アラーム情報を登録する
	 */
	@SuppressWarnings("static-access")
	public void ALMsetting(Context context, Long time) {
		Intent intent = new Intent(context, AlmManager.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alm = (AlarmManager) context
				.getSystemService(context.ALARM_SERVICE);
		alm.set(AlarmManager.ELAPSED_REALTIME, time, sender);
	}
}
