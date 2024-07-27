package com.ramzmania.tubefy.extractor.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

public class ContextUtils 
{
	public static Context context;
	public static void init(Context c){
		context=c;
	}
	public static  void CopytoClip(String x){

        ((ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", x));    
		Log.i("YtLib", "Copied");
	}
	
}
