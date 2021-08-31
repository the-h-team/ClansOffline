package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public class ClanFileManager implements Clan.Storage {

	private final FileManager manager;

	public ClanFileManager(@NotNull String id, @Nullable String location) {
		this.manager = FileList.search(ClansAPI.getInstance().getPlugin()).find(id, location);
	}

	public FileManager getStorage() {
		return manager;
	}
}
