package com.github.sanctum.clansoffline.lib;

import com.github.sanctum.clansoffline.api.ClanAddon;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public class AddonManager {

	private final Set<ClanAddon> ADDONS = new HashSet<>();

	public boolean load(@NotNull ClanAddon addon) {
		addon.onEnable();
		return ADDONS.add(addon);
	}

	public boolean remove(@NotNull ClanAddon addon) {
		addon.onDisable();
		return ADDONS.remove(addon);
	}

	public ClanAddon get(Predicate<ClanAddon> predicate) {
		for (ClanAddon addon : ADDONS) {
			if (predicate.test(addon)) {
				return addon;
			}
		}
		return null;
	}


}
