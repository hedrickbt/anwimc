package com.anwim.client.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class Settings extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		Spinner s = (Spinner) findViewById(R.id.authtypeentry);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.auth_type_array, android.R.layout.simple_spinner_dropdown_item);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
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

		TextView temp = (TextView) findViewById(R.id.anwimurientry);
		editor.putString("anwimuri", temp.getText().toString());

		temp = (TextView) findViewById(R.id.loginurientry);
		editor.putString("loginuri", temp.getText().toString());

		temp = (TextView) findViewById(R.id.logouturientry);
		editor.putString("logouturi", temp.getText().toString());

		Spinner temp2 = (Spinner) findViewById(R.id.authtypeentry);
		editor.putString("authtype", String.valueOf(temp2
				.getSelectedItemPosition()));

		temp = (TextView) findViewById(R.id.formusernamefieldentry);
		editor.putString("formusernamefield", temp.getText().toString());

		temp = (TextView) findViewById(R.id.formpasswordfieldentry);
		editor.putString("formpasswordfield", temp.getText().toString());

		temp = (TextView) findViewById(R.id.usernameentry);
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
		SharedPreferences settings = getSharedPreferences(Anwim.PREFS_NAME, 0);
		// SharedPreferences settings =
		// this.getPreferences(Context.MODE_PRIVATE);

		TextView temp = (TextView) findViewById(R.id.anwimurientry);
		temp.setText(settings.getString("anwimuri", ""));

		temp = (TextView) findViewById(R.id.loginurientry);
		temp.setText(settings.getString("loginuri", ""));

		temp = (TextView) findViewById(R.id.logouturientry);
		temp.setText(settings.getString("logouturi", ""));

		Spinner temp2 = (Spinner) findViewById(R.id.authtypeentry);
		temp2.setSelection(Integer
				.parseInt(settings.getString("authtype", "0")));

		temp = (TextView) findViewById(R.id.formusernamefieldentry);
		temp.setText(settings.getString("formusernamefield", ""));

		temp = (TextView) findViewById(R.id.formpasswordfieldentry);
		temp.setText(settings.getString("formpasswordfield", ""));

		temp = (TextView) findViewById(R.id.usernameentry);
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
