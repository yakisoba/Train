package com.blogspot.yakisobayuki.dtn;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import android.util.Log;

/**
 * ルート検索用クラス
 * 
 */
public class RouteSearch {
	List<Route>[] mList = null;

	/** Google乗り換え案内のURL */
	private static final String GOOGLE_URL = "http://www.google.co.jp/m/directions?";

	/** 現在地の住所 */
	private String currentaddress = null;
	/** 次のイベントの開始時間 */
	public String next_starttime = null;
	/** 次のイベントの会場 */
	private String next_location = null;
	/** タイプ(次スケジュール/終電) */
	private String type = null;

	/** ログ用タグ */
	final String TAG = "TrainTransfer";
	final String TAGtest = "TrainTransferTest";

	private String[] Turnaround_Time = null;		//所要時間
	private String[] arrival_time = null;			//到着時間

	/** 今日の日付 */
	final Calendar mCalendar = Calendar.getInstance();
	final String mToday = Integer.toString(mCalendar.get(Calendar.YEAR))
			+ Integer.toString(mCalendar.get(Calendar.MONTH) + 1)
			+ Integer.toString(mCalendar.get(Calendar.DAY_OF_MONTH));

	/**
	 * 現在地だけを登録するコンストラクター
	 * 
	 * @param address
	 *            現在地
	 */
	public RouteSearch(String address) {
		this.currentaddress = address;
	}

	/**
	 * GoogleTransferへ必要なデータを渡しつつアクセス
	 * 
	 * @return HTMLデータ(byte型)
	 */
	private byte[] GoogleTransferAccess() {
		byte[] w = null;
		String url = null;

		// URLの生成
		url = GOOGLE_URL + "dirflg=r" // ここまで基本設定
				+ "&saddr=" + currentaddress // 発
				+ "&daddr=" + next_location // 着
				+ "&date=" + mToday // 日付
				+ "&time=" + next_starttime // 時刻
				+ "&ttype=" + type // 到着時刻or終電
				+ "&btnG=この条件で検索";

		HttpClient c = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(c.getParams(), 5000);
		HttpConnectionParams.setSoTimeout(c.getParams(), 8000);

		HttpGet method = new HttpGet(url);
		HttpResponse response = null;

		try {
			response = c.execute(method);

			int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				throw new Exception("");
			} else if (status < 400) {
				w = EntityUtils.toByteArray(response.getEntity());
				return w;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * byte出力されたデータを"div"毎に配列に収納する
	 * 
	 * @param w
	 *            格納元のHTMLデータ
	 * @return 整形したデータ
	 */
	private String[] ConvertByteString(byte[] w) {
		String str = null;
		String[] output = null;

		if (w != null) {
			// byte型→string型に変換し、tab排除
			try {
				str = new String(w, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			Pattern p = Pattern.compile("<p>");
			output = p.split(str);
			return output;
		}
		return null;
	}

	/**
	 * 整形したデータから、必要なデータだけ取得する
	 * 
	 * @param str
	 *            "div"で区切られたデータ
	 * @return 出着時間や駅、乗り換えの情報など
	 */
	@SuppressWarnings("unchecked")
	private void getInfo(String[] str) {
		Route route = null;
		int route_number = 0;
		int route_num = 0;
		String[] dataSplit = null;
		String[] out = null;
		mList = new ArrayList[3];
		Turnaround_Time = new String[3];
		arrival_time = new String[3];

		if (str != null) {

			// [鴨居駅(神奈川) ～ 新横浜駅(神奈川) 11:00着]の表示
			// str[3].substring(0,str[3].length() - 6);

			// <br>でさらに分割
			Pattern p = Pattern.compile("<br/>");
			dataSplit = p.split(str[4]);

			// 分割した行数ループ
			for (int i = 0; i < dataSplit.length; i++) {
				Log.d(TAGtest,dataSplit[i]);

				// 検索結果3つの最初に入る箇所
				if (dataSplit[i].equals("   ")) {
					route_num = 0;

					// Route型のリストを作成
					mList[route_number] = new ArrayList<Route>();
					// [5分 - 150円]の表示
					Turnaround_Time[route_number] = dataSplit[i + 1].substring(0,
							dataSplit[i + 1].indexOf("-") - 1);
				}

				else if (dataSplit[i].equals("  ")) {
					route_num = 0;

					// リストに登録
					mList[route_number].add(route);
					// Route型のリストを作成
					mList[++route_number] = new ArrayList<Route>();
					// [5分 - 150円]の表示
					Turnaround_Time[route_number] = dataSplit[i + 1].substring(0,
							dataSplit[i + 1].indexOf("-") - 1);
				}

				// imgを含むのは、路線名と方向
				else if (dataSplit[i].indexOf("<img") != -1) {
					if (route_num == 0) {
						route = new Route();
					} else {
						// リストに登録
						mList[route_number].add(route);
						route = new Route();
					}
					out = new String[5];

					// 路線名を取得
					out[0] = dataSplit[i].substring(
							dataSplit[i].indexOf("/>") + 2,
							dataSplit[i].length());
					route.setTrans(out[0]);

					route_num++;
				}

				// 発 を含むのは、発射時刻と乗り駅
				else if (dataSplit[i].indexOf("発 ") != -1) {
					// 出発時間と出発駅を取得
					int point = dataSplit[i].indexOf("発");
					out[1] = dataSplit[i].substring(0, point);
					out[2] = dataSplit[i].substring(point + 2,
							dataSplit[i].length());
					route.setDTime_Locate(out[1], out[2]);
				}

				// 着 を含むのは、到着時刻と降り駅
				else if (dataSplit[i].indexOf("着 ") != -1) {
					int point = dataSplit[i].indexOf("着");
					out[3] = dataSplit[i].substring(0, point);
					out[4] = dataSplit[i].substring(point + 2,
							dataSplit[i].length());
					route.setATime_Locate(out[3], out[4]);
					arrival_time[route_number] = out[3];
				}

				else if (dataSplit[i].indexOf(" </p>") != -1) {
					// リストに登録
					mList[route_number].add(route);
					
					// 終電先が徒歩で歩ける場所にあるとエラーが出てしまうよ。
				}
			}
		}
	}

	/**
	 * 検索したルートを返す
	 * 
	 * @return ルート
	 */
	private void getRoute() {
		byte[] w = null;
		String[] str = null;

		// ルート検索した結果をHTMLデータで出力
		w = GoogleTransferAccess();

		// byte型のデータを整形して、String[]に変換
		str = ConvertByteString(w);

		// 必要なデータをリストに格納
		getInfo(str);
	}

	/**
	 * 次のスケジュールのルート検索
	 * 
	 * @param cdata
	 *            カレンダーデータ
	 * @return ルート検索した
	 */
	public List<Route>[] getNextRoute(CalenderData cdata) {
		this.next_starttime = cdata.getStartTime();
		this.next_location = cdata.getLocation();
		this.type = "arr";

		getRoute();
		return mList;
	}

	/**
	 * 終電のルート検索
	 * 
	 * @param data
	 *            家の位置
	 * @return ルート検索した
	 */
	public List<Route>[] getLastRoute(String data) {
		this.next_starttime = "1200";
		this.next_location = data;
		this.type = "last";

		getRoute();
		return mList;
	}

	/**
	 * 出発時間を算出する
	 * @param type 次予定or終電
	 * @return 出発時間 (HH:MM)
	 */
	public String[] getDeparture(int type) {
		String[] arr = new String[Turnaround_Time.length];

		try {
			// 所要時間を時間と分に分けて取得
			for (int i = 0; i < Turnaround_Time.length; i++) {
				if(Turnaround_Time[i] ==null){
					return arr;
				}
				
				int hour = 0, minute = 0;
				int hour_minus = 0, minute_minus = 0;
				int h_index = Turnaround_Time[i].indexOf("時間");
				int m_index = Turnaround_Time[i].indexOf("分");
				
				if (h_index != -1 && m_index != -1) {
					hour_minus = Integer.parseInt(Turnaround_Time[i].substring(0,h_index));
					minute_minus = Integer.parseInt(Turnaround_Time[i].substring(h_index+2, m_index));
				} else if (h_index != -1) {
					hour_minus = Integer.parseInt(Turnaround_Time[i].substring(0,h_index));
				} else if (m_index != -1) {
					minute_minus = Integer.parseInt(Turnaround_Time[i].substring(0, m_index));
				}

				switch (type) {
				case 0:
					// 次の予定は次の予定時間から算出
					hour = Integer.parseInt(next_starttime.substring(0,2));
					minute = Integer.parseInt(next_starttime.substring(2,4));

					break;
				case 1:
					// 終電のときは、最後の電車の降りた時間から算出
					hour = Integer.parseInt(arrival_time[i].substring(0,
							arrival_time[i].indexOf(":")));
					minute = Integer.parseInt(arrival_time[i].substring(
							arrival_time[i].indexOf(":")+1,
							arrival_time[i].length()));
					break;
				default:
					break;
				}
				
				// カレンダーに到着時間を格納し、所要時間を減算する
				mCalendar.set(mCalendar.get(Calendar.YEAR),
						mCalendar.get(Calendar.MONTH),
						mCalendar.get(Calendar.DATE), hour, minute);
				mCalendar.add(Calendar.HOUR_OF_DAY, -hour_minus);
				mCalendar.add(Calendar.MINUTE, -minute_minus);

				// 減算した時間を格納
				arr[i] = mCalendar.get(Calendar.HOUR_OF_DAY) + ":"
						+ mCalendar.get(Calendar.MINUTE);
			}

		} catch (Exception e) {
			Log.e(TAGtest,"ERROR " + e);
			return null;
		}

		return arr;

	}
}