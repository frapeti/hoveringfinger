package com.frapeti.fingerhovering;

import java.io.File;

import com.google.analytics.tracking.android.Log;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

public class DataBaseHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private String DB_PATH = Environment.getDownloadCacheDirectory()
			.getAbsolutePath() + File.pathSeparator;

	private static String DB_NAME = "settings.db";

	private SQLiteDatabase myDataBase;
	private Context context;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resourceºs.
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.context = context;
		DB_PATH = context.getExternalCacheDir().getAbsolutePath() + "/";
	}

	/*
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 */

	public void openDataBase() {

		try {
			// Open the database
			String myPath = DB_PATH + DB_NAME;
			myDataBase = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
		} catch (Exception e) {
			Log.e(e);
			Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void togglePointer(boolean isChecked) {

		MainActivity.sup
				.runCommand("echo `cp -rf /data/data/com.android.providers.settings/databases/settings.db "
						+ DB_PATH + DB_NAME + "`");

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		openDataBase();
		int value = 0;
		if (isChecked) {
			value = 1;
		}
		String sql = "UPDATE system SET value = " + value
				+ " WHERE name = \"finger_air_view_pointer\"";
		myDataBase.execSQL(sql);
		close();
		MainActivity.sup
				.runCommand("echo `cp -rf "
						+ DB_PATH
						+ DB_NAME
						+ " /data/data/com.android.providers.settings/databases/settings.db`");
		MainActivity.sup
				.runCommand("echo `chmod 660 /data/data/com.android.providers.settings/databases/settings.db`");
		MainActivity.sup
				.runCommand("echo `chown system:system /data/data/com.android.providers.settings/databases/settings.db`");
		MainActivity.sup.runCommand("echo `rm " + DB_PATH + DB_NAME + "`");
	}
}