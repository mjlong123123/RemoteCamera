package com.android.remotecamera;

import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.android.localcall.jni.Rtp;
import com.android.remotecamera.CustomObjectPool.Entity;

public class DataCacheThread extends Thread {
	private static final String TAG = "DataCacheThread";

	private CustomObjectPool mCustomObjectPool = new CustomObjectPool(
			1 * 1000 * 1000);

	private LinkedBlockingQueue<Entity> mLinkedBuffer = new LinkedBlockingQueue<Entity>(50);

	private boolean isLostFrame = false;

	private Rtp mRtp = null;

	private Statistics sta = new Statistics();

	private long mLastTime = 0;
	
	private Object mLock = new Object();
	
	private String mIP;

	public DataCacheThread(String ip) {
		super(TAG);
		mIP = ip;
	}
	
	@Override
	public void run() {
		Entity en = null;
		long oldtime = System.currentTimeMillis();
		long taketime = System.currentTimeMillis();
		
		mRtp = new Rtp();
		mRtp.openRtp(40018);
		mRtp.addRtpDestinationIp(mIP);
		
		try {
			while (!interrupted()) {

				if (Utils.DEBUG) {
					Log.e(TAG, "run time:"+(System.currentTimeMillis() - oldtime));
					oldtime = System.currentTimeMillis();
				}
				taketime = System.currentTimeMillis();
				en = mLinkedBuffer.take();
				if (Utils.DEBUG) {
					Log.e(TAG, "run take time:"+(System.currentTimeMillis() - taketime));
				}
				mRtp.write(en.getBuffer(), en.size());
				if(en.size() == 35){
					if(en.getBuffer()[34] == 35 && en.getBuffer()[0] == 35)
						break;
				}
				mCustomObjectPool.returnBuf(en);
				synchronized (mLock) {
					if (Utils.DEBUG) {
						Log.e(TAG, "run wait start");
					}
					mLock.wait(en.getSleepTime());
					if (Utils.DEBUG) {
						Log.e(TAG, "run wait end");
					}
				}
			}
		} catch (InterruptedException e) {
			if (Utils.DEBUG) {
				Log.e(TAG, "linkedbuffer take error:" + e);
			}
		} catch(RuntimeException e){

			if (Utils.DEBUG) {
				Log.e(TAG, "rtp close error:" + e);
			}
		}


		mRtp.closeRtp();
		mRtp.native_rease();
		mRtp = null;
		
		if (Utils.DEBUG) {
			Log.e(TAG, "run over");
		}
	}

	/**
	 * add data to buffer pool
	 * 
	 * @param e
	 */
	public void add(Entity e) {
		// lost frame
		if (isLostFrame) {
			// get frame type,type == 1 is key frame.send key frame begin.
			int type = (e.getBuffer()[4] & 0x1f);

			if (Utils.DEBUG) {
				Log.e(TAG, "add type:" + type);
			}

			if (type != 1) {
				return;
			} else {
				isLostFrame = false;
			}
		}

		if(mLinkedBuffer.size() > 30){
			if (Utils.DEBUG) {
				Log.e(TAG, "add buffer size > 30. notify");
			}
			synchronized (mLock) {
				mLock.notify();
			}
		}
		boolean ret = mLinkedBuffer.offer(e);
		// set lost frame flag.
		if (!ret) {
			isLostFrame = true;
			if (Utils.DEBUG) {
				Log.e(TAG, "add buffer is full.");
			}
		}
	}

	/**
	 * get buffer from buffer pool.
	 * 
	 * @param len
	 * @return
	 */
	public Entity getEmptyEntity(int len) {

		if (mLastTime == 0) {
			mLastTime = System.nanoTime();
		}
		
		long currentTime = System.nanoTime();

		sta.push(currentTime - mLastTime);
		
		mLastTime = currentTime;
		
		Entity en = (Entity) mCustomObjectPool.getBuf(len);
		
		en.setSleepTime(sta.average() / 1000000);

		return en;
	}

	/** Used in packetizers to estimate timestamps in RTP packets. */
	protected static class Statistics {

		public final static String TAG = "Statistics";

		private int count = 700, c = 0;
		private float m = 0, q = 0;
		private long elapsed = 0;
		private long start = 0;
		private long duration = 0;
		private long period = 10000000000L;
		private boolean initoffset = false;

		public Statistics() {
		}

		public Statistics(int count, int period) {
			this.count = count;
			this.period = period;
		}

		public void reset() {
			initoffset = false;
			q = 0;
			m = 0;
			c = 0;
			elapsed = 0;
			start = 0;
			duration = 0;
		}

		public void push(long value) {
			elapsed += value;
			if (elapsed > period) {
				elapsed = 0;
				long now = System.nanoTime();
				if (!initoffset || (now - start < 0)) {
					start = now;
					duration = 0;
					initoffset = true;
				}
				// Prevents drifting issues by comparing the real duration of
				// the
				// stream with the sum of all temporal lengths of RTP packets.
				value += (now - start) - duration;
				// Log.d(TAG,
				// "sum1: "+duration/1000000+" sum2: "+(now-start)/1000000+" drift: "+((now-start)-duration)/1000000+" v: "+value/1000000);
			}
			if (c < 5) {
				// We ignore the first 20 measured values because they may not
				// be accurate
				c++;
				m = value;
			} else {
				m = (m * q + value) / (q + 1);
				if (q < count)
					q++;
			}
		}

		public long average() {
			long l = (long) m;
			duration += l;
			return l;
		}

	}
}
