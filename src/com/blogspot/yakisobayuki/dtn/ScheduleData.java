package com.blogspot.yakisobayuki.dtn;

import java.util.ArrayList;
import java.util.List;

/**
 * 次の予定/終電の情報
 * 
 */
public class ScheduleData {

	private List<CalenderData> mCalenderData = new ArrayList<CalenderData>();
	private String mCurrentAddress = new String();
	private List<Route>[][] mNextSchedule;
	private List<Route>[] mLastTrain;
	private String[][] NextDeparture = new String[2][3];
	private String[] LastDeparture = new String[3];

	/**
	 * 現在値を設定する
	 * 
	 * @param address
	 *            現在値
	 */
	public void setCurrentAddress(String address) {
		this.mCurrentAddress = address;
	}

	/**
	 * 現在値を取得する
	 * 
	 * @return 現在値
	 */
	public String getCurrentAddress() {
		return mCurrentAddress;
	}

	/**
	 * カレンダーデータを格納する
	 * 
	 * @param data
	 *            カレンダーデータ(2個分)
	 */
	public void setCalendar(List<CalenderData> data) {
		this.mCalenderData = data;
	}

	/**
	 * カレンダーデータを取得する
	 * 
	 * @return カレンダーデータ
	 */
	public List<CalenderData> getCalendar() {
		return mCalenderData;
	}

	/**
	 * 次予定のルート検索データを格納する。
	 * 
	 * @param data
	 *            ルートデータ[2カレンダー分][3ルート分]
	 * @param arrival
	 *            次予定の出発時間[2カレンダー分][3ルート分]
	 */
	public void setNextRouteData(List<Route>[][] data, String[][] departure) {
		this.mNextSchedule = data;
		this.NextDeparture = departure;
	}

	/**
	 * 次予定のルート検索データを取得する
	 * 
	 * @return ルートデータ
	 */
	public List<Route>[][] getNextRouteData() {
		return mNextSchedule;
	}

	/**
	 * 次予定の出発時間を取得する
	 * 
	 * @return 出発時間
	 */
	public String[][] getNextDeparture() {
		return NextDeparture;
	}

	/**
	 * 終電のルート検索データを格納する。
	 * 
	 * @param data
	 *            ルートデータ[3ルート分？]
	 * @param arrival
	 *            次予定の出発時間[3ルート分？]
	 */
	public void setLastRouteData(List<Route>[] data, String[] departure) {
		this.mLastTrain = data;
		this.LastDeparture = departure;
	}

	/**
	 * 終電のルート検索データを取得する
	 * 
	 * @return ルートデータ
	 */
	public List<Route>[] getLastRouteData() {
		return mLastTrain;
	}

	/**
	 * 終電の出発時間を取得する
	 * 
	 * @return 出発時間
	 */
	public String[] getLastDeparture() {
		return LastDeparture;
	}
}
