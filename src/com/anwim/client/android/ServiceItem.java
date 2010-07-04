package com.anwim.client.android;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceItem {
	private String name = "";
	private int status = -1;

	public ServiceItem() {
		
	}
	
	public ServiceItem(String name, int status) {
		this.name = name;
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public int getStatus() {
		return status;
	}

	public String toString() {
		return name;
	}
}
