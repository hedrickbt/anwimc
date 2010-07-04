package com.anwim.client.android;

public class byServiceName implements java.util.Comparator<ServiceItem> {
	public int compare(ServiceItem left, ServiceItem right) {
		int result = left.getName().toUpperCase().compareTo(right.getName().toUpperCase());
		return result;
	}
}
