package com.github.sanctum.clansoffline.bukkit;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.api.ClanAddon;
import com.github.sanctum.clansoffline.bank.BankManager;
import com.github.sanctum.clansoffline.bukkit.command.ClanCommand;
import com.github.sanctum.clansoffline.bukkit.event.ClaimResidentialEvent;
import com.github.sanctum.clansoffline.impl.ClanDataFile;
import com.github.sanctum.clansoffline.impl.ClanFileManager;
import com.github.sanctum.clansoffline.impl.ClanPrefix;
import com.github.sanctum.clansoffline.impl.LocationStorage;
import com.github.sanctum.clansoffline.impl.Resident;
import com.github.sanctum.clansoffline.lib.AddonManager;
import com.github.sanctum.clansoffline.lib.ClaimManager;
import com.github.sanctum.clansoffline.lib.ClanManager;
import com.github.sanctum.clansoffline.lib.Manager;
import com.github.sanctum.clansoffline.lib.ShieldManager;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.event.EasyListener;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

	private final Set<Manager<?>> managers = new HashSet<>();
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
		Schedule.sync(() -> {

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (getClaimManager().isInClaim(p.getLocation())) {
					Claim claim = getClaimManager().get(c -> c.getChunk().equals(p.getLocation().getChunk()));
					ClaimResidentialEvent event = new Vent.Call<>(new ClaimResidentialEvent(claim, p)).run();
					if (!event.isCancelled()) {
						Resident r = event.getResident();
						Claim lastKnown = r.getClaim();
						if (claim.isActive()) {
							if (claim.getOwner() == null) {
								getClaimManager().remove(claim);
								return;
							}
							if (!event.getClaim().getId().equals(lastKnown.getId())) {
								if (r.hasProperty(Resident.Property.NOTIFIED)) {
									if (!lastKnown.getOwner().equals(claim.getOwner())) {
										r.setProperty(Resident.Property.NOTIFIED, false);
										Clan.Associate associate = ClansAPI.getInstance().getAssociate(r.getPlayer().getName());
										if (associate != null) {
											if (lastKnown.getOwner().equals(associate.getClan())) {
												r.setProperty(Resident.Property.TRAVERSED, true);
											}
										}
										r.update(event.getClaim());
										r.update(System.currentTimeMillis());
									}
								}
							}
							if (!r.hasProperty(Resident.Property.NOTIFIED)) {
								event.sendNotification();
								r.setProperty(Resident.Property.NOTIFIED, true);
							} else {
								if (r.hasProperty(Resident.Property.TRAVERSED)) {
									Clan.Associate associate = ClansAPI.getInstance().getAssociate(r.getPlayer().getName());
									if (associate != null) {
										r.setProperty(Resident.Property.TRAVERSED, false);
										r.setProperty(Resident.Property.NOTIFIED, false);
										r.update(System.currentTimeMillis());
									}
								}
							}
						}
					}
				} else {
					// Call wild event.
					Resident resident = ClaimResidentialEvent.getResident(p);
					if (resident != null) {
						resident.remove();
					}
				}
			}

		}).repeatReal(0, 20);
		managers.add(new ClanManager());
		managers.add(new ClaimManager());
		managers.add(new ShieldManager());
		managers.add(new AddonManager());
		this.bankManager = new BankManager(this);
		Optional.ofNullable(getMain().read(f -> f.getString("Clans.bank.starting-balance"))).map(s -> {
			try {
				return new BigDecimal(s);
			} catch (NumberFormatException ignored) {
				return null;
			}
		}).ifPresent(bankManager::setStartingBalance);
		CommandRegistration.use(new ClanCommand());
		new Registry<>(Listener.class).source(this).pick("com.github.sanctum.clansoffline.bukkit.listener").operate(l -> {
			new EasyListener(l).call(this);
			Vent.register(this, l);
		});
		getClanManager().refresh();
		new Registry.Loader<>(ClanAddon.class).source(this).from("Addons").operate(ClanAddon::register);
		this.prefix = new ClanPrefix(file.read(f -> f.getString("Formatting.Prefix.Start")), file.read(f -> f.getString("Formatting.Prefix.Text")), file.read(f -> f.getString("Formatting.Prefix.End")));
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
		return managers.stream().filter(m -> m instanceof AddonManager).map(manager -> (AddonManager)manager).findFirst().get();
	}

	@NotNull
	@Override
	public ClanManager getClanManager() {
		return managers.stream().filter(m -> m instanceof ClanManager).map(manager -> (ClanManager)manager).findFirst().get();
	}

	@NotNull
	@Override
	public ClaimManager getClaimManager() {
		return managers.stream().filter(m -> m instanceof ClaimManager).map(manager -> (ClaimManager)manager).findFirst().get();
	}

	@NotNull
	@Override
	public ShieldManager getShieldManager() {
		return managers.stream().filter(m -> m instanceof ShieldManager).map(manager -> (ShieldManager)manager).findFirst().get();
	}

	@Override
	public BankManager getBankManager() {
		return bankManager;
	}
}
