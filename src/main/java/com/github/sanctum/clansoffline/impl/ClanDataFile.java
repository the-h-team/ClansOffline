package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.labyrinth.data.Configurable;
import java.util.function.Function;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class ClanDataFile extends Clan.Component<ClanFileManager> {
	public ClanDataFile(ClanFileManager fileManager) {
		super(fileManager);
	}

	public boolean exists() {
		return getComponent().getStorage().getRoot().exists();
	}

	public void save() {
		getComponent().getStorage().getRoot().save();
	}

	public boolean delete() {
		return getComponent().getStorage().getRoot().delete();
	}

	public @Nullable
	Location getLocation(String path) {
		return getComponent().getStorage().getRoot().getLocation(path);
	}

	public @Nullable
	LocationStorage getLocationStorage(String path) {
		return read(fc -> {
			final Object o = fc.getNode(path).get();
			if (!(o instanceof LocationStorage)) return null;
			return ((LocationStorage) o);
		});
	}

	public <R> R read(Function<Configurable, R> function) {
		return getComponent().getStorage().read(function);
	}

	public void set(String path, Object value) {
		getComponent().getStorage().getRoot().set(path, value);
		save();
	}

	public <X extends CharSequence> boolean exists(X path) {
		return getComponent().getStorage().getRoot().getNode(path.toString()).get() != null;
	}


}
