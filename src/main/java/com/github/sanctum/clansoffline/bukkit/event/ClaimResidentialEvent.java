package com.github.sanctum.clansoffline.bukkit.event;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.impl.Resident;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class ClaimResidentialEvent extends ClanEvent {

	public static final Set<Resident> RESIDENTS = new HashSet<>();
	private final Player player;
	private final Claim claim;

	public ClaimResidentialEvent(Claim claim, Player resident) {
		this.player = resident;
		this.claim = claim;
		Resident res = getResident(resident);
		if (res == null) {
			RESIDENTS.add(new Resident(resident, claim));
		}
	}

	public static Resident getResident(Player player) {
		return RESIDENTS.stream().filter(r -> r.getPlayer().getName().equals(player.getName())).findFirst().orElse(null);
	}

	public void sendNotification() {
		player.sendTitle("Now entering " + '"' + getClaim().getOwner().getName() + '"', "Clan owned land.", 20, 60, 20);
	}

	public Claim getClaim() {
		return claim;
	}

	public Resident getResident() {
		return getResident(player);
	}
}
