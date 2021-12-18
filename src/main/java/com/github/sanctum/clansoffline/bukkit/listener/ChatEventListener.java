package com.github.sanctum.clansoffline.bukkit.listener;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.bukkit.event.associate.AssociateChatEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {

	@Subscribe
	public void onClanChat(AssociateChatEvent e) {
		switch (e.getChannel().toLowerCase()) {
			case "clan":
				e.setComponents(new TextComponent(MessageFormat.format(StringLibrary.get("Formatting.Chat.Channel.clan"), e.getAssociate().getName(), e.getAssociate().getRank().toFull(), e.getAssociate().getRank().toShort() , e.getMessage())));
				break;
			case "ally":
				e.setComponents(new TextComponent(MessageFormat.format(StringLibrary.get("Formatting.Chat.Channel.ally"), e.getAssociate().getName(), e.getAssociate().getRank().toFull(), e.getAssociate().getRank().toShort() , e.getMessage())));
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
					if (!StringLibrary.isDefaultFormatting()) return;
					String rankRepresentation = associate.getRank().toRank();
					String color = associate.getClan().getColor();
					String name = associate.getClan().getName();
					e.setFormat(StringUtils.use(MessageFormat.format(StringLibrary.get("Formatting.Chat.Channel.global"), rankRepresentation, color + name)).translate(e.getPlayer()) + e.getFormat());
					break;
				case "clan":
					AssociateChatEvent event = new Vent.Call<>(Vent.Runtime.Asynchronous, new AssociateChatEvent(associate, e.getMessage(), associate.getClan().getMembers().stream().filter(m -> m.getPlayer().isPresent() && m.getPlayer().get().isOnline()).map(Clan.Associate::getPlayer).map(Optional::get).map(OfflinePlayer::getPlayer).collect(Collectors.toSet()))).complete().join();
					if (!event.isCancelled()) {
						for (Player p : event.getRecipients()) {
							Message msg = Message.form(p);
							msg.build(event.getComponents());
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
					AssociateChatEvent event2 = new Vent.Call<>(Vent.Runtime.Asynchronous, new AssociateChatEvent(associate, e.getMessage(), players)).complete().join();
					if (!event2.isCancelled()) {
						for (Player p : event2.getRecipients()) {
							Message msg = Message.form(p);
							msg.build(event2.getComponents());
						}
					}
					e.getRecipients().clear();
					e.setCancelled(true);
					break;
			}

		}

	}

}
