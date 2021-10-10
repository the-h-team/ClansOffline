package com.github.sanctum.clansoffline.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.event.EasyListener;
import com.github.sanctum.labyrinth.event.custom.Vent;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClanAddon {

	private boolean registered;

	public abstract String getName();

	public abstract String[] getAuthors();

	public abstract String getVersion();

	public abstract void onLoad();

	public abstract void onEnable();

	public abstract void onDisable();

	public final boolean isRegistered() {
		return this.registered;
	}

	public final Plugin getPlugin() {
		return ClansAPI.getInstance().getPlugin();
	}

	public final void register() {
		if (!registered) {
			registered = true;
			onLoad();
			ClansAPI.getInstance().getAddonManager().load(this);
		}
	}

	public final void remove() {

	}

	public final void register(Listener listener) {
		new EasyListener(listener).call(getPlugin());
	}

	public final void register(Vent.Subscription<?> subscription) {
		LabyrinthProvider.getService(Service.VENT).subscribe(subscription);
	}

	public final @NotNull FileManager getFile(@NotNull String name, @Nullable String directory) {
		return FileList.search(getPlugin()).get(name, directory);
	}

}
