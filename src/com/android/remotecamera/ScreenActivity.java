package com.android.remotecamera;
import h264.com.VView;

import java.nio.ByteBuffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.localcall.jni.Rtp;
import com.android.localcall.jni.Rtp.IDataCallback;
import com.example.remotecamera.R;

public class ScreenActivity extends Activity implements Callback {
	private final static String TAG = "ScreenActivity";
	private SurfaceView mSurfaceView;
	// 传输数据
	private Rtp mRtp = null;
	private final static int LOCAL_PORT = 40018;
	private VView mH264Android = null;

	private int mFrameWidth = 0;
	private int mFrameHeight = 0;

	private byte[] mPixel = null;
	private ByteBuffer mBuffer = null;
	private Bitmap mVideoBit = null;

	private SurfaceHolder mSurfaceHolder = null;
	
	private ProgressBar mProgressBar;

	private int mScreenW = 0;
	private int mScreenH = 0;
	private int mSurfaceW = 0;
	private int mSurfaceH = 0;
	
	
	private final static int SHOW_PROCESS = 100;
	private final static int DISMISS_ROCESS = 101;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what)
			{
			case SHOW_PROCESS:
				if(mProgressBar != null)
					mProgressBar.setVisibility(View.VISIBLE);
				Toast.makeText(ScreenActivity.this, "没有接收到数据", Toast.LENGTH_LONG);
				break;
			case DISMISS_ROCESS:
				if(mProgressBar != null)
					mProgressBar.setVisibility(View.GONE);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.screen_activity_layout);
		initView();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	private void initView() {
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mSurfaceView.getHolder().addCallback(this);
		mProgressBar = (ProgressBar)findViewById(R.id.progressbar_process);
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
		mSurfaceHolder = arg0;
		mRtp = new Rtp();
		mRtp.openRtp(LOCAL_PORT);
		mRtp.setCallback(new IDataCallback() {

			@Override
			public void dataCallback(byte[] data, int size) {
				if(data.length == 35 && data[0]==35 && data[34]==35)
				{
					Log.e(TAG, "data bybe");
					mHandler.sendEmptyMessage(SHOW_PROCESS);
					
//					ScreenActivity.this.finish();
					return;
				}

				if (data.length == 30 && data[29] == 1&& data[28] == 0&& data[27] == 1&& data[26] == 0&& data[25] == 1&& data[24] == 0) {
					int w = (data[0] & 0xff) | ((data[1] & 0xff) << 8)
							| ((data[2] & 0xff) << 16)
							| ((data[3] & 0xff) << 24);
					int h = (data[4] & 0xff) | ((data[5] & 0xff) << 8)
							| ((data[6] & 0xff) << 16)
							| ((data[7] & 0xff) << 24);
					Log.e("dragon",
							String.format("dataCallback w:%d h:%d", w, h));

					mFrameWidth = w;
					mFrameHeight = h;

					if (mH264Android != null) {
						mH264Android.UninitDecoder();
						mH264Android = null;
					}

					mH264Android = new VView();
					mH264Android.InitDecoder(w, h);

					mPixel = new byte[mFrameWidth * mFrameHeight * 3];
					if (mBuffer != null) {
						mBuffer.clear();
						mBuffer = null;
					}
					mBuffer = ByteBuffer.wrap(mPixel);
					if (mVideoBit != null && !mVideoBit.isRecycled()) {
						mVideoBit.recycle();
						mVideoBit = null;
					}
					mVideoBit = Bitmap.createBitmap(w, h, Config.RGB_565);
					mHandler.sendEmptyMessage(DISMISS_ROCESS);

					mSurfaceW = mScreenW;
					mSurfaceH = (int) (mScreenW * (1.0f * mFrameHeight / mFrameWidth));
					if(mSurfaceH > mScreenH){
						mSurfaceW = (int) (mScreenH * (1.0f * mFrameWidth / mFrameHeight));
						mSurfaceH = mScreenH;
					}
					new Handler(Looper.getMainLooper())
							.post(new Runnable() {

								@Override
								public void run() {

									mSurfaceHolder.setFixedSize(mSurfaceW,mSurfaceH);
								}
							});
				} else {
					Log.e("dragon", "dataCallback data:" + data.length);
					Log.e("dragon", "dataCallback data[0]:" + data[0]);
					Log.e("dragon", "dataCallback data[1]:" + data[1]);
					Log.e("dragon", "dataCallback data[2]:" + data[2]);
					Log.e("dragon", "dataCallback data[3]:" + data[3]);

					if (mH264Android == null) {
						Log.v("dragon", "dataCallback mH264Android == null");
						return;
					}

					if (mSurfaceHolder != null) {
						SurfaceHolder sh = mSurfaceHolder;

						long time = System.currentTimeMillis();
						// decode nal
						int ret = mH264Android.DecoderNal(data, data.length,
								mPixel);
						Log.e("dragon",
								"frame show decode"
										+ (System.currentTimeMillis() - time));
						time = System.currentTimeMillis();
						if (ret >= 0) {
							mBuffer.mark();
							mVideoBit.copyPixelsFromBuffer(mBuffer);
							mBuffer.reset();

							Log.e("dragon",
									"frame show copy buffer "
											+ (System.currentTimeMillis() - time));
							time = System.currentTimeMillis();

							Canvas can = sh.lockCanvas();
							if (can != null) {
								can.save();
								can.drawBitmap(mVideoBit, null, new Rect(0, 0,
										mSurfaceW, mSurfaceH), null);
								can.restore();
							}
							sh.unlockCanvasAndPost(can);

							Log.e("dragon",
									"frame show draw "
											+ (System.currentTimeMillis() - time));
							time = System.currentTimeMillis();
						}
					}
				}
			}

			@Override
			public void dataCallback(short[] data, int size) {
			}
		});
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.e(TAG, "surfaceCreated");

		// 关闭rtp传输
		mRtp.closeRtp();
		mRtp.native_rease();
		mRtp = null;

		mSurfaceHolder = null;
	}
}