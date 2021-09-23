package com.github.sanctum.clansoffline.api;

import com.github.sanctum.clansoffline.bank.BankManager;
import com.github.sanctum.clansoffline.bukkit.ClansJavaPlugin;
import com.github.sanctum.clansoffline.impl.ClanDataFile;
import com.github.sanctum.clansoffline.impl.ClanPrefix;
import com.github.sanctum.clansoffline.lib.AddonManager;
import com.github.sanctum.clansoffline.lib.ClaimManager;
import com.github.sanctum.clansoffline.lib.ClanManager;
import com.github.sanctum.clansoffline.lib.ShieldManager;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClansAPI {

	static ClansAPI getInstance() {
		ClansAPI api = Bukkit.getServicesManager().load(ClansAPI.class);
		return api != null ? api : (ClansAPI) JavaPlugin.getProvidingPlugin(ClansJavaPlugin.class);
	}

	ClanPrefix getPrefix();

	Plugin getPlugin();

	@Nullable Clan getClan(HUID clanId);

	@Nullable Clan getClan(String clanName);

	@Nullable Clan.Associate getAssociate(OfflinePlayer player);

	@Nullable Clan.Associate getAssociate(String playerName);

	@NotNull ClanDataFile getMain();

	@NotNull AddonManager getAddonManager();

	@NotNull ClanManager getClanManager();

	@NotNull ClaimManager getClaimManager();

	@NotNull ShieldManager getShieldManager();

	BankManager getBankManager();


}
