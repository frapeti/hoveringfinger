package com.frapeti.fingerhovering;

import com.google.analytics.tracking.android.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
	SharedPreferences prefs;
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {

		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		MainActivity.toggleHovering((prefs.getBoolean(
				MainActivity.ENABLE_ON_BOOT, false)));

		Log.i("Enabled Hovering Finger feature: "
				+ prefs.getBoolean(MainActivity.ENABLE_ON_BOOT, false));
	}
}
