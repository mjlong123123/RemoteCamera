package com.android.remotecamera;

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
	private AlertDialog mDialogInputIp;
	private EditText mEditInputIp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.welcome_activity_layout);

		initView();
//		boolean isCreated = restoreShortcut();
//		if(!isCreated)
//		{
//            Intent intent = new Intent();  
//            Intent intentStart = new Intent();
//            intentStart.setClass(this, SplashSpotActivity.class);
//            intentStart.putExtra("goto", "games");
//            //install_shortcut action  
//            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");  
//            //点击shortcut时进入的activity，这里是自己  
//            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intentStart);  
//            //shortcut的name  
//            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "MyShortcut");  
//            Parcelable iconResource = Intent.ShortcutIconResource  
//                    .fromContext(WelcomeActivity.this, R.drawable.ic_launcher);  
//            //shortcut的icon  
//            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,  
//                    iconResource);  
//            //是否可以重复放置shortcut，默认true  
//            intent.putExtra("duplicate", false);  
//            sendBroadcast(intent);  
//            saveShortcut(true);
//		}
		super.onCreate(savedInstanceState);
	}

	private void initView() {
		mOpenCamera = (TextView) findViewById(R.id.open_camera);
		mOpenScreen = (TextView) findViewById(R.id.open_screen);
		mOpenCamera.setOnClickListener(this);
		mOpenScreen.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
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

	private void saveShortcut(boolean isCreated) {
		SharedPreferences sp = getSharedPreferences("isCreated", MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("isCreated", isCreated);
		editor.commit();
	}

	private boolean restoreShortcut() {
		boolean ret = false;
		SharedPreferences sp = getSharedPreferences("isCreated", MODE_PRIVATE);
		ret = sp.getBoolean("isCreated", false);
		return ret;
	}
}
