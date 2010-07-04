package com.anwim.client.android;

import com.anwim.client.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class Anwim extends Activity {
	public static final String PREFS_NAME = "AndroidWindowsManagementPrefs";

	public static final int MENU_SETTINGS = Menu.FIRST + 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void onServicesClick(View theButton) {
		startActivity(new Intent(this,
				com.anwim.client.android.Servers.class));
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem temp = menu.add(0, MENU_SETTINGS, 0, "Settings");
		temp.setIcon(R.drawable.ic_menu_preferences);
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			startActivity(new Intent(this,
					com.anwim.client.android.Settings.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
