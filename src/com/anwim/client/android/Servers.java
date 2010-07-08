package com.anwim.client.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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
import org.json.JSONArray;
import org.json.JSONException;

import com.anwim.client.android.R;
import com.anwim.client.android.Services.ParseException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

//public class Test1 extends Activity {
public class Servers extends ListActivity {
	private static final String TAG = "Anwim.Servers";

	public static final int MENU_SETTINGS = Menu.FIRST + 1;
	public static final int MENU_SERVICES = Menu.FIRST + 2;
	public static final int MENU_PROCESSES = Menu.FIRST + 3;

	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<String> servers = new ArrayList<String>();
	private ServerAdapter m_adapter;
	private Runnable viewServers;
	private String errorMessage = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// executeSearch();

		// tv.setText(pageContent);
		// setContentView(tv);

		setContentView(R.layout.servers);

		// add an item to the list so that the "no items found" message isn't
		// displayed when the app first starts up
		servers.add("");

		// Controlling the drawing behavior
		this.m_adapter = new ServerAdapter(this, R.layout.row, servers);
		setListAdapter(this.m_adapter);

		getListView().setOnCreateContextMenuListener(this);
		
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

		viewServers = new Runnable() {
			public void run() {
				try {
					parseContent();
				} catch (ApiException e) {
					Log.e(TAG, "Couldn't contact API", e);
				} catch (ParseException e) {
					Log.e(TAG, "Couldn't parse API response", e);
				}
			}
		};
		Thread thread = new Thread(null, viewServers, "MagentoBackground");
		thread.start();

		m_ProgressDialog = ProgressDialog.show(Servers.this, "Please wait...",
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
		// "view.toptext="
		// + ((TextView) v.findViewById(R.id.toptext)).getText()
		// + ", position=" + position + ", id=" + id)
		// .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dlg, int sumthin) {
		//
		// }
		// }).show();

		// ServiceItem serviceItem = (ServiceItem)
		// getListAdapter().getItem(info.position);
		Services.serverName = servers.get(position);
		startActivity(new Intent(this, com.anwim.client.android.Services.class));

	}

	// private TextView getSelection() {
	// return ((TextView) findViewById(R.id.selection));
	// }

	/**
	 * Shared buffer used by {@link #getUrlContent(String)} when reading results
	 * from an API request.
	 */
	private static byte[] sBuffer = new byte[512];

	/**
	 * User-agent string to use when making requests. Should be filled using
	 * {@link #prepareUserAgent(Context)} before making any other calls.
	 */
	private static String sUserAgent = null;
	private static String searchFor = null;

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

	public void parseContent() throws ApiException, ParseException {
		this.servers.clear();

		try {
			String content = getUrlContent();
			if (content != null) {
				if (content.startsWith("Error:")) {
					errorMessage = content;
				} else {
					try {
						// Drill into the JSON response to find the content body
						if (content.trim().length() > 0) {
							JSONArray response = new JSONArray(content);
							// content = "";
							int itemCount = response.length();
							for (int i = 0; i < itemCount; i++) {
								this.servers.add(response.getString(i));
							}
							Collections.sort(this.servers);
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

	/**
	 * Pull the raw text content of the given URL. This call blocks until the
	 * operation has completed, and is synchronized because it uses a shared
	 * buffer {@link #sBuffer}.
	 * 
	 * @param url
	 *            The exact URL to request.
	 * @return The raw content returned by the server.
	 * @throws ApiException
	 *             If any connection or server error occurs.
	 */
	protected String getUrlContent() throws ApiException {
		String result = "";
		if (sUserAgent == null) {
			throw new ApiException("User-Agent string must be prepared");
		}

		// XTrustProvider.install();
		// SchemeRegistry schemeRegistry = new SchemeRegistry();
		// schemeRegistry.register(new Scheme("https",
		// SSLSocketFactory.getSocketFactory(), 443));

		// HttpParams params = new BasicHttpParams();

		// SingleClientConnManager mgr = new SingleClientConnManager(params,
		// schemeRegistry);

		// Create client and set our specific user-agent string
		// HttpClient client = new DefaultHttpClient(mgr, params);
		DefaultHttpClient client = new DefaultHttpClient();
		client.setRedirectHandler(nonRedirectHandler); // I don't want to follow
		// redirects
		try {
			result = serversList(client);

			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
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

	private String serversList(HttpClient client)
			throws UnsupportedEncodingException, IOException,
			ClientProtocolException {
		String result = "";
		HttpResponse response;
		StatusLine status;
		HttpEntity entity;
		HttpGet httpget = new HttpGet(
				"http://192.168.0.7:8080/anwims/default.jsp");
		httpget.setHeader("User-Agent", sUserAgent);

		response = client.execute(httpget);
		// Check if server response is valid
		status = response.getStatusLine();
		Log.d(TAG, "serversList http status result: "
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

	public void onSearchClick(View theButton) {
		executeSearch();
	}

	private class ServerAdapter extends ArrayAdapter<String> {

		private ArrayList<String> servers;

		public ServerAdapter(Context context, int textViewResourceId,
				ArrayList<String> servers) {
			super(context, textViewResourceId, servers);
			this.servers = servers;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			String o = servers.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(o);
				}
				/*
				 * if (bt != null) { bt.setText("Work Phone: " +
				 * o.getBusinessPhone()); }
				 */
			}
			return v;
		}
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

		String server = servers.get(info.position);
		if (server == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}

		// Setup the menu header
		menu.setHeaderTitle(server);

		// Add a menu item to delete the note
		menu.add(0, MENU_SERVICES, 0, "Services");
		menu.add(0, MENU_PROCESSES, 0, "Processes");
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

		// ServerItem serverItem = (ServerItem) getListAdapter().getItem(
		// info.position);
		switch (item.getItemId()) {
		case MENU_SERVICES: {
			try {
				// Delete the note that the context menu is for
				// Uri noteUri =
				// ContentUris.withAppendedId(getIntent().getData(),
				// info.id);
				// getContentResolver().delete(noteUri, null, null);
				Log.d(TAG, "Long:Services");
				Log.d(TAG, servers.get(info.position));
				Services.serverName = servers.get(info.position);
				startActivity(new Intent(this,
						com.anwim.client.android.Services.class));

			} finally {
				return true; // this means you handled the long click so don't
				// bubble to regular click
			}
		}
		case MENU_PROCESSES: {
			// Delete the note that the context menu is for
			// Uri noteUri = ContentUris.withAppendedId(getIntent().getData(),
			// info.id);
			// getContentResolver().delete(noteUri, null, null);
			try {
				// Delete the note that the context menu is for
				// Uri noteUri =
				// ContentUris.withAppendedId(getIntent().getData(),
				// info.id);
				// getContentResolver().delete(noteUri, null, null);
				Log.d(TAG, "Long:Processes");
				Log.d(TAG, servers.get(info.position));
				Processes.serverName = servers.get(info.position);
				startActivity(new Intent(this,
						com.anwim.client.android.Processes.class));

			} finally {
				return true; // this means you handled the long click so don't
				// bubble to regular click
			}
		}
		}
		return false;
	}

}