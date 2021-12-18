package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ClanAllyRequests implements Serializable {

	private static final long serialVersionUID = 8245101703488368379L;

	private final Set<String> ids = new HashSet<>();

	public boolean contains(Clan c) {
		return ids.contains(c.getId().toString());
	}

	public boolean add(Clan c) {
		if (contains(c)) return false;
		return ids.add(c.getId().toString());
	}

	public boolean remove(Clan c) {
		if (!contains(c)) return false;
		return ids.remove(c.getId().toString());
	}

	public Clan[] get() {
		return ids.stream().map(s -> ClansAPI.getInstance().getClan(HUID.parseID(s).toID())).toArray(Clan[]::new);
	}

}
