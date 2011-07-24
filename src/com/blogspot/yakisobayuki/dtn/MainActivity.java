package com.blogspot.yakisobayuki.dtn;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.DurationFormatUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	private int route_select = 0;
	private int title_select = 0;
	private Button button1, button2;
	private ImageView imageView1, imageView2, imageTitle;
	private TextView textView1, textView2, textView3;
	private TextView dtime, dlocate, atime, alocate;
	private Timer timer;
	private Handler handler;
	private Date date1, date2, date3;
	private String time1, time2;

	private DataAdapter mAdapter = null;
	private ListView listView;

	final String TAGtest = "TrainTransferTest";
	final boolean DEBUG = true;

	final int NextSchedule = 0;
	final int LastTrain = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.dashboard);

		AssetManager mgr = getResources().getAssets();
		textView1 = (TextView) findViewById(R.id.text2);
		textView1
				.setTypeface(Typeface.createFromAsset(mgr, "font/7barPBd.TTF"));

		textView2 = (TextView) findViewById(R.id.event1);
		textView3 = (TextView) findViewById(R.id.event2);

		dtime = (TextView) findViewById(R.id.deperture_time);
		dlocate = (TextView) findViewById(R.id.deperture_location);
		atime = (TextView) findViewById(R.id.arrival_time);
		alocate = (TextView) findViewById(R.id.arrival_location);

		button1 = (Button) findViewById(R.id.prev_route);
		button2 = (Button) findViewById(R.id.next_route);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);

		listView = (ListView) findViewById(R.id.list);

		date2 = new Date();
		date3 = new Date();

		setTitle();
		set_NextSchedule();

		timer = new Timer(true);
		handler = new Handler();
		timer.schedule(new Task(), 1000, 1000);

		// 表示処理
		setView();
	}

	@Override
	public void onStart() {
		super.onStart();
		setView();

	}

	private void setView() {
		// DB参照
		DatabaseAccess database = new DatabaseAccess(this);
		database.getSchedule();

		switch (title_select) {
		case NextSchedule:
			if (database.toCalendar(0) != null) {
				String Event = database.toCalendar(0).getEvent();
				textView3.setText(Event);
			}else{
				textView3.setText("イベント未取得");
			}

			if (database.toNextData(route_select) != null) {
				date2 = database.toNextData(route_select);
			} else {
				date2 = null;
			}

			// 全体の出発時間場所、到着時間場所
			if (database.toStringNextTimeLocation(0, route_select) != null) {
				String[][] time_locate = new String[2][2];
				time_locate = database
						.toStringNextTimeLocation(0, route_select);

				dtime.setText(time_locate[0][0]);
				dlocate.setText(time_locate[0][1]);
				atime.setText(time_locate[1][0]);
				alocate.setText(time_locate[1][1]);
			}else{
				dtime.setText("--:--発");
				dlocate.setText("出発地点未取得");
				atime.setText("--:--着");
				alocate.setText("到着地点未取得");
			}

			// 取得したデータをviewにセット
			if (database.toNextRoute(0, route_select) != null) {
				mAdapter = new DataAdapter(this, R.layout.listview,
						database.toNextRoute(0, route_select));
				listView.setAdapter(mAdapter);
			}else{
				listView.setAdapter(null);
			}

			// ボタンの表示
			if (route_select < 2) {
				if (database.toNextRoute(0, route_select + 1) == null
						|| database.toNextRoute(0, route_select + 1).get(0)
								.getTrans() == null) {
					button2.setEnabled(false);
				}
			}

			break;

		case LastTrain:
			// 全体の出発時間場所、到着時間場所
			if (database.toStringLastTimeLocation(route_select) != null) {
				String[][] time_locate = new String[2][2];
				time_locate = database.toStringLastTimeLocation(route_select);

				dtime.setText(time_locate[0][0]);
				dlocate.setText(time_locate[0][1]);
				atime.setText(time_locate[1][0]);
				alocate.setText(time_locate[1][1]);
			}else{
				dtime.setText("--:--発");
				dlocate.setText("出発地点未取得");
				atime.setText("--:--着");
				alocate.setText("到着地点未取得");
			}

			// 取得したデータをviewにセット
			if (database.toLastRoute(route_select) != null) {
				mAdapter = new DataAdapter(this, R.layout.listview,
						database.toLastRoute(route_select));
				listView.setAdapter(mAdapter);
			}else{
				listView.setAdapter(null);
			}

			if (database.toLastData(route_select) != null) {
				date3 = database.toLastData(route_select);
			} else {
				date3 = null;
			}

			// ボタンの表示
			if (route_select < 2) {
				if (database.toLastRoute(route_select + 1) == null
						|| database.toLastRoute(route_select + 1).get(0)
								.getTrans() != null) {
					button2.setEnabled(false);
				}
			}

			break;

		default:
			break;
		}

		database.close();
	}

	public class DataAdapter extends ArrayAdapter<Route> {
		private LayoutInflater inflater;
		private List<Route> items;

		public DataAdapter(Context context, int textViewResourceId,
				List<Route> items) {
			super(context, textViewResourceId, items);

			this.items = items;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = inflater.inflate(R.layout.listview, null);
			final Route item = getItem(position);

			if (item != null) {
				TextView transfer, StartLocation, EndLocation;
				ImageView imageView;

				transfer = (TextView) view.findViewById(R.id.Transfer);
				StartLocation = (TextView) view
						.findViewById(R.id.StartLocation);
				EndLocation = (TextView) view.findViewById(R.id.EndLocation);
				imageView = (ImageView) view.findViewById(R.id.cursor);

				transfer.setText(item.getTrans());

				if (item.getDTime() == null) {
					StartLocation.setVisibility(View.INVISIBLE);
					EndLocation.setVisibility(View.INVISIBLE);
				} else {
					StartLocation.setText(item.getDTime() + "発 "
							+ item.getDLocate());
					EndLocation.setText(item.getATime() + "着 "
							+ item.getALocate());
				}

				if (position == (items.size() - 1)) {
					imageView.setVisibility(View.INVISIBLE);
				}
			}
			return view;
		}
	}

	class Task extends TimerTask {
		@Override
		public void run() {
			handler.post(new Runnable() {
				public void run() {
					date1 = new Date();

					time1 = toDiffTime(date1, date2);
					time2 = toDiffTime(date1, date3);

					if (title_select == 0) {
						textView1.setText(time1);
					} else {
						textView1.setText(time2);
					}
				}
			});
		}

		public String toDiffTime(Date startTime, Date endTime) {
			if (startTime == null || endTime == null) {
				return "00:00:00";
			} else {
				String diffTime = DurationFormatUtils.formatPeriod(
						startTime.getTime(), endTime.getTime(), "HH:mm:ss");
				return diffTime;
			}
		}
	}

	private void setTitle() {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.action_bar);

		imageTitle = (ImageView) findViewById(R.id.title_icon);
		imageView1 = (ImageView) findViewById(R.id.actionbar1);
		imageView1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (title_select == 0) {
					title_select = 1;
					route_select = 0;
					set_LastTrain();
				} else {
					title_select = 0;
					route_select = 0;
					set_NextSchedule();
				}
			}
		});

		imageView2 = (ImageView) findViewById(R.id.actionbar2);
		imageView2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("return", "MainActivity");
				intent.setAction("dtn.android.ViewAction.VIEW");
				sendBroadcast(intent);
			}
		});

	}

	private void set_NextSchedule() {
		imageTitle.setImageResource(R.drawable.nextschedule);
		button1.setEnabled(false);
		button2.setEnabled(true);

		if(time1 == null || time1.length() != 8){
			textView1.setText("00:00:00");
		}else{
			textView1.setText(time1);
		}

		textView2.setVisibility(View.VISIBLE);
		textView3.setVisibility(View.VISIBLE);

		setView();
	}

	private void set_LastTrain() {
		imageTitle.setImageResource(R.drawable.lasttrain);
		button1.setEnabled(false);
		button2.setEnabled(true);

		if(time1 == null || time1.length() != 8){
			textView1.setText("00:00:00");
		}else{
			textView1.setText(time1);
		}
		textView2.setVisibility(View.INVISIBLE);
		textView3.setVisibility(View.INVISIBLE);

		setView();
	}

	@Override
	public void onClick(View v) {
		if (v == button1) {
			route_select--;
		} else if (v == button2) {
			route_select++;
		}

		if (route_select == 0) {
			button1.setEnabled(false);
		} else if (route_select == 2) {
			button2.setEnabled(false);
		} else {
			button1.setEnabled(true);
			button2.setEnabled(true);
		}

		setView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, R.string.seting_label);
		return true;
	}

	// 　メニュー機能設定。
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// 設定画面表示
			Intent intent = new Intent(this, Setting.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}