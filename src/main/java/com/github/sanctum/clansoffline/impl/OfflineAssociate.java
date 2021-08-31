package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;

public class OfflineAssociate extends Clan.Associate {
	public OfflineAssociate(String id, Clan c, Clan.Rank r) {
		super(id, c, r);
	}
}
