package com.github.sanctum.clansoffline.impl;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bank.EconomyEntity;
import com.github.sanctum.clansoffline.bank.exceptions.DepositException;
import com.github.sanctum.clansoffline.bank.exceptions.WithdrawalException;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OfflineClan extends Clan {

	private final HUID id;
	private String name;
	private String color;
	private String description;
	private String password;
	private boolean friendly;
	private double powerBonus;
	private double claimBonus;
	private final Set<Associate> associates;
	private final Set<Clan> allies;
	private final Set<Clan> enemies;

	public OfflineClan(@NotNull String id) {

		this.id = HUID.parseID(id).toID();

		ClanDataFile file = new ClanDataFile(new ClanFileManager(id, "Clans"));

		ClanDataContainer container = new ClanDataContainer(new ClanPersistentContainer(id));

		file.setKey(id + ":data");
		container.setKey(id + ":container");

		add(file);
		add(container);

		Set<Associate> associates = new HashSet<>();

		if (file.exists()) {

			String name = file.read(f -> f.getString("name"));

			String description = file.read(f -> f.getString("description"));

			String password = file.read(f -> f.getString("password"));

			this.name = name;

			if (description != null) {
				this.description = description;
			}

			if (password != null) {
				this.password = password;
			}

			for (String rank : file.read(f -> f.getNode("members").getKeys(false))) {
				Rank r = Rank.valueOf(rank);
				for (String m : file.read(f -> f.getStringList("members." + rank))) {
					associates.add(new OfflineAssociate(m, this, r));
				}
			}

			if (file.read(f -> f.isNode("claims"))) {
				for (String claim : file.read(f -> f.getNode("claims").getKeys(false))) {
					LocationStorage storage = file.getLocationStorage("claims." + claim + ".storage");
					Claim c = new OfflineClaim(HUID.fromString(claim), storage.getStorage(), HUID.fromString(id));
					c.setActive(true);
					ClansAPI.getInstance().getClaimManager().load(c);
				}
			}
			LocationStorage b = file.getLocationStorage("base");
			if (b != null) {
				ClanBase base = new ClanBase(this, b);
				base.setKey(id + ":base");
				add(base);
			}

			file.read(f -> {
				if (f.isString("color")) {
					this.color = f.getString("color");
				}
				return "boop";

			});

		}

		if (description == null) {
			this.description = "I have no description";
		}
		if (color == null) color = "&f";
		this.powerBonus = file.read(f -> f.getDouble("power-bonus"));
		this.claimBonus = file.read(f -> f.getDouble("claim-bonus"));
		this.associates = associates;
		this.allies = new HashSet<>();
		this.enemies = new HashSet<>();
		if (!getContainer().exists("ally_requests")) {
			getContainer().attach("ally_requests", new ClanAllyRequests());
		}
	}

	@NotNull
	@Override
	public HUID getId() {
		return this.id;
	}

	@NotNull
	@Override
	public String getName() {
		return this.name;
	}

	@NotNull
	@Override
	public String getColor() {
		return this.color;
	}

	@NotNull
	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public double getPower() {
		double result = 0.0;
		double multiplier = 1.4;
		double add = getMembers().size() + 0.56;
		int claimAmount = getClaims().size();
		result = result + add + (claimAmount * multiplier);
		double bonus = this.powerBonus;
		if (ClansAPI.getInstance().getMain().read(c -> c.getBoolean("Clans.banks.influence"))) {
			if (EconomyProvision.getInstance().isValid()) {
				double bal = getBalance().doubleValue();
				if (bal != 0) {
					bonus += bal / 48.94;
				}
			}
		}
		return result + bonus;
	}

	@Override
	public int getMaxClaims() {
		if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.land-claiming.claim-influence.allow"))) {
			return 0;
		}
		if (ClansAPI.getInstance().getMain().read(f -> f.getString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW"))) {
			this.claimBonus += 13.33;
		}
		return (int) ((getMembers().size() + Math.cbrt(getPower())) + this.claimBonus);
	}

	@Nullable
	@Override
	public Associate getOwner(Predicate<Associate> predicate) {
		for (Associate a : getMembers().stream().filter(m -> m.getRank().getLevel() == 3).collect(Collectors.toSet())) {
			if (predicate.test(a)) {
				return a;
			}
		}
		return null;
	}

	@NotNull
	@Override
	public ClanBase getBase() {
		ClanBase base = get(c -> c instanceof ClanBase && c.getKey() != null && c.getKey().equals(id + ":base"));
		if (base == null) {
			ClanBase copy = new ClanBase(this, (Location) null);
			copy.setKey(id + ":base");
			return add(copy);
		}
		return base;
	}

	@NotNull
	@Override
	public Set<Associate> getMembers() {
		return this.associates;
	}

	@NotNull
	@Override
	public Set<Clan> getAllies() {
		return this.allies;
	}

	@NotNull
	@Override
	public Set<Clan> getEnemies() {
		return this.enemies;
	}

	@NotNull
	@Override
	public Set<Claim> getClaims() {
		return ClansAPI.getInstance().getClaimManager().getClaims().filter(c -> c.getOwner().equals(this)).collect(Collectors.toSet());
	}

	@NotNull
	@Override
	public synchronized ClanDataContainer getContainer() {
		ClanDataContainer container = get(c -> c instanceof ClanDataContainer && c.getKey() != null && c.getKey().equals(id + ":container"));
		assert container != null;
		return container;
	}

	@NotNull
	@Override
	public ClanAllyRequests getRequests() {
		return getContainer().get(ClanAllyRequests.class, "ally_requests");
	}

	@NotNull
	@Override
	public synchronized ClanDataFile getData() {
		ClanDataFile file = get(c -> c instanceof ClanDataFile && c.getKey() != null && c.getKey().equals(id + ":data"));
		assert file != null;
		return file;
	}

	@Override
	public boolean remove(Predicate<Associate> predicate) {
		for (Associate mem : getMembers()) {
			if (predicate.test(mem)) {
				remove(mem);
			}
		}
		return false;
	}

	@Override
	public boolean remove(Associate associate) {
		if (associate == null) return false;
		if (!associate.getClan().equals(this)) {
			return false;
		}
		if (associate.getRank().getLevel() == 3) {
			broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.clan-disband"), associate.getClan().getName(), associate.getName()));
			Schedule.sync(() -> {
				Schedule.sync(() -> ClansAPI.getInstance().getClanManager().remove(OfflineClan.this)).run();
			}).waitReal(2);
		}
		Schedule.sync(() -> getMembers().remove(associate)).run();
		return true;
	}

	@Override
	public boolean isLocked() {
		return getPassword() != null;
	}

	@Override
	public boolean isFriendly() {
		return this.friendly;
	}

	@Override
	public void broadcast(BaseComponent... components) {
		for (Associate ass : getMembers()) {
			ass.getPlayer().ifPresent(player -> {
				if (player.isOnline()) {
					player.getPlayer().spigot().sendMessage(components);
				}
			});
		}
	}

	@Override
	public void broadcast(Predicate<Associate> predicate, String message) {
		for (Associate ass : getMembers()) {
			if (predicate.test(ass)) {
				ass.getPlayer().ifPresent(p -> {
					if (p.isOnline()) {
						p.getPlayer().sendMessage(StringUtils.use(message).translate());
					}
				});
			}
		}
	}

	@Override
	public void setOwner(String newOwner) {
		Associate owner = getOwner(m -> true);
		if (owner != null) {
			owner.setRank(Rank.NORMAL);
		}
		Associate test = getMembers().stream().filter(m -> m.getId().equals(newOwner)).findFirst().orElse(null);
		if (test != null) {
			test.setRank(Rank.HIGHEST);
		} else {
			getMembers().add(new OfflineAssociate(newOwner, this, Rank.HIGHEST));
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public void setFriendly(boolean friendly) {
		this.friendly = friendly;
	}

	@Override
	public void addPower(double amount) {
		this.powerBonus += amount;
	}

	@Override
	public void takePower(double amount) {
		this.powerBonus -= amount;
	}

	@Override
	public void addClaim(double amount) {
		this.claimBonus += amount;
	}

	@Override
	public void takeClaim(double amount) {
		this.claimBonus -= amount;
	}

	// below for OfflineBank/EconomyEntity contract

	@Override
	public boolean hasAmount(@NotNull BigDecimal amount) {
		return ClansAPI.getInstance().getBankManager().getBalance(id).compareTo(amount) >= 0;
	}

	@Override
	public void takeAmount(@NotNull BigDecimal amount) throws WithdrawalException {
		if (!hasAmount(amount)) throw new WithdrawalException(amount, getName() + " does not have enough funds to withdraw " + amount);
		ClansAPI.getInstance().getBankManager().adjustBalance(id, amount.negate());
	}

	@Override
	public void giveAmount(@NotNull BigDecimal amount) {
		ClansAPI.getInstance().getBankManager().adjustBalance(id, amount);
	}

	@Override
	public @NotNull BigDecimal getBalance() {
		return ClansAPI.getInstance().getBankManager().getBalance(id);
	}

	@Override
	public void depositFrom(@NotNull EconomyEntity from, @NotNull BigDecimal amount) throws WithdrawalException {
		// Try to take amount from provided entity
		from.takeAmount(amount);
		// Give amount to the bank
		giveAmount(amount);
	}

	@Override
	public void withdrawTo(@NotNull EconomyEntity to, @NotNull BigDecimal amount) throws WithdrawalException, DepositException {
		// Try to take amount from the bank
		takeAmount(amount);
		// Try to give amount to provided entity
		try {
			to.giveAmount(amount);
		} catch (DepositException e) {
			// reverse take amount
			giveAmount(amount);
			// rethrow
			throw e;
		}
	}
}
