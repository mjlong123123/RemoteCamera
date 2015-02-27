package com.android.remotecamera;

import android.app.Activity;

import com.android.remotecamera.util.BaiduStatisticController;

public class BaseActivity extends Activity
{

	@Override
	protected void onPause()
	{
		BaiduStatisticController.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		BaiduStatisticController.onResume(this);
		super.onResume();
	}

}
