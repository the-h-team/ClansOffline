package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

public class ClanDataFile extends Clan.Component<ClanFileManager> {
	public ClanDataFile(ClanFileManager fileManager) {
		super(fileManager);
	}

	public boolean exists() {
		return getComponent().getStorage().exists();
	}

	public void save() {
		getComponent().getStorage().saveConfig();
	}

	public boolean delete() {
		return getComponent().getStorage().delete();
	}

	public @Nullable
	Location getLocation(String path) {
		return getComponent().getStorage().getLegacySafeLocation(path);
	}

	public @Nullable LocationStorage getLocationStorage(String path) {
		return read(fc -> {
			final Object o = fc.get(path);
			if (!(o instanceof LocationStorage)) return null;
			return ((LocationStorage) o);
		});
	}

	public <R> R read(Function<FileConfiguration, R> function) {
		return getComponent().getStorage().readValue(function);
	}

	public void set(String path, Object value) {
		getComponent().getStorage().getConfig().set(path, value);
		save();
	}

	public <X extends CharSequence> boolean exists(X path) {
		return getComponent().getStorage().getConfig().get(path.toString()) != null;
	}


}
