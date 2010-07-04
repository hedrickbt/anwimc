package com.anwim.client.android;

import com.anwim.client.android.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Settings extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);
	}

	public void onSaveClick(View theButton) {
		// new AlertDialog.Builder(this)
		// .setTitle("onSaveClick")
		// .setMessage("Save Clicked!")
		// .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dlg, int sumthin) {
		// finish();
		// }
		// })
		// .show();

		saveSettings();

	}

	private void saveSettings() {
		Editor editor = getSharedPreferences(Anwim.PREFS_NAME, 0).edit();

		TextView temp = (TextView) findViewById(R.id.usernameentry);
		editor.putString("username", temp.getText().toString());

		temp = (TextView) findViewById(R.id.passwordentry);
		editor.putString("password", temp.getText().toString());

		editor.commit();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadSettings();
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveSettings();
	}

	public void loadSettings() {
		SharedPreferences settings = getSharedPreferences(
				Anwim.PREFS_NAME, 0);
		// SharedPreferences settings =
		// this.getPreferences(Context.MODE_PRIVATE);

		TextView temp = (TextView) findViewById(R.id.usernameentry);
		temp.setText(settings.getString("username", ""));

		temp = (TextView) findViewById(R.id.passwordentry);
		temp.setText(settings.getString("password", ""));
	}

	public void onCancelClick(View theButton) {
		// new AlertDialog.Builder(this)
		// .setTitle("onCancelClick")
		// .setMessage("Cancel Clicked!")
		// .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dlg, int sumthin) {
		// finish();
		// }
		// })
		// .show();
		loadSettings();
		finish();

	}
}
