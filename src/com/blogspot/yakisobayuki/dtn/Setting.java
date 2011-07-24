package com.blogspot.yakisobayuki.dtn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class Setting extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private CheckBoxPreference mRouteRefresh;
	private CheckBoxPreference mAlarm;
	private EditTextPreference mSelectStation;
	private ListPreference mSelectTime;
	private CharSequence summary1;
	private CharSequence summary2;
	private ImageView imageTitle, imageView1, imageView2;

	final String TAGtest = "TrainTransferTest";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.layout.setting);

		setTitle();

		mRouteRefresh = (CheckBoxPreference) findPreference("refresh_onoff");
		mRouteRefresh
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object value) {
						return MngThreadStart(pref, value);
					}
				});

		mAlarm = (CheckBoxPreference) findPreference("alarm_onoff");
		mAlarm.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference pref, Object value) {
				return AlarmSetting(pref, value);
			}
		});

		mSelectStation = (EditTextPreference) findPreference("set_home_station");
		mSelectTime = (ListPreference) findPreference("set_alarm_time");
		summary1 = mSelectStation.getSummary();
		summary2 = mSelectTime.getSummary();
		
		if(!mAlarm.isChecked()){
			mSelectTime.setEnabled(false);
		}

		// Summary表示更新
		setPref();
	}
	
	@Override
	public void onStart(){
		super.onStart();
		if(mRouteRefresh.isChecked()){
			mRouteRefresh.setSummary("1時間毎の情報更新：起動中");
		}else{
			mRouteRefresh.setSummary("1時間毎の情報更新：停止中");
		}

		if(mAlarm.isChecked()){
			mAlarm.setSummary("出発時間のアラーム機能：起動中");
		}else{
			mAlarm.setSummary("出発時間のアラーム機能：停止中");
		}
	}

	private void setTitle() {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.action_bar);

		LayoutParams params = new LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);

		imageTitle = (ImageView) findViewById(R.id.title_icon);
		imageTitle.setLayoutParams(params);
		imageTitle.setImageResource(R.drawable.setting);

		imageView1 = (ImageView) findViewById(R.id.actionbar1);
		imageView2 = (ImageView) findViewById(R.id.actionbar2);
		imageView1.setVisibility(View.INVISIBLE);
		imageView2.setVisibility(View.INVISIBLE);
	}

	public boolean MngThreadStart(Preference pref, Object value) {
		// チェックされた
		try {
			if (((Boolean) value).booleanValue()) {
				((CheckBoxPreference) pref).setSummary("1時間毎の情報更新：起動中");
				Intent intent = new Intent(this, MngThread.class);
				PendingIntent sender = PendingIntent.getBroadcast(this, 0,
						intent, 0);
				AlarmManager alm = (AlarmManager) getSystemService(ALARM_SERVICE);
				alm.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60 * 60 *1000,
						sender);
			}
			// チェック外れた
			else {
				((CheckBoxPreference) pref).setSummary("1時間毎の情報更新：停止中");
				Intent intent = new Intent(this, MngThread.class);
				PendingIntent sender = PendingIntent.getBroadcast(this, 0,
						intent, 0);
				AlarmManager alm = (AlarmManager) getSystemService(ALARM_SERVICE);
				alm.cancel(sender);
			}
		} catch (Exception e) {
			Log.d(TAGtest, e.toString());
			return false;
		}
		return true;
	}

	public boolean AlarmSetting(Preference pref, Object value) {
		// チェックされた
		try {
			if (((Boolean) value).booleanValue()) {
				((CheckBoxPreference) pref).setSummary("出発時間のアラーム機能：起動中");
				mSelectTime.setEnabled(true);
			}
			// チェック外れた
			else {
				((CheckBoxPreference) pref).setSummary("出発時間のアラーム機能：停止中");
				mSelectTime.setEnabled(false);
			}
		} catch (Exception e) {
			Log.d(TAGtest, e.toString());
			return false;
		}
		return true;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String Key) {
		// Summary表示更新
		setPref();
	}

	private void setPref() {
		// Summary設定
		if (mSelectStation.getText() == null || mSelectStation.getText().equals("")) {
			mSelectStation.setSummary(summary1 + "\n最寄駅設定：未設定");
		} else {
			mSelectStation.setSummary(summary1 + "\n最寄駅設定："
					+ mSelectStation.getText());
		}

		if (mSelectTime.getValue() != null) {
			mSelectTime.setSummary(summary2 + "\nアラーム時間設定：出発"
					+ mSelectTime.getValue() + "分前");
		} else {
			mSelectTime.setSummary(summary2 + "\nアラーム時間設定：出発10分前");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

}
