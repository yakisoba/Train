package com.blogspot.yakisobayuki.dtn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * GoogleCalendarにアクセスし、情報を取得するクラス
 * 
 */
public class CalendarAccess {
	final String TAG = "TrainTransfer";

	/**
	 * カレンダー情報取得処理
	 * 
	 * @return カレンダーデータ (次の予定のイベント名、目的地、予定時間)
	 */
	public List<CalenderData> getCalenderSchedule(Context context) {
		ContentResolver contentresolver = context.getContentResolver();
		Cursor cursor = null;
		List<CalenderData> calenderDataList = new ArrayList<CalenderData>();
		long today = new Date().getTime();
		long nextday = (today - today % (1000 * 60 * 60 * 24))
				+ (1000 * 60 * 60 * 24);

		try {
			final Uri CONTENT_URI = Uri
					.parse("content://com.android.calendar/events");

			cursor = contentresolver.query(CONTENT_URI, new String[] { "title",
					"dtstart", "eventLocation" },
					"dtstart >= ? AND dtstart < ?", new String[] {
							Long.toString(today), Long.toString(nextday) },
					"dtstart");

			while (cursor.moveToNext()) {
				// DateFormat ymd = new SimpleDateFormat("yyyyMMdd");
				DateFormat hm = new SimpleDateFormat("HHmm");

				String title = cursor.getString(cursor.getColumnIndex("title"));
				Date dtstart = new Date(cursor.getLong(cursor
						.getColumnIndex("dtstart")));
				String eventlocation = cursor.getString(cursor
						.getColumnIndex("eventLocation"));

				if ((title != null) && (dtstart != null)
						&& (eventlocation != null)) {
					CalenderData cd = new CalenderData();
					cd.setSchedule(title, eventlocation, hm.format(dtstart));
					calenderDataList.add(cd);
					// スケジュール情報２つ取得したら、ループを抜ける。
					if (calenderDataList.size() >= 2) {
						break;
					}
				}
			}
		} catch (Exception e) {

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return calenderDataList;
	}
}
