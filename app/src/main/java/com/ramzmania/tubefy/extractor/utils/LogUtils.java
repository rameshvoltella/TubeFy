package com.ramzmania.tubefy.extractor.utils;

import android.util.Log;

import com.github.kotvertolet.youtubejextractor.BuildConfig;


public class LogUtils
{
	public static void log(String x){
		if(BuildConfig.DEBUG)
		Log.i("Naveed",x);
	}
	public static void log(int x){
		if(BuildConfig.DEBUG)
			Log.i("Naveed",String.valueOf(x));
	}
	
}
