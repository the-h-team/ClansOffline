package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanFileManager implements Clan.Storage {

	private static final long serialVersionUID = -3528265220538365419L;
	private final FileManager manager;

	public ClanFileManager(@NotNull String id, @Nullable String location) {
		this.manager = FileList.search(ClansAPI.getInstance().getPlugin()).find(id, location);
	}

	public FileManager getStorage() {
		return manager;
	}
}
