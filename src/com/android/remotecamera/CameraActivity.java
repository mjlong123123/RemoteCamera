package com.android.remotecamera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.android.remotecamera.CustomMediaRecorder.VideoInfor;
import com.example.remotecamera.R;

public class CameraActivity extends BaseActivity implements Callback {
	private final static String TAG = "CameraActivity";
	private int mPreviewWidth = 0;
	private int mPreviewHeight = 0;
	private int mScreenW = 0;
	private int mScreenH = 0;
	private int mSurfaceW = 0;
	private int mSurfaceH = 0;
	private SurfaceView mSurfaceView;
	// 支持socket传输的MediaRecorder
	private CustomMediaRecorder mCustomMediaRecorder = null;

	private Camera mCamera = null;

	
	private String mIP;

	private WakeLock wl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.camera_activity_layout);
		Intent intent = getIntent();
		if (intent == null) {
			finish();
		}
		mIP = intent.getStringExtra("IP");
		if (mIP == null || mIP.equals("")) {
			finish();
		}
		initView();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
@Override
protected void onResume() {
	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "my_wakelock");
	wl.acquire();
	super.onResume();
}
@Override
protected void onPause() {
	wl.release();
	wl = null;
	super.onPause();
}
	private void initView() {
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview_preview);
		mSurfaceView.getHolder().addCallback(this);

		Display display = getWindowManager().getDefaultDisplay();
		mScreenW = display.getWidth();
		mScreenH = display.getHeight();
		
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.e(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		Log.e(TAG, "surfaceCreated");
		if (mCustomMediaRecorder != null) {
			mCustomMediaRecorder.stopRecorder();
			mCustomMediaRecorder = null;
		}
		mCamera = Camera.open();

//		List<Size> list = mCamera.getParameters().getSupportedVideoSizes();
//		Size s;
//		for (int i = 0; i < list.size(); i++) {
//			s = list.get(i);
//			Log.e(TAG, "w:" + s.width);
//			Log.e(TAG, "h:" + s.height);
//		}

		mPreviewWidth = 320;
		mPreviewHeight = 240;

		mCamera.setDisplayOrientation(90);
		mCustomMediaRecorder = new CustomMediaRecorder(mIP);
		mCustomMediaRecorder.startRecorder(mCamera, mSurfaceView,
				new VideoInfor(6000, 20, mPreviewWidth, mPreviewHeight));
		mSurfaceW = mScreenW;
		mSurfaceH = (int) (mScreenW * (1.0f * mPreviewWidth / mPreviewHeight));
		if (mSurfaceH > mScreenH) {
			mSurfaceW = (int) (mScreenH * (1.0f * mPreviewHeight / mPreviewWidth));
			mSurfaceH = mScreenH;
		}
		arg0.setFixedSize(mSurfaceW, mSurfaceH);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.e(TAG, "surfaceDestroyed");
		if (mCustomMediaRecorder != null) {
			mCustomMediaRecorder.stopRecorder();
			mCustomMediaRecorder = null;
		}
		mCamera.release();
		mCamera = null;
	}
}
