package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import com.sun.istack.internal.NotNull;
import org.bukkit.Location;

public class ClanBase extends Clan.Component<LocationStorage> {

	private final Clan parent;

	public ClanBase(Clan clan, Location location) {
		super(new LocationStorage(location));
		this.parent = clan;
	}

	public ClanBase(Clan clan, LocationStorage storage) {
		super(storage);
		this.parent = clan;
	}

	public void update(@NotNull Location location) {
		this.t = new LocationStorage(location);
	}

	public Clan getParent() {
		return parent;
	}
}
