package com.blogspot.yakisobayuki.dtn;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

/**
 * 出発時刻にアラームで通知するクラス
 *
 */
public class AlmManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		vibrate(context,1000);
		// TODO 自動生成されたメソッド・スタブ
		NotificationManager notificationManager =
			(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification =
			new Notification(android.R.drawable.btn_default,
								"次の予定の出発時間です", System.currentTimeMillis());

		// 通知をクリックされたときのintent設定
		Intent newIntent = new Intent(context, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newIntent, 0);

		notification.setLatestEventInfo(context.getApplicationContext(),
						"TrainTransfer", "ALM", contentIntent);

		// 古い通知をクリアし、最新の情報を通知する
		notificationManager.cancelAll();
		notificationManager.notify(R.string.app_name, notification);

	}
	// バイブレーション
	public void vibrate(Context context, long milliseconds) {
		  ((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(milliseconds);
	}
}
