package com.android.remotecamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.remotecamera.R;

public class WelcomeActivity extends Activity implements View.OnClickListener{
	private Button mButtonCamera;
	private Button mButtonScreen;
	private AlertDialog mDialogInputIp;
	private EditText mEditInputIp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.welcome_activity_layout);
		
		initView();
		
		super.onCreate(savedInstanceState);
	}

	private void initView()
	{
		mButtonCamera = (Button)findViewById(R.id.button_camera);
		mButtonScreen = (Button)findViewById(R.id.button_screen);
		mButtonCamera.setOnClickListener(this);
		mButtonScreen.setOnClickListener(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.button_camera:
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setPositiveButton("OK", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					mEditInputIp = (EditText)mDialogInputIp.findViewById(R.id.edit_ipinput);
					String input = mEditInputIp.getText().toString();
					if(input == null || input.equals(""))
					{
						Toast.makeText(WelcomeActivity.this, "Please input IP", Toast.LENGTH_LONG);
						return;
					}
					Intent intent = new Intent();
					intent.setClass(WelcomeActivity.this, CameraActivity.class);
					intent.putExtra("IP", input);
					WelcomeActivity.this.startActivity(intent);
					saveIp(input);
				}
			});
			builder.setNegativeButton("Cancel", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			builder.setTitle("Please input remote camera ip");
			LayoutInflater li = LayoutInflater.from(WelcomeActivity.this);
			RelativeLayout rl = (RelativeLayout) li.inflate(R.layout.dialog_editor_layout, null);
			mEditInputIp = (EditText) rl.findViewById(R.id.edit_ipinput);
			String ip = restoreIp();
			if(ip != null)
				mEditInputIp.setText(ip);
			builder.setView(rl);
			
			mDialogInputIp = builder.show();
			
		}
			break;
		case R.id.button_screen:
		{
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
