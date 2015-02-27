package com.android.remotecamera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.remotecamera.util.UtilsExt;
import com.example.remotecamera.R;

public class DialActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "DialActivity";

	private EditText mEditor = null;
	private Button mButton0 = null;
	private Button mButton1 = null;
	private Button mButton2 = null;
	private Button mButton3 = null;
	private Button mButton4 = null;
	private Button mButton5 = null;
	private Button mButton6 = null;
	private Button mButton7 = null;
	private Button mButton8 = null;
	private Button mButton9 = null;
	private Button mButtonp = null;
	private Button mButtond = null;
	private Button mButtonCall = null;

	private Button buttondel = null;

	private StringBuffer mTextBuffer = new StringBuffer(15);

	private String mIp = "";
	private int mPort = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_dial);
		findView();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void findView() {
		mEditor = (EditText) findViewById(R.id.editText1);
		mButton0 = (Button) findViewById(R.id.button0);
		mButton1 = (Button) findViewById(R.id.button1);
		mButton2 = (Button) findViewById(R.id.button2);
		mButton3 = (Button) findViewById(R.id.button3);
		mButton4 = (Button) findViewById(R.id.button4);
		mButton5 = (Button) findViewById(R.id.button5);
		mButton6 = (Button) findViewById(R.id.button6);
		mButton7 = (Button) findViewById(R.id.button7);
		mButton8 = (Button) findViewById(R.id.button8);
		mButton9 = (Button) findViewById(R.id.button9);
		mButtonp = (Button) findViewById(R.id.buttonp);
		mButtond = (Button) findViewById(R.id.buttond);
		mButtonCall = (Button) findViewById(R.id.buttonc);
		buttondel = (Button) findViewById(R.id.buttondel);

		mButton0.setOnClickListener(this);
		mButton1.setOnClickListener(this);
		mButton2.setOnClickListener(this);
		mButton3.setOnClickListener(this);
		mButton4.setOnClickListener(this);
		mButton5.setOnClickListener(this);
		mButton6.setOnClickListener(this);
		mButton7.setOnClickListener(this);
		mButton8.setOnClickListener(this);
		mButton9.setOnClickListener(this);
		mButtonp.setOnClickListener(this);
		mButtond.setOnClickListener(this);
		mButtonCall.setOnClickListener(this);
		buttondel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button0:
			mTextBuffer.append(0);
			break;
		case R.id.button1:
			mTextBuffer.append(1);
			break;
		case R.id.button2:
			mTextBuffer.append(2);
			break;
		case R.id.button3:
			mTextBuffer.append(3);
			break;
		case R.id.button4:
			mTextBuffer.append(4);
			break;
		case R.id.button5:
			mTextBuffer.append(5);
			break;
		case R.id.button6:
			mTextBuffer.append(6);
			break;
		case R.id.button7:
			mTextBuffer.append(7);
			break;
		case R.id.button8:
			mTextBuffer.append(8);
			break;
		case R.id.button9:
			mTextBuffer.append(9);
			break;
		case R.id.buttonp:
			mTextBuffer.append(".");
			break;
		case R.id.buttond:
			mTextBuffer.append(".");
			break;
		case R.id.buttondel:
			try {
				mTextBuffer.deleteCharAt(mTextBuffer.length() - 1);
			} catch (Exception e) {
			}
			break;
		case R.id.buttonc: {
			String ret = mEditor.getText().toString();
			boolean fault = true;
			if (ret != null) {
				mIp = ret;

				if (UtilsExt.isIpAddress(mIp)) {
					fault = false;
				}
			}

			if (fault) {
				Toast.makeText(DialActivity.this, R.string.ip_error,
						Toast.LENGTH_SHORT).show();
			} else {
				Intent intent = new Intent();
				intent.setClass(DialActivity.this, CameraActivity.class);
				intent.putExtra("IP", mIp);
				DialActivity.this.startActivity(intent);
			}
		}
			break;
		}
		mEditor.setText(mTextBuffer.toString());
	}

}
