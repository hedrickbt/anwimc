package com.anwim.client.android;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcessItem {
	private String name = "";
	private String pid = "";

	public ProcessItem() {
		
	}
	
	public ProcessItem(String name, String pid) {
		this.name = name;
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public String getPid() {
		return pid;
	}

	public String toString() {
		return name;
	}
}
