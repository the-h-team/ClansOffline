package com.github.sanctum.clansoffline.bukkit.listener;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.bukkit.command.ClanCommand;
import com.github.sanctum.clansoffline.bukkit.event.claim.ClaimResidentEvent;
import com.github.sanctum.clansoffline.bukkit.event.claim.WildernessInhabitantEvent;
import com.github.sanctum.clansoffline.impl.Resident;
import com.github.sanctum.clansoffline.impl.AsynchronousLoanableTask;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.Mailer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PlayerEventListener implements Listener {

	protected static final AsynchronousLoanableTask LOANABLE_TASK = new AsynchronousLoanableTask((p, task) -> {
		task.synchronize(() -> {
			if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
				WildernessInhabitantEvent.removeInhabitant(p);
				Claim claim = ClansAPI.getInstance().getClaimManager().get(c -> c.getChunk().equals(p.getLocation().getChunk()));
				ClaimResidentEvent event = new Vent.Call<>(new ClaimResidentEvent(claim, p)).run();
				if (!event.isCancelled()) {
					Resident r = event.getResident();
					Claim lastKnown = r.getClaim();
					if (claim.isActive()) {
						if (claim.getOwner() == null) {
							ClansAPI.getInstance().getClaimManager().remove(claim);
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
				Resident resident = ClaimResidentEvent.getResident(p);
				if (resident != null) {
					resident.remove();
				}
				WildernessInhabitantEvent event = new Vent.Call<>(new WildernessInhabitantEvent(p)).run();
				if (!WildernessInhabitantEvent.isInhabitant(p)) {
					if (WildernessInhabitantEvent.isFreshie(p)) {
						WildernessInhabitantEvent.removeFreshie(p);
					} else {
						event.sendNotification();
					}
					WildernessInhabitantEvent.addInhabitant(p);
				}
			}
		});
	});

	@Subscribe
	public void onJoin(DefaultEvent.Join e) {
		Player p = e.getPlayer();
		LOANABLE_TASK.join(p);
		WildernessInhabitantEvent.addFreshie(p);
	}

	@Subscribe
	public void onLeave(DefaultEvent.Leave e) {
		Player p = e.getPlayer();
		LOANABLE_TASK.leave(p);
		WildernessInhabitantEvent.removeInhabitant(p);
		WildernessInhabitantEvent.removeFreshie(p);
		ClanCommand.teleportation.remove(p);
	}

	@Subscribe
	public void onAttack(DefaultEvent.PlayerDamagePlayer e) {
		Clan.Associate attacker = ClansAPI.getInstance().getAssociate(e.getPlayer());
		Clan.Associate victim = ClansAPI.getInstance().getAssociate(e.getVictim());
		if (attacker != null) {
			if (victim != null) {
				if (attacker.getClan().getAllies().contains(victim.getClan())) {
					Mailer.empty(e.getPlayer()).prefix().start(ClansAPI.getInstance().getPrefix().getJoined()).finish().chat(StringLibrary.get("Response.cant-hurt-ally")).queue();
					e.setCancelled(true);
				} else if (attacker.getClan().equals(victim.getClan())) {
					if (attacker.getClan().isFriendly()) {
						Mailer.empty(e.getPlayer()).prefix().start(ClansAPI.getInstance().getPrefix().getJoined()).finish().chat(StringLibrary.get("Response.cant-hurt-ally")).queue();
						e.setCancelled(true);
					}
				}
			} else {
				// do stuff
			}
		}
	}

}
