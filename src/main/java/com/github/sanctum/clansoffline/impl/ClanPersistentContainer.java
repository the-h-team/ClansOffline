package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.container.PersistentContainer;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import com.sun.istack.internal.NotNull;

public class ClanPersistentContainer implements Clan.Storage {

	private final PersistentContainer manager;

	public ClanPersistentContainer(@NotNull String id) {
		this.manager = LabyrinthProvider.getInstance().getContainer(new NamespacedKey(ClansAPI.getInstance().getPlugin(), id));
	}

	@Override
	public PersistentContainer getStorage() {
		return manager;
	}
}
