package com.android.remotecamera;

import h264.com.VView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.android.localcall.jni.Rtp;
import com.android.localcall.jni.Rtp.IDataCallback;
import com.android.remotecamera.CustomMediaRecorder.VideoInfor;
import com.example.remotecamera.R;

public class MainActivity extends BaseActivity implements Callback,
		PreviewCallback {

	byte[] mRGBbuffer = null;
	ByteBuffer mByteBuffer = null;
	Bitmap mBitmap = null;

	private SurfaceView mPreviewSurface = null;
	private Camera mCamera = null;
	private boolean mCameraOpen = false;

	private boolean mResizeSurface = false;
	private int mPreviewWidth = 0;
	private int mPreviewHeight = 0;
	private int mScreenW = 0;
	private int mScreenH = 0;
	private int mSurfaceW = 0;
	private int mSurfaceH = 0;

	private RadioButton mRadioConnect = null;
	private RadioButton mRadioCammera = null;
	private RadioGroup mRadioGroup = null;

	// 支持socket传输的MediaRecorder
	private CustomMediaRecorder mCustomMediaRecorder = null;

	private SurfaceHolder mSurfaceHolder = null;

	private VView mH264Android = null;

	private byte[] mPixel = null;
	private ByteBuffer mBuffer = null;
	private Bitmap mVideoBit = null;
	private EditText mEditText = null;
	// 传输数据
	private Rtp mRtp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		
		
		
		
		
		
		
		
		
		
		// 开启rtp传输
		mRtp = new Rtp();
		mRtp.openRtp(40018);
		mRtp.setCallback(new IDataCallback() {
			@Override
			public void dataCallback(short[] data, int size) {
				Log.e("dragon", "dataCallback data :" + data.length);
			}

			@Override
			public void dataCallback(byte[] data, int size) {

				if (data.length == 30) {
					int w = (data[0] & 0xff) | ((data[1] & 0xff) << 8)
							| ((data[2] & 0xff) << 16)
							| ((data[3] & 0xff) << 24);
					int h = (data[4] & 0xff) | ((data[5] & 0xff) << 8)
							| ((data[6] & 0xff) << 16)
							| ((data[7] & 0xff) << 24);
					Log.e("dragon",
							String.format("dataCallback w:%d h:%d", w, h));

					mPreviewWidth = w;
					mPreviewHeight = h;

					mResizeSurface = true;
					if (mH264Android != null) {
						mH264Android.UninitDecoder();
						mH264Android = null;
					}

					mH264Android = new VView();
					mH264Android.InitDecoder(w, h);

					mPixel = new byte[w * h * 3];
					mBuffer = ByteBuffer.wrap(mPixel);
					if (mVideoBit != null && !mVideoBit.isRecycled()) {
						mVideoBit.recycle();
						mVideoBit = null;
					}
					mVideoBit = Bitmap.createBitmap(w, h, Config.RGB_565);
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

					closeCamera();

					if (mSurfaceHolder != null) {
						SurfaceHolder sh = mSurfaceHolder;
						if (mResizeSurface) {
							mSurfaceW = mScreenW;
							mSurfaceH = (int) (mScreenW * (1.0f * mPreviewHeight / mPreviewWidth));
							if (mSurfaceH > mScreenH) {
								mSurfaceW = (int) (mScreenH * (1.0f * mPreviewWidth / mPreviewHeight));
								mSurfaceH = mScreenH;
							}
							new Handler(Looper.getMainLooper())
									.post(new Runnable() {

										@Override
										public void run() {

											mSurfaceHolder.setFixedSize(
													mSurfaceW, mSurfaceH);
										}

									});
							mResizeSurface = false;
						}
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
		});
	}

	@Override
	protected void onDestroy() {
		if (mH264Android != null) {
			mH264Android.UninitDecoder();
			mH264Android = null;
		}
		// 关闭rtp传输
		mRtp.closeRtp();
		mRtp.native_rease();
		mRtp = null;
		super.onDestroy();
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

	private void initView() {
		mEditText = (EditText) findViewById(R.id.editText1);
		mEditText.setText(restoreIp());
		mPreviewSurface = (SurfaceView) findViewById(R.id.previewsurface);
		mPreviewSurface.getHolder().addCallback(this);

		Display display = getWindowManager().getDefaultDisplay();
		mScreenW = display.getWidth();
		mScreenH = display.getHeight();

		mRadioGroup = (RadioGroup) findViewById(R.id.radiogroup);
		mRadioConnect = (RadioButton) findViewById(R.id.connectbutton);
		mRadioCammera = (RadioButton) findViewById(R.id.openbutton);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.connectbutton) {

				}

				if (checkedId == R.id.openbutton) {

					if (mCustomMediaRecorder != null) {
						mCustomMediaRecorder.stopRecorder();
						mCustomMediaRecorder = null;
					}

					String ip = mEditText.getText().toString();
					saveIp(ip);

					mRtp.addRtpDestinationIp(ip);

					mCustomMediaRecorder = new CustomMediaRecorder(ip);
					mCustomMediaRecorder.startRecorder(mCamera,
							mPreviewSurface, new VideoInfor(6000, 20,
									mPreviewWidth, mPreviewHeight));
				}
			}
		});
	}

	@SuppressLint("NewApi")
	private void openCamera(SurfaceHolder holder) {
		if (mCamera != null) {
			return;
		}
		if (mCameraOpen)
			return;

		mCamera = Camera.open();
		mCameraOpen = true;
		Parameters para = mCamera.getParameters();
		List<Size> list = para.getSupportedVideoSizes();
		Size size;
		int lenth = list.size();
		for (int i = 0; i < lenth - 4; i++) {
			size = list.get(i);
			mPreviewWidth = size.width;
			mPreviewHeight = size.height;
		}
		Log.v("dragon", "pre view mPreviewWidth:" + mPreviewWidth);
		Log.v("dragon", "pre view mPreviewHeight:" + mPreviewHeight);
		para.setPreviewSize(mPreviewWidth, mPreviewHeight);
		// para.setPreviewFrameRate(10);
		mCamera.setParameters(para);
		mCamera.setPreviewCallback(this);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.v(Utils.TAG, "e:" + e);
		}
		mCamera.setDisplayOrientation(90);
		mCamera.startPreview();
		mSurfaceW = mScreenW;
		mSurfaceH = (int) (mScreenW * (1.0f * mPreviewWidth / mPreviewHeight));
		if (mSurfaceH > mScreenH) {
			mSurfaceW = (int) (mScreenH * (1.0f * mPreviewHeight / mPreviewWidth));
			mSurfaceH = mScreenH;
		}
		holder.setFixedSize(mSurfaceW, mSurfaceH);
	}

	private void closeCamera() {
		if (!mCameraOpen)
			return;
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
		mCameraOpen = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (Utils.DEBUG) {
			Log.v(Utils.TAG, "surfaceChanged");
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (Utils.DEBUG) {
			Log.v(Utils.TAG, "surfaceCreated");
		}
		mSurfaceHolder = holder;
		openCamera(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (Utils.DEBUG) {
			Log.v(Utils.TAG, "surfaceDestroyed");
		}

		if (mCustomMediaRecorder != null) {
			mCustomMediaRecorder.stopRecorder();
			mCustomMediaRecorder = null;
		}
		closeCamera();
	}

	@Override
	public void onPreviewFrame(byte[] arg0, Camera arg1) {

	}
}
