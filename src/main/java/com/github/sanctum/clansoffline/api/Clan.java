package com.github.sanctum.clansoffline.api;

import com.github.sanctum.clansoffline.bank.OfflineBank;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.impl.ClanAllyRequests;
import com.github.sanctum.clansoffline.impl.ClanBase;
import com.github.sanctum.clansoffline.impl.ClanDataContainer;
import com.github.sanctum.clansoffline.impl.ClanDataFile;
import com.github.sanctum.clansoffline.impl.EmptyComponent;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.skulls.CustomHead;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Clan implements OfflineBank {

	protected final Set<Component<?>> components = new HashSet<>();

	public @NotNull abstract HUID getId();

	public @NotNull abstract String getName();

	public @NotNull abstract String getColor();

	public @NotNull abstract String getDescription();

	public @Nullable abstract String getPassword();

	public abstract double getPower();

	public abstract int getMaxClaims();

	public @Nullable abstract Associate getOwner(Predicate<Associate> predicate);

	public @NotNull abstract ClanBase getBase();

	public @NotNull abstract Set<Associate> getMembers();

	public @NotNull abstract Set<Clan> getAllies();

	public @NotNull abstract Set<Clan> getEnemies();

	public @NotNull abstract Set<Claim> getClaims();

	public @NotNull abstract ClanDataContainer getContainer();

	public @NotNull abstract ClanAllyRequests getRequests();

	public @NotNull	abstract ClanDataFile getData();

	public abstract boolean remove(Predicate<Associate> predicate);

	public abstract boolean remove(Associate associate);

	public abstract boolean isLocked();

	public abstract boolean isFriendly();

	public abstract void broadcast(BaseComponent... components);

	public abstract void broadcast(Predicate<Associate> predicate, String text);

	public abstract void setOwner(String newOwner);

	public abstract void setName(String name);

	public abstract void setPassword(String password);

	public abstract void setDescription(String description);

	public abstract void setColor(String color);

	public abstract void setFriendly(boolean friendly);

	public abstract void addPower(double amount);

	public abstract void takePower(double amount);

	public abstract void addClaim(double amount);

	public abstract void takeClaim(double amount);

	public final void save() {

		ClanDataFile data = getData();

		data.set("name", getName());

		data.set("description", getDescription());

		data.set("friendly", isFriendly());

		if (isLocked()) {
			data.set("password", getPassword());
		}

		ClanBase base = getBase();

		if (base.getComponent().getStorage() != null) {
			data.set("base", base.getComponent());
		}

		Map<Rank, List<Associate>> associates = new HashMap<>();

		for (Rank r : Rank.values()) {
			associates.put(r, new ArrayList<>());
		}

		for (Associate member : getMembers()) {
			Rank rank = member.getRank();
			List<Associate> ass = associates.get(rank);
			ass.add(member);
			associates.put(rank, ass);
		}

		for (Map.Entry<Rank, List<Associate>> entry : associates.entrySet()) {
			data.set("members." + entry.getKey().name(), entry.getValue().stream().map(Associate::getId).collect(Collectors.toList()));
		}


	}

	public final @NotNull
	<T extends Storage, R extends Component<T>> R add(@NotNull R component) {
		this.components.add(component);
		return component;
	}

	public final @Nullable
	<R extends Component<?>> R get(Predicate<Component<?>> predicate) {
		for (Component<?> c : this.components) {
			if (predicate.test(c)) {
				return (R) c;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Clan)) return false;
		Clan clan = (Clan) o;
		return getId().equals(clan.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId().toString());
	}

	public interface Storage extends Serializable {

		default Object getStorage() {
			return "Absent==[{NO VALUES}]";
		}

	}


	public abstract static class Component<T extends Storage> {

		protected T t;

		protected String key;

		public Component() {
			this.t = (T) new EmptyComponent();
		}

		public Component(T t) {
			this.t = t;
		}

		public @Nullable String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public T getComponent() {
			return t;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Component)) return false;
			Component<?> component = (Component<?>) o;
			return Objects.equals(t, component.t) &&
					Objects.equals(getKey(), component.getKey());
		}

		@Override
		public int hashCode() {
			return Objects.hash(t, getKey());
		}
	}

	public abstract static class Associate {

		private final String id;
		private final String name;

		private final Clan clan;

		private Rank rank;

		private String chat;

		private final ItemStack head;

		public Associate(@NotNull String id, @NotNull Clan c, Rank rank) {
			this.id = id;
			this.rank = rank;
			this.clan = c;
			if (getUUID().isPresent()) {
				this.head = CustomHead.Manager.get(getUUID().get());
			} else {
				this.head = CustomHead.Manager.get(id);
			}
			this.chat = "GLOBAL";
			if (!getUUID().isPresent()) {
				this.name = id;

			} else {
				this.name = Bukkit.getOfflinePlayer(getUUID().get()).getName();
			}
		}

		public String getName() {
			return this.name;
		}

		public ItemStack getHead() {
			return head;
		}

		public Clan getClan() {
			return clan;
		}

		public void setRank(Rank rank) {
			this.rank = rank;
		}

		public Rank getRank() {
			return this.rank;
		}

		@Note("This could return either a uuid or user-name!")
		public String getId() {
			return id;
		}

		public String getChat() {
			return chat;
		}

		public void setChat(String chat) {
			this.chat = chat;
		}

		public final Optional<OfflinePlayer> getPlayer() {
			if (getUUID().isPresent()) return Optional.of(Bukkit.getOfflinePlayer(getUUID().get()));
			return Arrays.stream(Bukkit.getOfflinePlayers()).filter(o -> Objects.equals(o.getName(), this.id)).findFirst();
		}

		public final Optional<UUID> getUUID() {
			try {
				UUID id = UUID.fromString(this.id);
				return Optional.of(id);
			} catch (Exception ignored) {
			}
			return Optional.empty();
		}

	}

	public enum Rank {
		NORMAL(0),
		HIGH(1),
		HIGHER(2),
		HIGHEST(3);

		private final int level;

		Rank(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}

		public String toShort() {
			String test = StringLibrary.get("Formatting.Ranks.NORMAL.Full");
			switch (this) {
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
			return test;
		}

		public String toFull() {
			String test = StringLibrary.get("Formatting.Ranks.NORMAL.Full");
			switch (this) {
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
			return test;
		}

		public String toRank() {
			String test = StringLibrary.get("Formatting.Ranks.NORMAL.Full");
			switch (StringLibrary.get("Formatting.Style").toLowerCase()) {
				case "wordless":
					switch (this) {
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
					switch (this) {
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
			return test;
		}

	}

}
