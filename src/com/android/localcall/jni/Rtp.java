package com.android.localcall.jni;

import java.lang.ref.WeakReference;

public class Rtp
{
	private final String TAG = "Rtp";
	
	private int mJavaRtp = 0;//保存本地对象

	private static IDataCallback mIDataCallback = null;
	
	//加载本地库
	static 
	{
		try
		{
			System.loadLibrary("rtp-jni");
			native_init();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 构建Rtp对象
	 */
	public Rtp()
	{
		native_setup(new WeakReference<Rtp>(this));
	}
	
	/**
	 * 本地代码数据回调
	 */
	static private void callBackFromNative(short[] buffer, int size)
	{
		if(mIDataCallback != null)
		{
			mIDataCallback.dataCallback(buffer, size);
		}
	}
	
	/**
	 * 本地代码数据回调
	 */
	static private void callBackFromNative1(byte[] buffer, int size)
	{
		if(mIDataCallback != null)
		{
			mIDataCallback.dataCallback(buffer, size);
		}
	}
	
	/**
	 * 设置接收数据的回调
	 * @param callback
	 */
	public void setCallback(IDataCallback callback)
	{
		mIDataCallback = callback;
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		native_rease();
		super.finalize();
	}

	/**
	 * 初始化本地代码信息，保存Rtp类信息到本地。加载lib库时调用。
	 */
	public native static void native_init();
	
	/**
	 * 设置Rtp对象到native保存
	 */
	public native void native_setup(Object rtp);
	/**
	 * 释放native资源
	 */
	public native void native_rease();
	/**
	 * 打开rtp会话
	 */
	public native boolean openRtp(int localport);
	/**
	 * 关闭rtp会话
	 */
	public native void closeRtp();
	/**
	 * 添加接收地址
	 * @param ip
	 */
	public native void addRtpDestinationIp(String ip);
	/**
	 * 删除接收地址
	 * @param ip
	 */
	public native void delRtpDestinationIp(String ip);
	/**
	 * 返回audio数据帧大小
	 * @return
	 */
	public native int getFrameSize();
	/**
	 * 写数据，非阻塞状态。
	 * 长度是data.lenth
	 * @param data 数据
	 */
	public native void write(short [] data);
	public native void write(byte [] data, boolean isAll, int lenth);
	public void write(byte [] data,int lenth){
		write(data,true,lenth);
	}
	/**
	 * 写数据，非阻塞状态。
	 * @param data 数据
	 * @param lenth 数据长度
	 */
	public native void write(short [] data,int lenth);
	

	public native int capture(short lin[], int size);
	public native int playback(short lin[], int size);
	
	
	public interface IDataCallback
	{
		void dataCallback(short [] data, int size);
		void dataCallback(byte [] data, int size);
	}
}
