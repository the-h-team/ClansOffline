package com.github.sanctum.clansoffline.bukkit.event.claim;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.bukkit.event.ClanEvent;
import com.github.sanctum.clansoffline.impl.Resident;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;

public class ClaimResidentEvent extends ClanEvent {

	public static final Set<Resident> RESIDENTS = new HashSet<>();
	private final Map<String, String> context = new HashMap<>();
	private final Player player;
	private final Claim claim;

	public ClaimResidentEvent(Claim claim, Player resident) {
		this.player = resident;
		this.claim = claim;
		Resident res = getResident(resident);
		if (res == null) {
			RESIDENTS.add(new Resident(resident, claim));
		}
		context.put("TITLE", MessageFormat.format(StringLibrary.get("Clans.land-claiming.titles.claim"), claim.getOwner().getName()));
		context.put("SUB-TITLE", MessageFormat.format(StringLibrary.get("Clans.land-claiming.titles.claim-sub"), claim.getOwner().getName()));
	}

	public static Resident getResident(Player player) {
		return RESIDENTS.stream().filter(r -> r.getPlayer().getName().equals(player.getName())).findFirst().orElse(null);
	}

	public void sendNotification() {
		if (LabyrinthProvider.getInstance().isLegacy()) {
			player.sendTitle(MessageFormat.format(StringUtils.use(context.get("TITLE")).translate(player), getClan().getName()), MessageFormat.format(StringUtils.use(context.get("SUB-TITLE")).translate(player), getClan().getName()));
		} else {
			player.sendTitle(MessageFormat.format(StringUtils.use(context.get("TITLE")).translate(player), getClan().getName()), MessageFormat.format(StringUtils.use(context.get("SUB-TITLE")).translate(player), getClan().getName()), 20, 60, 20);
		}
		player.sendMessage(StringUtils.use(MessageFormat.format(StringLibrary.get("Clans.land-claiming.titles.claim-msg"), claim.getOwner().getName())).translate(player));
	}

	public void setClaimTitle(String title, String subTitle) {
		context.put("TITLE", title);
		context.put("SUB-TITLE", subTitle);
	}

	public String getTitle() {
		return context.get("TITLE");
	}

	public String getSubTitle() {
		return context.get("SUB-TITLE");
	}

	public Claim getClaim() {
		return claim;
	}

	public Resident getResident() {
		return getResident(player);
	}

	@Override
	public Clan getClan() {
		return getClaim().getOwner();
	}
}
