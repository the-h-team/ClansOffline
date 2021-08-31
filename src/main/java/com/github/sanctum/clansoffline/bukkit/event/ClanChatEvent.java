package com.github.sanctum.clansoffline.bukkit.event;

import com.github.sanctum.clansoffline.api.Clan;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public class ClanChatEvent extends AsyncClanEvent {

	private final Clan.Associate associate;
	private final Set<Player> recipients;
	private String message;
	private BaseComponent component;

	public ClanChatEvent(Clan.Associate associate, String message, Set<Player> recipients) {
		this.associate = associate;
		this.message = message;
		this.recipients = recipients;
	}

	public Set<Player> getRecipients() {
		return recipients;
	}

	public BaseComponent getComponent() {
		return component;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setComponent(BaseComponent component) {
		this.component = component;
	}

	public String getChannel() {
		return getAssociate().getChat();
	}

	public Clan.Associate getAssociate() {
		return associate;
	}
}
