package com.frapeti.fingerhovering;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class SuperUserProcess {
	Process p;
	DataOutputStream stdin;
	InputStream stdout, stderr;
	final int BUFF_LEN = 4096;
	String out;
	int read;
	byte[] buffer;
	final String TAG = "SuperUserProcess";

	public SuperUserProcess() {

		try {
			p = Runtime.getRuntime().exec(
					new String[] { "su", "-c", "system/bin/sh" });
		} catch (IOException e) {
			Log.w(TAG, "ERROR: (Running ROOT shell)");
			e.printStackTrace();
		}

		stdin = new DataOutputStream(p.getOutputStream());

		stdout = p.getInputStream();
		stderr = p.getErrorStream();

		buffer = new byte[BUFF_LEN];

		out = new String();

	}

	public String runCommand(String cmd) {
		try {

			stdin.writeBytes(cmd + "\n"); // \n executes the command

			// read method will wait forever if there is nothing in the stream
			// so we need to read it in another way than
			// while((read=stdout.read(buffer))>0)
			
			if (cmd.startsWith("echo")) {
				return "";
			}

			out = "";

			while (true) {

				read = stdout.read(buffer);
				out += new String(buffer, 0, read);
				if (read < BUFF_LEN) {
					// we have read everything
					break;
				}
			}

		} catch (IOException e) {
			Log.w(TAG, "ERROR: (Running normal command)");
			e.printStackTrace();
		}

		return out;
	}
}
