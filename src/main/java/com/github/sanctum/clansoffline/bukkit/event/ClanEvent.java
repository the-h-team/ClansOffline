package com.github.sanctum.clansoffline.bukkit.event;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.labyrinth.event.custom.Vent;

public abstract class ClanEvent extends Vent {

	public ClanEvent() {
		super(false);
	}

	public ClanEvent(boolean isAsync) {
		super(isAsync);
	}

	public abstract Clan getClan();

}
