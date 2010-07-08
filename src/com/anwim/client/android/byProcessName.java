package com.anwim.client.android;

public class byProcessName implements java.util.Comparator<ProcessItem> {
	public int compare(ProcessItem left, ProcessItem right) {
		int result = left.getName().toUpperCase().compareTo(right.getName().toUpperCase());
		return result;
	}
}
