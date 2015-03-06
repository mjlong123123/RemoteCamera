/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.remotecamera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import android.util.Log;

/**
 * copy bytearraypool
 */
public class CustomObjectPool {
	private static final String TAG = "CustomObjectPool";

	private LinkedList<Entity> mBuffersByLastUse = new LinkedList<Entity>();

	private ArrayList<Entity> mBuffersBySize = new ArrayList<Entity>(64);

	private int mCurrentSize = 0;

	private final int mSizeLimit;

	protected static final Comparator<Entity> BUF_COMPARATOR = new Comparator<Entity>() {
		@Override
		public int compare(Entity lhs, Entity rhs) {
			return lhs.size() - rhs.size();
		}
	};

	public CustomObjectPool(int sizeLimit) {
		mSizeLimit = sizeLimit;
	}

	public synchronized Entity getBuf(int len) {
//		for (int i = 0; i < mBuffersBySize.size(); i++) {
//			Entity buf = mBuffersBySize.get(i);
//			if (buf.size() >= len) {
//				mCurrentSize -= buf.size();
//				mBuffersBySize.remove(i);
//				mBuffersByLastUse.remove(buf);
//				return buf;
//			}
//		}
		return new Entity(len);
	}

	/**
	 * Returns a buffer to the pool, throwing away old buffers if the pool would
	 * exceed its allotted size.
	 * 
	 * @param buf
	 *            the buffer to return to the pool.
	 */
	public synchronized void returnBuf(Entity buf) {
//		if (buf == null || buf.size() > mSizeLimit) {
//			return;
//		}
//		mBuffersByLastUse.add(buf);
//		int pos = Collections.binarySearch(mBuffersBySize, buf, BUF_COMPARATOR);
//		if (pos < 0) {
//			pos = -pos - 1;
//		}
//		mBuffersBySize.add(pos, buf);
//		mCurrentSize += buf.size();
//		trim();
	}

	/**
	 * Removes buffers from the pool until it is under its size limit.
	 */
	private synchronized void trim() {
		while (mCurrentSize > mSizeLimit) {

			if (Utils.DEBUG) {
				Log.e(TAG, "trim mCurrentSize:" + mCurrentSize);
				Log.e(TAG, "trim mSizeLimit:" + mSizeLimit);
			}

			Entity buf = mBuffersByLastUse.remove(0);
			mBuffersBySize.remove(buf);
			mCurrentSize -= buf.size();
		}
	}

	public static class Entity {
		private static final String TAG = "Entity";
		private static final int SLEEP_TIME_DEFAULT = 5;
		private static final int SLEEP_TIME_MAX = 100;

		private int mSize = 0;

		private byte[] mBuffer = null;

		private long mSleepTime = SLEEP_TIME_DEFAULT;

		protected Entity(int size) {
			mSize = size;
			mBuffer = new byte[size];

			if (Utils.DEBUG) {
				Log.e(TAG, "Entity new size:" + size);
			}
		}

		public void setSleepTime(long time) {

			if (Utils.DEBUG) {
				Log.e(TAG, "Entity setSleepTime time:" + time);
			}

			if (time < SLEEP_TIME_DEFAULT || time > SLEEP_TIME_MAX) {
				return;
			}
			mSleepTime = time - SLEEP_TIME_DEFAULT;
		}

		public long getSleepTime() {
			if (Utils.DEBUG) {
				Log.e(TAG, "Entity setSleepTime getSleepTime:" + mSleepTime);
			}
			return mSleepTime;
		}

		public int size() {
			return mSize;
		}

		public byte[] getBuffer() {
			return mBuffer;
		}
	}
}
