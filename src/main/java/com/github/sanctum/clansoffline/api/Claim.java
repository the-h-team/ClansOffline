package com.github.sanctum.clansoffline.api;

import com.github.sanctum.clansoffline.impl.ClanDataFile;
import com.github.sanctum.clansoffline.impl.LocationStorage;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.Chunk;
import org.bukkit.Location;

public abstract class Claim {

	private final HUID id;
	private final HUID owner;
	private final Location location;
	private boolean active;

	public Claim(HUID id, Location location, HUID owner) {
		this.id = id;
		this.owner = owner;
		this.location = location;
	}

	public HUID getId() {
		return id;
	}

	public Location getLocation() {
		return location;
	}

	public Chunk getChunk() {
		return location.getChunk();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Clan getOwner() {
		return ClansAPI.getInstance().getClan(this.owner);
	}


	public final void save() {
		ClanDataFile file = getOwner().getData();
		file.set("claims." + getId().toString() + ".storage", new LocationStorage(getLocation()));
		file.set("claims." + getId().toString() + ".active", active);
	}

}
