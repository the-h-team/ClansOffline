package com.github.sanctum.clansoffline.bukkit.event.claim;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.bukkit.event.ClanEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;

public class WildernessInhabitantEvent extends ClanEvent {

	private static final Set<Player> inhabitants = new HashSet<>();
	private static final Set<Player> freshies = new HashSet<>();
	private final Map<String, String> context = new HashMap<>();
	private final Player target;

	public WildernessInhabitantEvent(Player target) {
		this.target = target;
		context.put("TITLE", MessageFormat.format(StringLibrary.get("Clans.land-claiming.titles.wilderness"), target.getName()));
		context.put("SUB-TITLE", MessageFormat.format(StringLibrary.get("Clans.land-claiming.titles.wilderness-sub"), target.getName()));
	}

	public void sendNotification() {
		if (LabyrinthProvider.getInstance().isLegacy()) {
			target.sendTitle(StringUtils.use(context.get("TITLE")).translate(target), StringUtils.use(context.get("SUB-TITLE")).translate(target));
		} else {
			target.sendTitle(StringUtils.use(context.get("TITLE")).translate(target), StringUtils.use(context.get("SUB-TITLE")).translate(target), 20, 60, 20);
		}
		target.sendMessage(StringUtils.use(MessageFormat.format(StringLibrary.get("Clans.land-claiming.titles.wilderness-msg"), target.getName())).translate(target));
	}

	public Player getInhabitant() {
		return target;
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public static boolean isInhabitant(Player player) {
		return inhabitants.contains(player);
	}

	public static boolean isFreshie(Player player) {
		return freshies.contains(player);
	}

	public static void addFreshie(Player freshie) {
		if (!isFreshie(freshie)) {
			freshies.add(freshie);
		}
	}

	public static void removeFreshie(Player freshie) {
		if (isFreshie(freshie)) {
			freshies.remove(freshie);
		}
	}

	public static void addInhabitant(Player player) {
		if (!isInhabitant(player)) {
			inhabitants.add(player);
		}
	}

	public static void removeInhabitant(Player player) {
		if (isInhabitant(player)) {
			inhabitants.remove(player);
		}
	}

}
