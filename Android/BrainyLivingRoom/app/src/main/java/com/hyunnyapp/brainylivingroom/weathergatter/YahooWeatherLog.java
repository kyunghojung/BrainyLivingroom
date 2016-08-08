package com.hyunnyapp.brainylivingroom.weathergatter;

import android.util.Log;

class YahooWeatherLog {
	
	public static final String TAG = YahooWeatherLog.class.getSimpleName();
	public static boolean isDebuggable = false;
	
	public static void setDebuggable(final boolean isDebuggable) {
	    YahooWeatherLog.isDebuggable = isDebuggable;
	}
	
	public static void d(final String tag, final String message) {
		if (!isDebuggable) return;
		Log.d(tag, message);
	}
	
	public static void d(final String message) {
		if (!isDebuggable) return;
		Log.d(TAG, message);
	}
	
	public static void printStack(final Exception e) {
	    if (!isDebuggable) return;
	    e.printStackTrace();
	}
	
}
