package com.github.sanctum.clansoffline.bukkit.listener;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.bukkit.event.ClanChatEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

	@Subscribe
	public void onClanChat(ClanChatEvent e) {
		switch (e.getChannel().toLowerCase()) {
			case "clan":
				TextLib.consume(t -> e.setComponent(t.textHoverable("&8&l(&2CC&8&l) ", e.getAssociate().getId(), "&f: " + e.getMessage(), "&6My rank in the clan is " + e.getAssociate().getRank().name())));
				break;
			case "ally":
				TextLib.consume(t -> e.setComponent(t.textHoverable("&8&l(&a&lAC&8&l) ", e.getAssociate().getId(), "&f: " + e.getMessage(), "&6My rank in the clan is " + e.getAssociate().getRank().name())));
				break;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {

		Clan.Associate associate = ClansAPI.getInstance().getAssociate(e.getPlayer());

		if (associate != null) {

			String chat = associate.getChat();

			switch (chat.toLowerCase()) {
				case "global":
					String test = "";
					switch (StringLibrary.get("Formatting.Style").toLowerCase()) {
						case "wordless":
							switch (associate.getRank()) {
								case HIGH:
									test = StringLibrary.get("Formatting.Ranks.HIGH.Wordless");
									break;
								case HIGHER:
									test = StringLibrary.get("Formatting.Ranks.HIGHER.Wordless");
									break;
								case NORMAL:
									test = StringLibrary.get("Formatting.Ranks.NORMAL.Wordless");
									break;
								case HIGHEST:
									test = StringLibrary.get("Formatting.Ranks.HIGHEST.Wordless");
									break;
							}
							break;
						case "full":
							switch (associate.getRank()) {
								case HIGH:
									test = StringLibrary.get("Formatting.Ranks.HIGH.Full");
									break;
								case HIGHER:
									test = StringLibrary.get("Formatting.Ranks.HIGHER.Full");
									break;
								case NORMAL:
									test = StringLibrary.get("Formatting.Ranks.NORMAL.Full");
									break;
								case HIGHEST:
									test = StringLibrary.get("Formatting.Ranks.HIGHEST.Full");
									break;
							}
							break;
					}
					e.setFormat(StringUtils.use("[" + test + "][" + associate.getClan().getColor() + associate.getClan().getName() + "]").translate() + e.getFormat());
					break;
				case "clan":
					ClanChatEvent event = new Vent.Call<>(Vent.Runtime.Asynchronous, new ClanChatEvent(associate, e.getMessage(), associate.getClan().getMembers().stream().filter(m -> m.getPlayer().isPresent() && m.getPlayer().get().isOnline()).map(Clan.Associate::getPlayer).map(Optional::get).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()))).complete().join();
					if (!event.isCancelled()) {
						for (Player p : event.getRecipients()) {
							Message msg = Message.form(p);
							msg.build(event.getComponent());
						}
					}
					e.getRecipients().clear();
					e.setCancelled(true);
					break;
				case "ally":
					Set<Player> players = new HashSet<>(associate.getClan().getMembers().stream().filter(m -> m.getPlayer().isPresent() && m.getPlayer().get().isOnline()).map(Clan.Associate::getPlayer).map(Optional::get).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()));
					for (Clan c : associate.getClan().getAllies()) {
						for (Clan.Associate ass : c.getMembers()) {
							ass.getPlayer().ifPresent(p -> {
								if (p.isOnline()) {
									players.add(p.getPlayer());
								}
							});
						}
					}
					ClanChatEvent event2 = new Vent.Call<>(Vent.Runtime.Asynchronous, new ClanChatEvent(associate, e.getMessage(), players)).complete().join();
					if (!event2.isCancelled()) {
						for (Player p : event2.getRecipients()) {
							Message msg = Message.form(p);
							msg.build(event2.getComponent());
						}
					}
					e.getRecipients().clear();
					e.setCancelled(true);
					break;
			}

		}

	}

}
