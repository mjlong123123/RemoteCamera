package com.android.remotecamera;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewListener;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.android.localcall.jni.Rtp;
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
	// 传输数据
	private Rtp mRtp = null;

	private Camera mCamera = null;

	private LinearLayout mLinearLayoutAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.camera_activity_layout);
		// 开启rtp传输
		mRtp = new Rtp();
		mRtp.openRtp(40018);
		Intent intent = getIntent();
		if (intent == null) {
			finish();
		}
		String ip = intent.getStringExtra("IP");
		if (ip == null || ip.equals("")) {
			finish();
		}
		mRtp.addRtpDestinationIp(ip);
		initView();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		mRtp.closeRtp();
		mRtp.native_rease();
		mRtp = null;
		super.onDestroy();
	}

	private void initView() {
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview_preview);
		mSurfaceView.getHolder().addCallback(this);

		Display display = getWindowManager().getDefaultDisplay();
		mScreenW = display.getWidth();
		mScreenH = display.getHeight();
		
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

//		CamcorderProfile cp = CamcorderProfile
//				.get(CamcorderProfile.QUALITY_QVGA);

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
		mCustomMediaRecorder = new CustomMediaRecorder(mRtp);
		mCustomMediaRecorder.startRecorder(mCamera, mSurfaceView,
				new VideoInfor(6000, 10, mPreviewWidth, mPreviewHeight));
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
