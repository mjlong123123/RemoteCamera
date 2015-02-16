package com.android.remotecamera;

import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceView;

import com.android.localcall.jni.Rtp;
import com.android.remotecamera.ParseThread.FinishCallback;

/**
 * @author dragon 支持网络传输的MediaRecorder
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class CustomMediaRecorder {
	private static final String TAG = "CustomMediaRecorder";

	/**
	 * 系统Recorder
	 */
	private MediaRecorder mMediaRecorder = null;

	/**
	 * 系统相机
	 */
	private Camera mCamera = null;

	/**
	 * 显示用surfaceview
	 */
	private SurfaceView mSurfaceView = null;

	/**
	 * 视频信息设置
	 */
	private VideoInfor mVideoInfor = null;

	/**
	 * 编码信息解析测试文件
	 */
	private final File TESTFILE = new File(
			Environment.getExternalStorageDirectory(), "h264.h264");
	/**
	 * 测试文件时长
	 */
	private final int TESTTIME = 2000;

	/**
	 * Recorder数据流读取使用，fdPair[1]用于写入数据,fdPair[0]用于读取数据
	 */
	ParcelFileDescriptor[] fdPair = null;

	/**
	 * 解析测试文件用线程
	 */
	private ParseThread mParseThread = null;

	/**
	 * 保存解析测试文件取得的sps信息
	 */
	private byte[] mSPS = null;
	/**
	 * 保存解析测试文件取得的pps信息
	 */
	private byte[] mPPS = null;

	/**
	 * 解析实时视频流
	 */
	private FormatReadThread mFormatReadThread = null;

	/**
	 * 发送rtp数据用
	 */
	private Rtp mRtp;

	public CustomMediaRecorder(Rtp rtp) {
		mRtp = rtp;
	}

	/**
	 * 开始录制
	 * 
	 * @param camera
	 *            录制使用的camera
	 * @param sv
	 *            显示预览的surface view
	 * @param infor
	 *            录制视频信息设置
	 */
	public void startRecorder(Camera camera, SurfaceView sv, VideoInfor infor) {
		if (Utils.DEBUG)
			Log.d(TAG, "CustomMediaRecorder: startRecorder");

		mCamera = camera;
		mSurfaceView = sv;
		mVideoInfor = infor;
		initMediaRecoreder(true);
	}

	/**
	 * 停止录制
	 */
	public void stopRecorder() {

		if (Utils.DEBUG)
			Log.d(TAG, "CustomMediaRecorder: stopRecorder");

		if (mParseThread != null && !mParseThread.isInterrupted()) {
			mParseThread.interrupt();
			mParseThread = null;
		}

		if (mFormatReadThread != null && !mFormatReadThread.isInterrupted()) {
			mFormatReadThread.interrupt();
			mFormatReadThread = null;
		}
		if (mMediaRecorder != null) {
			try {
				mMediaRecorder.stop();
				mMediaRecorder.release();
			} catch (Exception e) {
				Log.e(TAG, "CustomMediaRecorder stop e:" + e);
			}
			mMediaRecorder = null;
		}
		if (fdPair != null) {
			try {
				fdPair[0].close();
			} catch (IOException e) {
				Log.e(TAG, "fdPair close e:" + e);
			}
			try {
				fdPair[1].close();
			} catch (IOException e) {
				Log.e(TAG, "fdPair close e:" + e);
			}
			fdPair = null;
		}
	}

	/**
	 * 初始化MediaRecorder
	 * 
	 * @param isTest
	 *            true：初始化测试文件生成
	 * @return
	 */
	private boolean initMediaRecoreder(boolean isTest) {
		boolean ret = true;
		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
		mMediaRecorder.setVideoSize(mVideoInfor.getW(), mVideoInfor.getH());
		
//		mMediaRecorder.setVideoFrameRate(mVideoInfor.getVideoFrameRate());
//		mMediaRecorder.setVideoEncodingBitRate(4000000);
		if (isTest) {
			if (TESTFILE.exists())
				TESTFILE.delete();
			mMediaRecorder.setOutputFile(TESTFILE.getAbsolutePath());
			mMediaRecorder.setMaxDuration(TESTTIME);
			mMediaRecorder.setOnInfoListener(new OnInfoListener() {

				@Override
				public void onInfo(MediaRecorder arg0, int what, int arg2) {
					if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {

						if (Utils.DEBUG)
							Log.d(TAG, "MediaRecorder: MAX_DURATION_REACHED");

						stopRecorder();

						if (mParseThread != null
								&& !mParseThread.isInterrupted()) {
							mParseThread.interrupt();
							mParseThread = null;
						}
						mParseThread = new ParseThread(TESTFILE,
								new FinishCallback() {

									@Override
									public void onCallback(byte[] sps,
											byte[] pps) {
										if (sps == null || pps == null) {
											Log.e(TAG,
													"Parse sps == null || pps == null");
											return;
										}
										mSPS = sps;
										mPPS = pps;
										initMediaRecoreder(false);
									}
								});
						mParseThread.start();
					}
				}
			});
		} else {
			try {
				fdPair = ParcelFileDescriptor.createPipe();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mMediaRecorder.setMaxDuration(0);
			mMediaRecorder.setMaxFileSize(0);
			mMediaRecorder.setOutputFile(fdPair[1].getFileDescriptor());
			startSocket();
		}
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			if (Utils.DEBUG)
				Log.d(TAG, "prepare state error:" + e);
		} catch (IOException e) {
			if (Utils.DEBUG)
				Log.d(TAG, "prepare io error:");
		}
		mMediaRecorder.start();
		return ret;
	}

	/**
	 * 启动传输socket
	 */
	private void startSocket() {
		if (mFormatReadThread != null && !mFormatReadThread.isInterrupted()) {
			mFormatReadThread.interrupt();
			mFormatReadThread = null;
		}

		mFormatReadThread = new FormatReadThread(
				new ParcelFileDescriptor.AutoCloseInputStream(fdPair[0]), mSPS,
				mPPS, mRtp, mVideoInfor.getW(), mVideoInfor.getH());
		mFormatReadThread.start();
	}

	
	
	
	/**
	 * @author dragon 录制视频信息
	 */
	public static class VideoInfor {
		private int mVideoWidth = 0;
		private int mVideoHeight = 0;
		private int mVideoFrameRate = 20;
		private int mPort = 5000;

		public VideoInfor(int port, int videorate, int w, int h) {
			mPort = port;
			mVideoFrameRate = videorate;
			mVideoWidth = w;
			mVideoHeight = h;
		}

		public int getW() {
			return mVideoWidth;
		}

		public int getH() {
			return mVideoHeight;
		}

		public int getPort() {
			return mPort;
		}

		public int getVideoFrameRate() {
			return mVideoFrameRate;
		}
	}
}
