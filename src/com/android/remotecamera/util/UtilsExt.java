package com.android.remotecamera.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;
import android.util.Log;

public class UtilsExt {
	static private final String TAG = "Utils";
	// ip格式控制
	static private Pattern mPattern = Pattern
			.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");

	/**
	 * 判断是否是ip地址
	 * 
	 * @param ip
	 * @return
	 */
	static public boolean isIpAddress(String ip) {
		boolean ret = false;
		if (ip != null) {
			if (mPattern.matcher(ip).find()) {
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * 取得本机ip
	 * 
	 * @return
	 */
	static public String getLocalIpAddress(Context context) {
		String ret = null;
		try {
			String gate = getGateWay(context);
			String[] locals = null;
			String local = null;
			if (gate != null) {
				locals = gate.split("\\.");
				if (locals != null && locals.length > 0 && locals[0] != null
						&& !locals[0].equals("")) {
					local = locals[0];
				}
			}

			Log.v(TAG, "getLocalIpAddress local:" + local);
			Log.v(TAG, "getLocalIpAddress gate:" + gate);

			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();

				Log.v(TAG, "getLocalIpAddress intf:" + intf);
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						ret = inetAddress.getHostAddress().toString();

						Log.v(TAG, "getLocalIpAddress ret:" + ret);

						if (ret != null && mPattern.matcher(ret).find()) {
							if (local != null && ret.contains(local)) {
								return ret;
							}
						}
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}

	/**
	 * 获取网关
	 * 
	 * @param context
	 * @return
	 */
	static public String getGateWay(Context context) {
		String ret = "";
		if (context == null) {
			Log.v(TAG, "getGateWay context == null");
			return ret;
		}
		WifiManager wifi_service = (WifiManager) context
				.getSystemService(Service.WIFI_SERVICE);
		if (wifi_service == null) {
			Log.v(TAG, "getGateWay wifi_service == null");
			return ret;
		}
		DhcpInfo dhcpinfo = wifi_service.getDhcpInfo();
		if (dhcpinfo == null) {
			Log.v(TAG, "getGateWay dhcpinfo == null");
			return ret;
		}
		return IntegerToString(dhcpinfo.gateway);
	}

	/**
	 * ip地址转换
	 * 
	 * @param in
	 * @return
	 */
	static private String IntegerToString(int in) {
		String ret = "";
		ret += String.valueOf(in & 0xff);
		ret += ".";
		ret += String.valueOf((in >> 8) & 0xff);
		ret += ".";
		ret += String.valueOf((in >> 16) & 0xff);
		ret += ".";
		ret += String.valueOf((in >> 24) & 0xff);
		return ret;
	}

	public final static int getNHostAddresses() {
		int nHostAddrs = 0;
		try {
			Enumeration nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = (InetAddress) addrs.nextElement();
					if (isUsableAddress(addr) == false)
						continue;
					nHostAddrs++;
				}
			}
		} catch (Exception e) {
			Log.v(TAG, "e:" + e);
		}
		return nHostAddrs;
	}

	public final static String getHostAddress(int n) {
		int hostAddrCnt = 0;
		try {
			Enumeration nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = (InetAddress) addrs.nextElement();
					if (isUsableAddress(addr) == false)
						continue;
					if (hostAddrCnt < n) {
						hostAddrCnt++;
						continue;
					}
					String host = addr.getHostAddress();
					// if (addr instanceof Inet6Address)
					// host = "[" + host + "]";
					Log.v(TAG, "111 host：" + host);
					return host;
				}
			}
		} catch (Exception e) {
			Log.v(TAG, "111 e:" + e);
		}
		return "";
	}

	private final static boolean isUsableAddress(InetAddress addr) {
		if (addr.isLoopbackAddress() == true)
			return false;
		return true;
	}

}
