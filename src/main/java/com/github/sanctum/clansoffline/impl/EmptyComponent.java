package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;

public class EmptyComponent implements Clan.Storage {

	private final String text;

	public EmptyComponent() {
			this.text = "No context";
		}

	@Override
	public String toString() {
		return this.text;
	}
}
