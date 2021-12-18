package com.github.sanctum.clansoffline.bukkit.event.associate;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.bukkit.event.ClanEvent;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public class AssociateChatEvent extends ClanEvent {

	private final Clan.Associate associate;
	private final Set<Player> recipients;
	private String message;
	private BaseComponent[] component;

	public AssociateChatEvent(Clan.Associate associate, String message, Set<Player> recipients) {
		super(true);
		this.associate = associate;
		this.message = message;
		this.recipients = recipients;
	}

	public Set<Player> getRecipients() {
		return recipients;
	}

	public BaseComponent[] getComponents() {
		return component;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setComponents(BaseComponent... component) {
		this.component = component;
	}

	public String getChannel() {
		return getAssociate().getChat();
	}

	public Clan.Associate getAssociate() {
		return associate;
	}

	@Override
	public Clan getClan() {
		return getAssociate().getClan();
	}
}
