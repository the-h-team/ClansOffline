package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Clan;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SerializableAs("com.github.sanctum.clansoffline.impl.LocationStorage")
public class LocationStorage implements Clan.Storage, ConfigurationSerializable {

	private final Location location;

	public LocationStorage(@Nullable Location location) {
		this.location = location;
	}

	@Override
	public @Nullable
	Location getStorage() {
		return this.location;
	}

	@NotNull
	@Override
	public Map<String, Object> serialize() {
		final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
		builder.put("location", this.location);
		return builder.build();
	}

	public static LocationStorage deserialize(Map<String, Object> map) throws IllegalArgumentException {
		Location loc = (Location) map.get("location");
		return new LocationStorage(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
	}

}
