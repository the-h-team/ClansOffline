package com.github.sanctum.clansoffline.lib;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.impl.OfflineClan;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ClanManager extends Manager<Clan>{

	private final List<Clan> CLANS = new LinkedList<>();

	@Override
	public boolean load(@NotNull Clan clan) {
		return this.CLANS.add(clan);
	}

	@Override
	public boolean remove(@NotNull Clan c) {
		c.getData().delete();
		return CLANS.remove(c);
	}

	public boolean remove(@NotNull Clan.Associate associate) {
		Clan c = associate.getClan();
		return c.remove(associate);
	}

	public Clan generateClan(OfflinePlayer owner, String name) {
		Clan c = new OfflineClan(HUID.randomID().toString());
		c.setOwner(owner.getName());
		c.setName(name);
		load(c);
		return c;
	}

	public Clan generateClan(OfflinePlayer owner, String name, String password) {
		Clan c = new OfflineClan(HUID.randomID().toString());
		c.setOwner(owner.getName());
		c.setName(name);
		c.setPassword(password);
		load(c);
		return c;
	}

	public UniformedComponents<Clan> getClans() {
		return UniformedComponents.accept(CLANS);
	}

	public void refresh() {
		for (Clan c : CLANS) {
			try {
				c.save();
				for (Claim cl : c.getClaims()) {
					cl.save();
				}
			} catch (Exception ignored) {}
		}
		CLANS.clear();
		final File dir = new File(FileManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
		File d = new File(dir.getParentFile().getPath(), ClansAPI.getInstance().getPlugin().getDescription().getName() + "/" + "Clans" + "/");
		if (!d.exists()) {
			d.mkdirs();
		}
		for (File f : d.listFiles()) {
			String id = f.getName().replace(".yml", "");
			Clan c = new OfflineClan(id);
			if (!ClansAPI.getInstance().getClanManager().load(c)) {
				ClansAPI.getInstance().getPlugin().getLogger().warning("- Clan " + '"' + id + '"' + " failed to load properly.");
			}

		}
	}

}
