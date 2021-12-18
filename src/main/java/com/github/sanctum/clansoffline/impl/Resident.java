package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.bukkit.event.claim.ClaimResidentEvent;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class Resident {

	private final Player player;
	private Claim c;
	private long time;
	private final Set<Property> properties = new HashSet<>();


	public Resident(Player player, Claim c) {
		this.player = player;
		this.time = System.currentTimeMillis();
		this.c = c;
	}

	public Claim getClaim() {
		return c;
	}

	public void update(Claim claim) {
		this.c = claim;
	}

	public void update(long time) {
		this.time = time;
	}

	public TimeWatch.Recording getRecording() {
		return TimeWatch.Recording.subtract(this.time);
	}

	public Player getPlayer() {
		return player;
	}

	public void setProperty(Property property, boolean state) {
		if (hasProperty(property)) {
			if (!state) {
				this.properties.remove(property);
			}
		} else {
			if (state) {
				this.properties.add(property);
			}
		}
	}


	public boolean hasProperty(Property property) {
		return properties.contains(property);
	}

	public void remove() {
		Schedule.sync(() -> ClaimResidentEvent.RESIDENTS.remove(this)).run();
	}

	public enum Property {
		TRAVERSED, NOTIFIED
	}

}
