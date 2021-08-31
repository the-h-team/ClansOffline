package com.github.sanctum.clansoffline.bukkit;

import com.github.sanctum.clansoffline.api.ClansAPI;
import java.util.List;

public class StringLibrary {

	public static synchronized String get(String path) {
		return ClansAPI.getInstance().getMain().read(f -> f.getString(path));
	}

	public static synchronized int getInt(String path) {
		return ClansAPI.getInstance().getMain().read(f -> f.getInt(path));
	}

	public static synchronized int getClearance(String path) {
		return ClansAPI.getInstance().getMain().read(f -> f.getInt("Clans.clearance-adjustment." + path));
	}

	public static synchronized double getDouble(String path) {
		return ClansAPI.getInstance().getMain().read(f -> f.getDouble(path));
	}

	public static synchronized List<String> getList(String path) {
		return ClansAPI.getInstance().getMain().read(f -> f.getStringList(path));
	}

}
