package com.blogspot.yakisobayuki.dtn;

public class CalenderData {
	/** 【次の予定】カレンダーから取得する イベント名/会場/開始時間 */
	private String event_name = null;
	private String location = null;
	private String start_time = null;

	/**
	 * カレンダーから取得したデータを格納
	 *
	 * @param event
	 *            イベント名
	 * @param locate
	 *            会場
	 * @param start
	 *            開始時間
	 */
	public void setSchedule(String event, String locate, String start) {
		event_name = event;
		location = locate;
		start_time = start;
	}

	/**
	 * イベント名を取得
	 *
	 * @return イベント名
	 */
	public String getEvent() {
		return event_name;
	}

	/**
	 * イベント会場名を取得
	 *
	 * @return イベント会場
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * イベント開始時間を取得
	 *
	 * @return　開始時間
	 */
	public String getStartTime() {
		return start_time;
	}
}
