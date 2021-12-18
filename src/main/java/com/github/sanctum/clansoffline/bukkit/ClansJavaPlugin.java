package com.github.sanctum.clansoffline.bukkit;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClanAddon;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bank.BankManager;
import com.github.sanctum.clansoffline.bukkit.command.ClanCommand;
import com.github.sanctum.clansoffline.bukkit.event.claim.ClaimResidentEvent;
import com.github.sanctum.clansoffline.bukkit.event.claim.WildernessInhabitantEvent;
import com.github.sanctum.clansoffline.impl.ClanDataFile;
import com.github.sanctum.clansoffline.impl.ClanFileManager;
import com.github.sanctum.clansoffline.impl.ClanPrefix;
import com.github.sanctum.clansoffline.impl.LocationStorage;
import com.github.sanctum.clansoffline.impl.Resident;
import com.github.sanctum.clansoffline.impl.placeholder.LabyrinthPlaceholders;
import com.github.sanctum.clansoffline.impl.placeholder.PapiPlaceholders;
import com.github.sanctum.clansoffline.lib.AddonManager;
import com.github.sanctum.clansoffline.lib.ClaimManager;
import com.github.sanctum.clansoffline.lib.ClanManager;
import com.github.sanctum.clansoffline.lib.ShieldManager;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.event.EasyListener;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ClansJavaPlugin extends JavaPlugin implements ClansAPI {

	private ClanManager clanManager;
	private ClaimManager claimManager;
	private ShieldManager shieldManager;
	private AddonManager adddonManager;
	private ClanDataFile file;
	private ClanPrefix prefix;
	private BankManager bankManager;

	@Override
	public void onEnable() {
		// Plugin startup logic
		Bukkit.getServicesManager().register(ClansAPI.class, this, this, ServicePriority.High);
		ConfigurationSerialization.registerClass(LocationStorage.class);
		this.file = new ClanDataFile(new ClanFileManager("Config", "Configuration"));

		if (!this.file.exists()) {
			FileList.search(this).copyYML("Config", file.getComponent().getStorage());
		}
		this.claimManager = new ClaimManager();
		this.clanManager = new ClanManager();
		this.adddonManager = new AddonManager();
		this.shieldManager = new ShieldManager();
		this.bankManager = new BankManager(this);
		Optional.ofNullable(getMain().read(f -> f.getString("Clans.bank.starting-balance"))).map(s -> {
			try {
				return new BigDecimal(s);
			} catch (NumberFormatException ignored) {
				return null;
			}
		}).ifPresent(bankManager::setStartingBalance);
		CommandRegistration.use(new ClanCommand());
		new Registry<>(Listener.class).source(this).pick("com.github.sanctum.clansoffline.bukkit.listener").operate(l -> LabyrinthProvider.getService(Service.VENT).subscribe(this, l));
		getClanManager().refresh();
		new Registry.Loader<>(ClanAddon.class).source(this).from("Addons").confine(ClanAddon::register);
		this.prefix = new ClanPrefix(file.read(f -> f.getString("Formatting.Prefix.Start")), file.read(f -> f.getString("Formatting.Prefix.Text")), file.read(f -> f.getString("Formatting.Prefix.End")));
		TaskScheduler.of(() -> {
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				getLogger().info("- Placeholder api found! Registering expansion.");
				new PapiPlaceholders().register();
			} else {
				getLogger().warning("- PlaceholderAPI not found, unable to register placeholders.");
			}
			new LabyrinthPlaceholders().register().deploy();
		}).scheduleLater(85);
	}

	@Override
	public void onDisable() {
		getClanManager().getClans().list().forEach(c -> {
			c.save();
			for (Claim cl : c.getClaims()) {
				cl.save();
			}
		});
		bankManager.safeShutdown(this);
	}

	@Override
	public ClanPrefix getPrefix() {
		return this.prefix;
	}

	@Override
	public Plugin getPlugin() {
		return this;
	}

	@Override
	public Clan getClan(HUID clanId) {
		return getClanManager().getClans().filter(c -> c.getId().equals(clanId)).findFirst().orElse(null);
	}

	@Override
	public Clan getClan(String clanName) {
		return getClanManager().getClans().filter(c -> c.getName().equals(clanName)).findFirst().orElse(null);
	}

	@Override
	public Clan.Associate getAssociate(OfflinePlayer player) {
		return getAssociate(player.getName());
	}

	@Override
	public Clan.Associate getAssociate(String playerName) {
		for (Clan c : getClanManager().getClans().list()) {
			for (Clan.Associate a : c.getMembers()) {
				if (a.getId().equals(playerName)) {
					return a;
				}
			}
		}
		return null;
	}

	@NotNull
	@Override
	public ClanDataFile getMain() {
		return this.file;
	}

	@NotNull
	@Override
	public AddonManager getAddonManager() {
		return this.adddonManager;
	}

	@NotNull
	@Override
	public ClanManager getClanManager() {
		return this.clanManager;
	}

	@NotNull
	@Override
	public ClaimManager getClaimManager() {
		return this.claimManager;
	}

	@NotNull
	@Override
	public ShieldManager getShieldManager() {
		return this.shieldManager;
	}

	@Override
	public BankManager getBankManager() {
		return this.bankManager;
	}
}
