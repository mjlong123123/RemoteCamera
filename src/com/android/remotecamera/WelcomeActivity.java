package com.android.remotecamera;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewListener;
import net.youmi.android.spot.SpotManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.remotecamera.R;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {
	private TextView mOpenCamera;
	private TextView mOpenScreen;
	private LinearLayout mLinearLayoutAd;
	private AlertDialog mDialogInputIp;
	private EditText mEditInputIp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.welcome_activity_layout);

		initView();

		super.onCreate(savedInstanceState);
	}

	private void initView() {
		mOpenCamera = (TextView) findViewById(R.id.open_camera);
		mOpenScreen = (TextView) findViewById(R.id.open_screen);
		mOpenCamera.setOnClickListener(this);
		mOpenScreen.setOnClickListener(this);
		mLinearLayoutAd = (LinearLayout) findViewById(R.id.ad_linearlayout);
		AdView adView = new AdView(this, AdSize.FIT_SCREEN);
		mLinearLayoutAd.addView(adView);
		adView.setAdListener(new AdViewListener() {

			@Override
			public void onSwitchedAd(AdView arg0) {
				Log.i("YoumiAdDemo", "广告条切换");
			}

			@Override
			public void onReceivedAd(AdView arg0) {
				Log.i("YoumiAdDemo", "请求广告成功");

			}

			@Override
			public void onFailedToReceivedAd(AdView arg0) {
				Log.i("YoumiAdDemo", "请求广告失败");
			}
		});
	}

	@Override
	protected void onDestroy() {
		SpotManager.getInstance(this).onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		SpotManager.getInstance(this).onStop();
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.open_camera: {
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, DialActivity.class);
			this.startActivity(intent);
			/*
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setPositiveButton(R.string.ok, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// mEditInputIp =
					// (EditText)mDialogInputIp.findViewById(R.id.edit_ipinput);
					String input = mEditInputIp.getText().toString();
					if (input == null || input.equals("")) {
						Toast.makeText(WelcomeActivity.this, "Please input IP",
								Toast.LENGTH_LONG);
						return;
					}
					Intent intent = new Intent();
					intent.setClass(WelcomeActivity.this, CameraActivity.class);
					intent.putExtra("IP", input);
					WelcomeActivity.this.startActivity(intent);
					saveIp(input);
				}
			});
			builder.setNegativeButton(R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			builder.setTitle(R.string.dialog_title_input_ip);
			LayoutInflater li = LayoutInflater.from(WelcomeActivity.this);
			RelativeLayout rl = (RelativeLayout) li.inflate(
					R.layout.dialog_editor_layout, null);
			mEditInputIp = (EditText) rl.findViewById(R.id.edit_ipinput);
			String ip = restoreIp();
			if (ip != null)
				mEditInputIp.setText(ip);
			builder.setView(rl);

			mDialogInputIp = builder.show();

		*/}
			break;
		case R.id.open_screen: {
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, ScreenActivity.class);
			WelcomeActivity.this.startActivity(intent);
		}
			break;
		}
	}

	private void saveIp(String ip) {
		SharedPreferences sp = getSharedPreferences("ip_editor", MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("ip", ip);
		editor.commit();
	}

	private String restoreIp() {
		String ret = "";
		SharedPreferences sp = getSharedPreferences("ip_editor", MODE_PRIVATE);
		ret = sp.getString("ip", "192.168.1.1");
		return ret;
	}
}
