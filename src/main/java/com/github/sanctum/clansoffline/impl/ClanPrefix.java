package com.github.sanctum.clansoffline.impl;

public class ClanPrefix {

	private final String start;
	private final String text;
	private final String end;

	public ClanPrefix(String start, String text, String end) {
		this.start = start;
		this.text= text;
		this.end = end;
	}

	public String getStart() {
		return start;
	}

	public String getText() {
		return text;
	}

	public String getEnd() {
		return end;
	}

	public String getJoined() {
		return getStart() + getText() + getEnd();
	}

}
