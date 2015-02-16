package com.android.remotecamera;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

/**
 * @author dragon 解析测试数据使用
 */
public class ParseThread extends Thread {
	private final static String TAG = "ParseThread";
	/**
	 * 解析数据完成回调
	 */
	private FinishCallback mFinishCallback = null;
	/**
	 * 测试文件
	 */
	private File mInFile = null;

	/**
	 * @param in 视频文件流
	 * @param callback 解析文件格式回调
	 */
	public ParseThread(File in, FinishCallback callback) {
		super("ParseThread");
		mFinishCallback = callback;
		mInFile = in;
	}

	@Override
	public void run() {
		RandomAccessFile raf = null;
		byte[] pps = null;
		byte[] sps = null;
		try {
			raf = new RandomAccessFile(mInFile, "r");
			// avcc box. a v c C
			final byte[] avcC = { 0x61, 0x76, 0x63, 0x43 };
			byte[] buffer = new byte[4];
			raf.readFully(buffer);
			// 搜索avcC
			while (!Thread.interrupted()
					&& !(buffer[0] == avcC[0] && buffer[1] == avcC[1]
							&& buffer[2] == avcC[2] && buffer[3] == avcC[3])) {
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];
				buffer[2] = buffer[3];
				buffer[3] = raf.readByte();
			}
			// 跳过无用数据
			raf.skipBytes(7);
			// 读取sps长度
			int spsLen = raf.readByte();

			if (Utils.DEBUG)
				Log.v(TAG, "find avcc spsLen:" + spsLen);

			sps = new byte[spsLen];
			raf.readFully(sps);
			// 跳过无用数据
			raf.skipBytes(2);
			// 读取pps长度
			int ppsLen = raf.readByte();

			if (Utils.DEBUG)
				Log.v(TAG, "find avcc ppsLen:" + ppsLen);

			pps = new byte[ppsLen];
			raf.readFully(pps);

		} catch (Exception e) {
			Log.e(TAG, "RandomAccessFile e:" + e);
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				Log.e(TAG, "RandomAccessFile close e:" + e);
			}
		}

		if (Utils.DEBUG) {
			for (byte b : sps) {
				Log.v(TAG, String.format("sps %x", b));
			}

			for (byte b : pps) {
				Log.v(TAG, String.format("pps %x", b));
			}
		}

		if (mFinishCallback != null) {
			mFinishCallback.onCallback(sps, pps);
		}
	}

	/**
	 * @author dragon
	 * 解析sps和pps数据回调
	 */
	public static interface FinishCallback {
		void onCallback(byte[] sps, byte[] pps);
	}
}
