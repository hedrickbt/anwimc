package com.anwim.client.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//public class Test1 extends Activity {
public class Processes extends ListActivity {
	public static String serverName = null;
	private static final String TAG = "Anwim.Processes";

	public static final int MENU_SETTINGS = Menu.FIRST + 1;
	public static final int MENU_KILL = Menu.FIRST + 2;

	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<ProcessItem> items = new ArrayList<ProcessItem>();
	private ServiceAdapter m_adapter;
	private Runnable viewProcesses;
	private String errorMessage = null;

	/** The index of the title column */
	private static final int COLUMN_INDEX_TITLE = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// executeSearch();

		// tv.setText(pageContent);
		// setContentView(tv);

		setContentView(R.layout.processes);

		// add an item to the list so that the "no items found" message isn't
		// displayed when the app first starts up
		items.add(new ProcessItem());

		// Controlling the drawing behavior
		this.m_adapter = new ServiceAdapter(this, R.layout.processes_row, items);
		setListAdapter(this.m_adapter);
		getListView().setOnCreateContextMenuListener(this);

		// ListView lv = getListView();
		// // Then you can create a listener like so:
		// // Then you can create a listener like so:
		// lv
		// .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		// {
		// public boolean onItemLongClick(AdapterView<?> av, View v,
		// int pos, long id) {
		// return onLongListItemClick(av, v, pos, id);
		// // return false;
		// }
		//
		// });

		// Make sure the username and password are set
		checkAuthenticationSettings();
		executeSearch();
	}

	private void executeSearch() {
		errorMessage = null;
		/*
		 * if ((searchFor == null) || (searchFor.trim().length() == 0)) { new
		 * AlertDialog.Builder(this).setTitle("Error").setMessage(
		 * "Please enter your search criteria").setNeutralButton("OK", new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dlg, int sumthin) {
		 * 
		 * } }).show(); return; }
		 */
		prepareUserAgent(this);

		viewProcesses = new Runnable() {
			public void run() {
				try {
					processList();
				} catch (ApiException e) {
					Log.e(TAG, "Couldn't contact API", e);
				} catch (ParseException e) {
					Log.e(TAG, "Couldn't parse API response", e);
				}
			}
		};
		Thread thread = new Thread(null, viewProcesses, "MagentoBackground");
		thread.start();

		m_ProgressDialog = ProgressDialog.show(Processes.this, "Please wait...",
				"Retrieving data ...", true);
	}

	private RedirectHandler nonRedirectHandler = new RedirectHandler() {

		public boolean isRedirectRequested(HttpResponse response,
				HttpContext context) {
			return false;
		}

		public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
			return null;
		}
	};

	private Runnable returnRes = new Runnable() {
		public void run() {
			// if (items != null /*&& items.size() > 0*/) {
			// m_adapter.notifyDataSetChanged();
			// for (int i = 0; i < items.size(); i++)
			// m_adapter.add(items.get(i));
			// }
			m_ProgressDialog.dismiss();
			if (errorMessage != null) {
				((TextView) findViewById(android.R.id.empty))
						.setText(errorMessage);
			} else {
				((TextView) findViewById(android.R.id.empty))
						.setText(R.string.main_no_items);
			}
			m_adapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		checkAuthenticationSettings();
	}

	private void checkAuthenticationSettings() {
		// SharedPreferences settings =
		// this.getPreferences(Context.MODE_PRIVATE);
		if ((getUsername().length() == 0) || (getPassword().length() == 0)) {
			startActivity(new Intent(this,
					com.anwim.client.android.Settings.class));
		}
	}

	private String getPassword() {
		SharedPreferences settings = getSharedPreferences(Anwim.PREFS_NAME, 0);
		return settings.getString("password", "");
	}

	private String getUsername() {
		SharedPreferences settings = getSharedPreferences(Anwim.PREFS_NAME, 0);
		return settings.getString("username", "");
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		// getSelection().setText(items[position].toString());
		// new AlertDialog.Builder(this).setTitle("Item selected").setMessage(
		// "serverName=" + serverName + ", view.toptext="
		// + ((TextView) v.findViewById(R.id.toptext)).getText()
		// + ", position=" + position + ", id=" + id)
		// .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dlg, int sumthin) {
		//
		// }
		// }).show();

	}

	// public boolean onLongListItemClick(AdapterView parent, View v,
	// int position, long id) {
	// Log.i(TAG, "onLongListItemClick id=" + id);
	// return true;
	// }

	// private TextView getSelection() {
	// return ((TextView) findViewById(R.id.selection));
	// }

	/**
	 * Shared buffer used by {@link #remoteJsonRequest(String)} when reading
	 * results from an API request.
	 */
	private static byte[] sBuffer = new byte[512];

	/**
	 * User-agent string to use when making requests. Should be filled using
	 * {@link #prepareUserAgent(Context)} before making any other calls.
	 */
	private static String sUserAgent = null;

	/**
	 * Thrown when there were problems contacting the remote API server, either
	 * because of a network error, or the server returned a bad status code.
	 */
	public static class ApiException extends Exception {
		public ApiException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public ApiException(String detailMessage) {
			super(detailMessage);
		}
	}

	/**
	 * Thrown when there were problems parsing the response to an API call,
	 * either because the response was empty, or it was malformed.
	 */
	public static class ParseException extends Exception {
		public ParseException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}
	}

	/**
	 * Prepare the internal User-Agent string for use. This requires a
	 * {@link Context} to pull the package name and version number for this
	 * application.
	 */
	public static void prepareUserAgent(Context context) {
		try {
			// Read package name and version number from manifest
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			sUserAgent = String.format(context
					.getString(R.string.template_user_agent), info.packageName,
					info.versionName);

		} catch (NameNotFoundException e) {
			Log
					.e(
							TAG,
							"Couldn't find package information in PackageManager",
							e);
		}
	}

	public void processList() throws ApiException, ParseException {
		this.items.clear();

		try {
			String content = remoteJsonRequest("http://192.168.0.4:8080/anwims/processes.jsp?server="
					+ serverName);
			if (content != null) {
				if (content.startsWith("Error:")) {
					errorMessage = content;
				} else {
					try {
						// Drill into the JSON response to find the content body
						if (content.trim().length() > 0) {
							JSONObject response = new JSONObject(content);

							Iterator iterator = response.keys();
							while (iterator.hasNext()) {
								String processId = (String) iterator.next();
								String processName = (String) response
										.getString(processId);
								this.items.add(new ProcessItem(processName, processId));
							}
							Collections.sort(this.items, new byProcessName());

						}
					} catch (JSONException e) {
						throw new ParseException(
								"Problem parsing API response", e);
					}

				}
			}
		} catch (Exception e) {
			errorMessage = "Error:" + e.getMessage();
			Log.e(TAG, e.getMessage(), e);
		}

		runOnUiThread(returnRes);
	}

	public void processesKill(String pid) throws ApiException,
			ParseException {
		this.items.clear();

		try {
			String content = remoteJsonRequest("http://192.168.0.4:8080/anwims/processeskill.jsp?server="
					+ serverName + "&pid=" + pid);
			if (content != null) {
				if (content.startsWith("Error:")) {
					errorMessage = content;
				} else {
					try {
						// Drill into the JSON response to find the content body
						if (content.trim().length() > 0) {
							JSONObject response = new JSONObject(content);

							Iterator iterator = response.keys();
							while (iterator.hasNext()) {
								String processId = (String) iterator.next();
								String processName = (String) response
										.getString(processId);
								this.items.add(new ProcessItem(processName, processId));
							}
							Collections.sort(this.items, new byProcessName());

						}
					} catch (JSONException e) {
						throw new ParseException(
								"Problem parsing API response", e);
					}

				}
			}
		} catch (Exception e) {
			errorMessage = "Error:" + e.getMessage();
			Log.e(TAG, e.getMessage(), e);
		}

		runOnUiThread(returnRes);
	}

	protected String remoteJsonRequest(String requestUri) throws ApiException {
		String result = "";
		if (sUserAgent == null) {
			throw new ApiException("User-Agent string must be prepared");
		}

		DefaultHttpClient client = new DefaultHttpClient();
		client.setRedirectHandler(nonRedirectHandler); // I don't want to follow
		try {
			result = httpRequest(client, requestUri);
			client.getConnectionManager().shutdown();
			return result;
		} catch (IOException e) {
			throw new ApiException("Problem communicating with API", e);
		}
	}

	private String ssoLogin(DefaultHttpClient client)
			throws UnsupportedEncodingException, IOException,
			ClientProtocolException {
		String result = ""; // an empty string means all was well
		HttpResponse response;
		StatusLine status;
		HttpEntity entity;
		HttpPost httpost = new HttpPost(
				"https://www.whatever.com/am/UI/Login?module=AD");
		httpost.setHeader("User-Agent", sUserAgent);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("IDToken1", getUsername()));
		nvps.add(new BasicNameValuePair("IDToken2", getPassword()));

		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

		response = client.execute(httpost);

		// Check if server response is valid
		status = response.getStatusLine();

		result = checkSsoCookie(client);

		entity = response.getEntity();

		Log.d(TAG, "login http status result: " + response.getStatusLine());
		if (entity != null) {
			entity.consumeContent();
		}

		return result;

	}

	private String checkSsoCookie(DefaultHttpClient client) {
		String result = "";
		boolean foundCookie = false;
		Log.d(TAG, "Initial set of cookies:");
		List<Cookie> cookies = client.getCookieStore().getCookies();
		if (cookies.isEmpty()) {
			result = "Error: unable to log in.\nNo cookies were exchanged.";
			Log.d(TAG, "\tNo cookies");
		} else {
			for (int i = 0; i < cookies.size(); i++) {
				if (cookies.get(i).getName().equalsIgnoreCase(
						"iPlanetDirectoryPro")) {
					foundCookie = true;
				}
				Log.d(TAG, "\tCookie: " + cookies.get(i).toString());
			}
		}
		if (!foundCookie) {
			result = "Error: unable to log in.\n  Invalid username or password.";

		}
		return result;
	}

	private String httpRequest(HttpClient client, String requestUri)
			throws UnsupportedEncodingException, IOException,
			ClientProtocolException {
		String result = "";
		HttpResponse response;
		StatusLine status;
		HttpEntity entity;
		HttpGet httpget = new HttpGet(requestUri);
		httpget.setHeader("User-Agent", sUserAgent);

		response = client.execute(httpget);
		// Check if server response is valid
		status = response.getStatusLine();
		Log.d(TAG, "httpRequest http status result: "
				+ response.getStatusLine());
		entity = response.getEntity();
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

			// Pull content stream from response
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			result = new String(content.toByteArray());
		} else {
			entity.consumeContent();
		}

		return result;
	}

	private void ssoLogout(HttpClient client) throws IOException,
			ClientProtocolException {
		HttpGet request;
		HttpResponse response;
		// logout
		request = new HttpGet("https://www.whatever.com/am/UI/Logout");
		request.setHeader("User-Agent", sUserAgent);
		response = client.execute(request);
		Log.d(TAG, "ssoLogout http status result: " + response.getStatusLine());
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem temp = menu.add(0, MENU_SETTINGS, 0, "Settings");
		temp.setIcon(R.drawable.ic_menu_preferences);
		return true;
		// theMenu = menu;
		// /new MenuInflater(this).inflate(R.menu.mainmenu, menu);
		// return (super.onCreateOptionsMenu(menu));
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		ProcessItem processItem = (ProcessItem) getListAdapter().getItem(
				info.position);
		if (processItem == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}

		// Setup the menu header
		menu.setHeaderTitle(processItem.getName());

		// Add a menu item to delete the note
		menu.add(0, MENU_KILL, 0, "Kill");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		ProcessItem processItem = (ProcessItem) getListAdapter().getItem(
				info.position);
		switch (item.getItemId()) {
		case MENU_KILL: {
			try {
				// Delete the note that the context menu is for
				// Uri noteUri =
				// ContentUris.withAppendedId(getIntent().getData(),
				// info.id);
				// getContentResolver().delete(noteUri, null, null);
				Log.d(TAG, "Long:Stop");
				Log.d(TAG, processItem.getName());
				processesKill(processItem.getName());
			} catch (ParseException e) {
				Log.e(TAG, "processKill", e);
			} finally {
				return true; // this means you handled the long click so don't
				// bubble to regular click
			}
		}
		}
		return false;
	}

	public void onSearchClick(View theButton) {
		executeSearch();
	}

	private class ServiceAdapter extends ArrayAdapter<ProcessItem> {

		private ArrayList<ProcessItem> processes;

		public ServiceAdapter(Context context, int textViewResourceId,
				ArrayList<ProcessItem> processes) {
			super(context, textViewResourceId, processes);
			this.processes = processes;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.processes_row, null);
			}
			ProcessItem o = processes.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				if (tt != null) {
					tt.setText(o.getName());
				}

				TextView rt = (TextView) v.findViewById(R.id.righttext);
				if (rt != null) {
					rt.setText(o.getPid());
				}
/*
				 * if (bt != null) { bt.setText("Work Phone: " +
				 * o.getBusinessPhone()); }
				 */
			}
			return v;
		}
	}

}