package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

public class ClanDataContainer extends Clan.Component<ClanPersistentContainer> {

	public ClanDataContainer(ClanPersistentContainer container) {
		super(container);
	}

	public boolean exists(String key) {
		return getComponent().getStorage().exists(key);
	}

	public <R> R get(Class<R> type, String key) {
		return getComponent().getStorage().get(type, key);
	}

	public <R> R attach(String key, R value) {
		return getComponent().getStorage().attach(key, value);
	}

	public <R> R lend(String key, R value) {
		return getComponent().getStorage().lend(key, value);
	}


	public List<String> persistnetKeySet() {
		return getComponent().getStorage().persistentKeySet();
	}

	public Set<String> keySet() {
		return getComponent().getStorage().keySet();
	}

	public <R> Collection<? extends R> values(Class<R> type) {
		return getComponent().getStorage().values(type);
	}


}
