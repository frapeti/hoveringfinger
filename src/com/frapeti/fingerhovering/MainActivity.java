package com.frapeti.fingerhovering;

import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Log;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity {

	public static final String CMD_FS_PATH = "/sys/class/sec/tsp/cmd";
	public static final String RESULT_FS_PATH = "/sys/class/sec/tsp/cmd_result";
	public static final String ENABLE_ON_BOOT = "enable_on_boot";
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
	private static final int AD_ID = generateViewId();
	private DataBaseHelper dbHelper;

	public static SuperUserProcess sup;

	TextView test_zone;
	Button visit_thread;
	ToggleButton hovering_toggle, pointer_toggle;
	CheckBox boot_cb;
	private String PREF_POINTER = "pref_pointer";
	private AdView adView;
	private InterstitialAd interstitial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeComponents();

		loadBanner();
		loadInterstitial();

	}

	private void loadInterstitial() {

		// Create ad request.
		AdRequest adRequest = new AdRequest.Builder().build();

		// Begin loading your interstitial.
		interstitial.loadAd(adRequest);

	}

	// Invoke displayInterstitial() when you are ready to display an
	// interstitial.
	public void displayInterstitial() {
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
	}

	@SuppressLint("NewApi")
	public static int generateViewId() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return View.generateViewId();

		} else {
			for (;;) {
				final int result = sNextGeneratedId.get();
				int newValue = result + 1;
				if (newValue > 0x00FFFFFF) {
					newValue = 1;
				}
				if (sNextGeneratedId.compareAndSet(result, newValue)) {
					return result;
				}
			}
		}

	}

	private void loadBanner() {

		FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);

		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-4795721323288776/8433735644");
		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setId(AD_ID);

		FrameLayout.LayoutParams params = new LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);

		adView.setLayoutParams(params);

		adView.loadAd(new AdRequest.Builder().build());

		adFrame.addView(adView);

	}

	private void initializeComponents() {

		// Create the interstitial.
		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId("ca-app-pub-4795721323288776/9473256043");

		interstitial.setAdListener(new AdListener() {

			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				displayInterstitial();
			}
		});

		sup = new SuperUserProcess();
		dbHelper = new DataBaseHelper(this);

		test_zone = (TextView) findViewById(R.id.test_zone);
		visit_thread = (Button) findViewById(R.id.visit_thread_button);
		visit_thread.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri url = Uri
						.parse("http://forum.xda-developers.com/showthread.php?t=2705514");
				Intent i = new Intent(Intent.ACTION_VIEW, url);
				startActivity(i);
			}
		});
		hovering_toggle = (ToggleButton) findViewById(R.id.hovering_toggle);
		pointer_toggle = (ToggleButton) findViewById(R.id.pointer_toggle);
		boot_cb = (CheckBox) findViewById(R.id.cb_boot);
		boot_cb.setChecked(PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(ENABLE_ON_BOOT, false));
		boot_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Editor editor = PreferenceManager.getDefaultSharedPreferences(
						MainActivity.this).edit();
				editor.putBoolean(ENABLE_ON_BOOT, isChecked);
				editor.commit();
			}
		});

		test_zone.setOnHoverListener(new OnHoverListener() {

			@Override
			public boolean onHover(View v, MotionEvent event) {

				new LongOperation().execute(new String[0]);
				return true;
			}
		});

		hovering_toggle.setChecked(getHoveringStatus());

		hovering_toggle
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						toggleHovering(isChecked);
					}
				});

		pointer_toggle.setChecked(PreferenceManager
				.getDefaultSharedPreferences(this).getBoolean(PREF_POINTER,
						false));

		pointer_toggle
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						togglePointer(isChecked);
					}
				});
	}

	protected void togglePointer(boolean isChecked) {
		if (sup.runCommand("getprop").contains("knox")) {
			dbHelper.togglePointer(isChecked);
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			editor.putBoolean(PREF_POINTER, isChecked);
			editor.commit();

			Toast.makeText(MainActivity.this, "Reboot to apply...",
					Toast.LENGTH_SHORT).show();

		}

		else {
			Toast.makeText(this,
					"This option is enabled for TouchWiz ROMs only!",
					Toast.LENGTH_SHORT).show();
		}

	}

	private boolean getHoveringStatus() {

		final String OFF = "0x82";
		final String ON = "0x03";

		String result = sup.runCommand("cat " + RESULT_FS_PATH);

		Boolean state = false;

		if (result.contains(ON)) {
			state = true;
		}

		if (result.contains(OFF)) {
			state = false;
		}

		return state;
	}

	public static void toggleHovering(boolean isChecked) {

		final String OFF = "0";
		final String ON = "1";

		String value = OFF;

		if (isChecked) {
			value = ON;
		}

		if (sup == null) {
			sup = new SuperUserProcess();
		}
		sup.runCommand("echo hover_enable," + value + " > " + CMD_FS_PATH);
	}

	private class LongOperation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			test_zone.setTextColor(getResources().getColor(
					android.R.color.holo_blue_light));
			test_zone.setText("Not hovering here :(");
		}

		@Override
		protected void onPreExecute() {
			test_zone.setTextColor(getResources().getColor(
					android.R.color.holo_green_light));
			test_zone.setText("Hovering here :)");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}

	@Override
	public void onPause() {
		displayInterstitial();
		super.onPause();
		adView.pause();
	}

	public void onResume() {
		displayInterstitial();
		super.onResume();
		adView.resume();

		int google_play_state = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		switch (google_play_state) {
		case ConnectionResult.SERVICE_MISSING:
		case ConnectionResult.SERVICE_INVALID:
		case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED: {

			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
					google_play_state, this, 0);
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();

				}
			});
			dialog.show();
		}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}
}
