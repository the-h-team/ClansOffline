package com.github.sanctum.clansoffline.bukkit.listener;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.text.MessageFormat;
import org.bukkit.event.Listener;

public class BlockEventListener implements Listener {

	@Subscribe(priority = Vent.Priority.HIGH)
	public void onBreak(DefaultEvent.BlockBreak e) {
		Claim test = ClansAPI.getInstance().getClaimManager().get(claim -> claim.getChunk().equals(e.getBlock().getChunk()));
		if (test != null) {
			Clan owner = test.getOwner();
			if (owner.getMembers().stream().noneMatch(m -> m.getName().equals(e.getPlayer().getName()))) {
				Clan.Associate amI = ClansAPI.getInstance().getAssociate(e.getPlayer());
				if (amI != null) {
					if (!amI.getClan().equals(owner)) {
						e.getPlayer().sendMessage(StringUtils.use(MessageFormat.format(StringLibrary.get("Response.land-already-owned"), owner.getName())).translate(e.getPlayer()));
						e.setCancelled(true);
					}
				} else {
					e.getPlayer().sendMessage(StringUtils.use(MessageFormat.format(StringLibrary.get("Response.land-already-owned"), owner.getName())).translate(e.getPlayer()));
					e.setCancelled(true);
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.HIGH)
	public void onPlace(DefaultEvent.BlockPlace e) {
		Claim test = ClansAPI.getInstance().getClaimManager().get(claim -> claim.getChunk().equals(e.getBlock().getChunk()));
		if (test != null) {
			Clan owner = test.getOwner();
			if (owner.getMembers().stream().noneMatch(m -> m.getName().equals(e.getPlayer().getName()))) {
				Clan.Associate amI = ClansAPI.getInstance().getAssociate(e.getPlayer());
				if (amI != null) {
					if (!amI.getClan().equals(owner)) {
						e.getPlayer().sendMessage(StringUtils.use(MessageFormat.format(StringLibrary.get("Response.land-already-owned"), owner.getName())).translate(e.getPlayer()));
						e.setCancelled(true);
					}
				} else {
					e.getPlayer().sendMessage(StringUtils.use(MessageFormat.format(StringLibrary.get("Response.land-already-owned"), owner.getName())).translate(e.getPlayer()));
					e.setCancelled(true);
				}
			}
		}
	}

}
