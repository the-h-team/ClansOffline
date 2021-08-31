package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.Location;

public class OfflineClaim extends Claim {
	public OfflineClaim(HUID id, Location location, HUID owner) {
		super(id, location, owner);
	}
}
