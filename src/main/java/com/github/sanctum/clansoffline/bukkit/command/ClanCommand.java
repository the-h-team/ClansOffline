package com.github.sanctum.clansoffline.bukkit.command;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bank.entities.EconomyProvisionPlayerEconomyEntity;
import com.github.sanctum.clansoffline.bank.exceptions.DepositException;
import com.github.sanctum.clansoffline.bank.exceptions.WithdrawalException;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.impl.ClanAllyRequests;
import com.github.sanctum.clansoffline.impl.ClanBase;
import com.github.sanctum.clansoffline.impl.OfflineAssociate;
import com.github.sanctum.clansoffline.impl.OfflineClaim;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.pagination.AbstractPaginatedCollection;
import com.github.sanctum.labyrinth.formatting.pagination.Page;
import com.github.sanctum.labyrinth.interfacing.EqualsOperator;
import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.MathUtils;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.TaskChain;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanCommand extends Command implements EqualsOperator {

	public static final Set<Player> teleportation = new HashSet<>();
	private final AbstractPaginatedCollection<String> collection = AbstractPaginatedCollection.of("&7|&2) &6/" + "{0}" + " &rcreate <name> | [password]",
			"&7|&2) &6/" + "{0}" + " &rleave",
			"&7|&2) &6/" + "{0}" + " &rjoin <name> | [password]",
			"&7|&2) &6/" + "{0}" + " &rchat",
			"&7|&2) &6/" + "{0}" + " &rinfo | [player, clanname]",
			"&7|&2) &6/" + "{0}" + " &rbank",
			"&7|&2) &6/" + "{0}" + " &rdeposit <amount>",
			"&7|&2) &6/" + "{0}" + " &rwithdraw <amount>",
			"&7|&2) &6/" + "{0}" + " &rclaim",
			"&7|&2) &6/" + "{0}" + " &runclaim | [all]",
			"&7|&2) &6/" + "{0}" + " &rbase",
			"&7|&2) &6/" + "{0}" + " &rsetbase",
			"&7|&2) &6/" + "{0}" + " &rtag <newTag>",
			"&7|&2) &6/" + "{0}" + " &rcolor <newColor>",
			"&7|&2) &6/" + "{0}" + " &rally <clanName>",
			"&7|&2) &6/" + "{0}" + " &renemy <clanName>",
			"&7|&2) &6/" + "{0}" + " &rtruce <clanName>",
			"&7|&2) &6/" + "{0}" + " &c&mpermissions &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mwar &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mforfeit,surrender &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mtruce &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mmode &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mmessage &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mplayers &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mbio &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mlogo &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mdisplay &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mmail &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mgift &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mflags &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mterritory &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mbounty &r(&3Pro&r)",
			"&7|&2) &6/" + "{0}" + " &c&mmap &r(&3Pro&r)").limit(5);

	public ClanCommand() {
		super("clan");
		setAliases(Arrays.asList("c", "cl"));
		setDescription("The primary alias to clan commands.");
		setPermission("clans.use");
	}

	public boolean console(@NotNull CommandSender c, @NotNull String label, @NotNull String[] args) {


		return true;
	}

	public boolean player(@NotNull Player p, @NotNull String label, @NotNull String[] args) {

		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p);
		Message msg = Message.form(p).setPrefix(ClansAPI.getInstance().getPrefix().getJoined());

		if (StringLibrary.getList("Clans.list.worlds").stream().anyMatch(s -> p.getWorld().getName().equals(s))) {
			msg.send("&cClan features are disabled for this world.");
			return true;
		}

		if (args.length == 0) {

			Page<String> page1 = collection.get(1);
			msg.send("- Command help (&7/clan #&r)");
			msg.setPrefix(null);
			msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			for (String content : page1) {
				msg.send(content.replace("{0}", label));
			}
			msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

			return true;
		}

		if (args.length == 1) {

			if (equals(args[0], "create")) {
				if (associate != null) {
					msg.send(StringLibrary.get("Response.already-in-clan"));
				} else {
					msg.send(StringLibrary.get("Response.invalid-name"));
				}
				return true;
			}

			if (equals(args[0], "leave")) {
				if (associate != null) {
					if (associate.getRank().getLevel() == 3) {
						Bukkit.broadcastMessage(StringUtils.use(MessageFormat.format(StringLibrary.get("Response.clan-disband"), associate.getClan().getName())).translate());
					} else {
						associate.getClan().broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.member-leave"), p.getName()));
					}
					associate.getClan().remove(associate);
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "join")) {
				msg.send("&cInvalid usage, expected name");
				return true;
			}

			if (equals(args[0], "chat")) {
				if (associate != null) {
					if (associate.getChat().equalsIgnoreCase("global")) {
						associate.setChat("CLAN");
						msg.send("&aNow in clan chat!");
						return true;
					}
					if (associate.getChat().equalsIgnoreCase("CLAN")) {
						associate.setChat("ALLY");
						msg.send("&aNow in clan ally chat!");
						return true;
					}
					if (associate.getChat().equalsIgnoreCase("ALLY")) {
						associate.setChat("GLOBAL");
						msg.send("&aNow in global chat!");
						return true;
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "unclaim")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= StringLibrary.getClearance("un-claim")) {

						if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
							Claim c = ClansAPI.getInstance().getClaimManager().get(cl -> cl.getChunk().equals(p.getLocation().getChunk()));
							if (c.getOwner().equals(associate.getClan())) {
								associate.getClan().broadcast(a -> true, MessageFormat.format(StringLibrary.get("Response.clan-unclaim"), c.getChunk().getX(), c.getChunk().getZ(), c.getChunk().getWorld().getName()));
								ClansAPI.getInstance().getClaimManager().remove(c);
							} else {
								// we dont own it. Test settings for raid shield then try to unclaim
								if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
									if (associate.getClan().getPower() > c.getOwner().getPower()) {
										c.getOwner().broadcast(associate1 -> true, MessageFormat.format(StringLibrary.get("Response.clan-breach"), c.getChunk().getX(), c.getChunk().getZ(), c.getChunk().getWorld().getName()));
										msg.send(MessageFormat.format(StringLibrary.get("Response.clan-unclaim"), c.getChunk().getX(), c.getChunk().getZ(), c.getChunk().getWorld().getName()));
										ClansAPI.getInstance().getClaimManager().remove(c);
									} else {
										msg.send("&cWe aren't powerful enough to take this land!");
									}
								} else {
									msg.send("&5The shield is persistent... try again later.");
									p.playSound(p.getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 10, 2);
								}
							}
						} else {
							msg.send(StringLibrary.get("Response.not-in-claim"));
						}

					} else {
						msg.send(StringLibrary.get("Response.no-clearance"));
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "claim")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= StringLibrary.getClearance("claim")) {

						if (!ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
							if (associate.getClan().getClaims().size() >= associate.getClan().getMaxClaims()) {
								msg.send(StringLibrary.get("Response.claim-limit-reached"));
								return true;
							}
							// test amount
							Claim claim = new OfflineClaim(HUID.randomID(), p.getLocation(), associate.getClan().getId());
							claim.setActive(true);
							ClansAPI.getInstance().getClaimManager().load(claim);
							associate.getClan().broadcast(a -> true, MessageFormat.format(StringLibrary.get("Response.clan-claim"), p.getLocation().getChunk().getX(), p.getLocation().getChunk().getZ()));
						} else {
							Claim c = ClansAPI.getInstance().getClaimManager().get(cl -> cl.getChunk().equals(p.getLocation().getChunk()));
							if (c.getOwner().equals(associate.getClan())) {
								msg.send(StringLibrary.get("Response.land-self-owned"));
							} else {
								// test other clan power
								msg.send(MessageFormat.format(StringLibrary.get("Response.land-already-owned"), c.getOwner().getName()));
							}
						}

					} else {
						msg.send(StringLibrary.get("Response.no-clearance"));
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "info", "i")) {
				if (associate != null) {
					// TODO: send info
					msg.send(associate.getClan().getColor() + associate.getClan().getName());
					msg.setPrefix(null);
					Clan c = associate.getClan();
					msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					msg.send("&6Power: &f" + c.getPower());
					if (c.getBase().getComponent().getStorage() != null) {
						msg.build(new FancyMessage("&6Base: &f").then("(").then("Teleport").hover("Click to teleport").action(() -> {
							if (teleportation.contains(p)) {
								msg.send("&cYou are already teleporting!");
								return;
							}
							teleportation.add(p);
							TaskChain chain = LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS);
							msg.send(StringLibrary.get("Response.teleporting"));
							chain.wait(() -> {
								p.teleport(c.getBase().getComponent().getStorage());
								teleportation.remove(p);
							}, "teleport:" + p.getUniqueId(), TimeUnit.SECONDS.toMillis(10));
						}).color(ChatColor.DARK_AQUA).then(")").build());
					} else {
						msg.send("&6Base: &cNot set");
					}
					double bal = ClansAPI.getInstance().getBankManager().getBalance(c.getId()).doubleValue();
					msg.send("&6Bank: &f" + NumberFormat.getCurrencyInstance().format(bal));
					msg.send("&6Claims: &f" + c.getClaims().size() + "/" + c.getMaxClaims());
					msg.send("&6Allies: &f" + (c.getAllies().isEmpty() ? "&cNone" : c.getAllies().stream().map(Clan::getName).collect(Collectors.joining(", "))));
					msg.send("&6Enemies: &f" + (c.getEnemies().isEmpty() ? "&cNone" : c.getEnemies().stream().map(Clan::getName).collect(Collectors.joining(", "))));
					msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					AbstractPaginatedCollection<Clan.Associate> associates = AbstractPaginatedCollection.of(c.getMembers()).limit(c.getMembers().size()).sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getPlayer().get().getName(), o2.getPlayer().get().getName()));
					for (Clan.Associate ass : associates.get(1)) {
						msg.send("- &b" + ass.getName() + "&7 Lvl." + ass.getRank().getLevel());
					}
					msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "setbase")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= StringLibrary.getClearance("setbase")) {
						associate.getClan().getBase().update(p.getLocation());
						msg.send(StringLibrary.get("Response.base-set"));
					} else {
						msg.send(StringLibrary.get("Response.no-clearance"));
					}

				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "base")) {
				if (associate != null) {
					ClanBase base = associate.getClan().getBase();
					if (base.getComponent().getStorage() != null) {
						if (teleportation.contains(p)) {
							msg.send("&cYou are already teleporting!");
							return true;
						}
						teleportation.add(p);
						TaskChain chain = LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS);
						msg.send(StringLibrary.get("Response.teleporting"));
						chain.wait(() -> {
							p.teleport(base.getComponent().getStorage());
							teleportation.remove(p);
						}, "teleport:" + p.getUniqueId(), TimeUnit.SECONDS.toMillis(10));
					} else {
						msg.send(StringLibrary.get("Response.no-base"));
					}

				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}

				return true;
			}

			// begin bank section
			if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate != null) {
					msg.send("&aWelcome to the clan bank.");
					msg.send("The clan balance seems to be " + formatCurrency(associate.getClan().getBalance()));
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			} else if (equals(args[0], "deposit", "withdraw")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate != null) {
					msg.send("&aValid syntax:\n &b/clan &7<&edeposit&7|&ewithdraw&7> &7<&aamount&7>");
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			// end bank section
			try {
				int page = Integer.parseInt(args[0]);
				if (page > collection.size() || page < 1) {
					msg.send("&cThis page doesn't exist!");
					return true;
				}
				Page<String> page1 = collection.get(page);
				msg.send("- Command help (&7/clan #&r)");
				msg.setPrefix(null);
				msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				for (String content : page1) {
					msg.send(content.replace("{0}", label));
				}
				msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			} catch (NumberFormatException fail) {
				msg.send("&cInvalid page number.");
			}
			return true;
		}

		if (args.length == 2) {
			String i = args[1];
			if (equals(args[0], "color")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= 1) {

						if (!i.matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !i.matches("(&[a-zA-Z0-9])+") && !i.matches("(&#[a-zA-Z0-9])+") && !i.matches("(#[a-zA-Z0-9])+")) {
							msg.send("&c&oInvalid color format.");
							return true;
						}

						associate.getClan().setColor(i);
						msg.send("&eClan color changed to " + i + "TEST");

					} else {
						msg.send(StringLibrary.get("Response.no-clearance"));
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}
			if (equals(args[0], "unclaim") && equals(i, "all")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= StringLibrary.getClearance("un-claim-all")) {

						for (Claim c : associate.getClan().getClaims()) {
							Schedule.sync(() -> ClansAPI.getInstance().getClaimManager().remove(c)).run();
						}
						associate.getClan().broadcast(a -> true, StringLibrary.get("Response.clan-unclaim-all"));

					} else {
						msg.send(StringLibrary.get("Response.no-clearance"));
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}
			if (equals(args[0], "join")) {
				if (ClansAPI.getInstance().getAssociate(p) == null) {
					Clan test = ClansAPI.getInstance().getClan(i);
					if (test != null) {
						if (test.getPassword() != null) {
							msg.send("&cThis clan is locked and requires a password to join.");
							return true;
						}
						test.getMembers().add(new OfflineAssociate(p.getName(), test, Clan.Rank.NORMAL));
						test.broadcast(associate1 -> true, StringUtils.use(MessageFormat.format(StringLibrary.get("Response.member-join"), p.getName())).translate(p));
					} else {
						msg.send(MessageFormat.format(StringLibrary.get("Response.invalid-name"), i));
					}
				} else {
					msg.send(StringLibrary.get("Response.already-in-clan"));
				}
				return true;
			}
			if (equals(args[0], "create")) {
				if (associate != null) {
					msg.send(StringLibrary.get("Response.already-in-clan"));
				} else {
					if (i.length() >= StringLibrary.getTagLimit()) {
						msg.send(MessageFormat.format(StringLibrary.get("Response.tag-too-long"), StringLibrary.getTagLimit()));
						return true;
					}
					Clan c = ClansAPI.getInstance().getClanManager().generateClan(p, i);
					// TODO: announce creation
					msg.send(MessageFormat.format(StringLibrary.get("Response.clan-create"), c.getName(), "OPEN"));
				}
				return true;
			}
			if (equals(args[0], "info", "i")) {
				Clan test = ClansAPI.getInstance().getClan(i);
				if (test != null) {
					msg.send(test.getColor() + test.getName());
					msg.setPrefix(null);
					msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					msg.send("&2Power: &f" + test.getPower());
					if (test.getBase().getComponent().getStorage() != null) {
						msg.send("&2Base: &aSet");
					} else {
						msg.send("&2Base: &cNot set");
					}
					msg.send("&2Claims: &f" + test.getClaims().size() + "/" + test.getMaxClaims());
					msg.send("&2Allies: &f" + (test.getAllies().isEmpty() ? "&cNone" : test.getAllies().stream().map(Clan::getName).collect(Collectors.joining(", "))));
					msg.send("&2Enemies: &f" + (test.getEnemies().isEmpty() ? "&cNone" : test.getEnemies().stream().map(Clan::getName).collect(Collectors.joining(", "))));
					msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					AbstractPaginatedCollection<Clan.Associate> associates = AbstractPaginatedCollection.of(test.getMembers()).limit(test.getMembers().size()).sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getPlayer().get().getName(), o2.getPlayer().get().getName()));
					for (Clan.Associate ass : associates.get(1)) {
						msg.send("- &b" + ass.getName() + "&7 Lvl." + ass.getRank().getLevel());
					}
					msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				} else {
					if (PlayerSearch.of(i) != null) {
						Clan.Associate anothertest = ClansAPI.getInstance().getAssociate(PlayerSearch.of(i).getPlayer());
						if (anothertest != null) {
							test = anothertest.getClan();
							msg.send(test.getColor() + test.getName());
							msg.setPrefix(null);
							msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
							msg.send("&2Power: &f" + test.getPower());
							if (test.getBase().getComponent().getStorage() != null) {
								msg.send("&2Base: &aSet");
							} else {
								msg.send("&2Base: &cNot set");
							}
							msg.send("&2Claims: &f" + test.getClaims().size() + "/" + test.getMaxClaims());
							msg.send("&2Allies: &f" + (test.getAllies().isEmpty() ? "&cNone" : test.getAllies().stream().map(Clan::getName).collect(Collectors.joining(", "))));
							msg.send("&2Enemies: &f" + (test.getEnemies().isEmpty() ? "&cNone" : test.getEnemies().stream().map(Clan::getName).collect(Collectors.joining(", "))));
							msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
							AbstractPaginatedCollection<Clan.Associate> associates = AbstractPaginatedCollection.of(test.getMembers()).limit(test.getMembers().size()).sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getPlayer().get().getName(), o2.getPlayer().get().getName()));
							for (Clan.Associate ass : associates.get(1)) {
								msg.send("- &b" + ass.getName() + "&7 Lvl." + ass.getRank().getLevel());
							}
							msg.send("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
						}

					} else {
						msg.send(MessageFormat.format(StringLibrary.get("Response.invalid-name"), i));
						return true;
					}
				}
				return true;
			}

			// begin bank section
			else if (equals(args[0], "deposit", "withdraw")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate == null) {
					msg.send(StringLibrary.get("Response.not-in-clan"));
					return true;
				}
				final BigDecimal amount;
				try {
					amount = roundUserInput(new BigDecimal(i));
				} catch (NumberFormatException ignored) {
					msg.send("&c" + i + "&c is not a valid number.");
					return true;
				}
				if (amount.signum() < 0) {
					msg.send("&cAmount cannot be negative!");
					return true;
				}
				final EconomyProvisionPlayerEconomyEntity playerEconomyEntity = new EconomyProvisionPlayerEconomyEntity(p);
				if (equals(args[0], "deposit")) {
					try {
						associate.getClan().depositFrom(playerEconomyEntity, amount);
					} catch (WithdrawalException e) {
						msg.send("&cUnable to deposit " + formatCurrency(e.getOriginalAmount()) + ", do you have it?");
						return true;
					}
					msg.send("&2Deposited " + formatCurrency(amount) + " with the clan.");
					msg.send("&aNew balance: &e" + formatCurrency(associate.getClan().getBalance()));
				} else if (equals(args[0], "withdraw")) {
					// Check for access level
					if (associate.getRank().getLevel() < 2) {
						msg.send("&cYou must have a higher rank.");
						return true;
					}
					try {
						associate.getClan().withdrawTo(playerEconomyEntity, amount);
					} catch (WithdrawalException e) {
						msg.send("&cUnable to withdraw " + formatCurrency(e.getOriginalAmount()) + ". Does the bank have it?");
						return true;
					} catch (DepositException e) {
						msg.send("Sorry! We weren't able to give you the funds. They remain with the bank.");
						return true;
					}
					msg.send("&2Withdrew " + formatCurrency(amount) + " from the clan.");
					msg.send("&aNew balance: &e" + formatCurrency(associate.getClan().getBalance()));
				}
				return true;
			} else if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				msg.send("&aValid syntax:\n &b/clan bank &7<&esend&7> &7<&eother_clan&7> &7<&aamount&7>");
			}

			// end bank section

			if (equals(args[0], "truce")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= 2) {
						Clan target = ClansAPI.getInstance().getClan(i);
						if (target != null) {
							if ((target.getEnemies().contains(associate.getClan()) || associate.getClan().getEnemies().contains(target)) || target.getAllies().contains(associate.getClan())) {
								// send truce out message
								Applicable data = () -> {
									target.getAllies().remove(associate.getClan());
									associate.getClan().getAllies().remove(target);
									target.getEnemies().remove(associate.getClan());
									associate.getClan().getEnemies().remove(target);
									target.broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.clan-truced"), associate.getClan().getColor() + associate.getClan().getName()));
									associate.getClan().broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.clan-truced"), target.getColor() + target.getName()));
								};
								target.broadcast(new FancyMessage(MessageFormat.format(StringLibrary.get("Response.clan-truce"), associate.getClan().getColor() + associate.getClan().getName())).then(" ").then("[").color(ChatColor.GRAY).then("✔️").color(ChatColor.GREEN).style(ChatColor.BOLD).hover("&aClick to truce.").action(data).then("]").color(ChatColor.GRAY).build());
							} else {
								msg.send(MessageFormat.format(StringLibrary.get("Response.already-neutral"), target.getName()));
							}
						} else {
							msg.send(MessageFormat.format(StringLibrary.get("Response.invalid-name"), i));
						}
					} else {
						msg.send(StringLibrary.get("Response.no-clearance"));
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "ally")) {
				if (associate != null) {
					Clan target = ClansAPI.getInstance().getClan(i);
					if (target != null) {
						if (associate.getClan().getAllies().contains(target)) {
							msg.send(MessageFormat.format(StringLibrary.get("Response.already-allies"), target.getName()));
						} else {
							if (target.getEnemies().contains(associate.getClan())) {
								new FancyMessage("You are currently enemies with " + target.getName()).then(" ").then("ask for a truce?").then(" ").then("|").then(" ").then("[").color(ChatColor.GRAY).then("✔️").color(ChatColor.GREEN).style(ChatColor.BOLD).hover("&eClick to request truce.").command("c truce " + target.getName()).then("]").color(ChatColor.GRAY).send(p).queue();
							} else {
								if (associate.getRank().getLevel() >= 2) {
									if (associate.getClan().getRequests().contains(target)) {
										associate.getClan().getRequests().remove(target);
										associate.getClan().getAllies().add(target);
										target.getAllies().add(associate.getClan());
										target.broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.now-allies"), associate.getClan().getColor() + associate.getClan().getName()));
										associate.getClan().broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.now-allies"), target.getColor() + target.getName()));
										return true;
									}
									final ClanAllyRequests requests = target.getRequests();
									if (!requests.contains(associate.getClan())) {
										requests.add(associate.getClan());
										String message = MessageFormat.format(StringLibrary.get("Response.clan-ally"), associate.getClan().getColor() + associate.getClan().getName());
										target.broadcast(new FancyMessage(message).then(" ").then("[").color(ChatColor.GRAY).then("✔️").color(ChatColor.GREEN).style(ChatColor.BOLD).hover("&aClick to accept friendship.").command("c ally " + associate.getClan().getName()).then("]").color(ChatColor.GRAY).then(" ").then("[").color(ChatColor.GRAY).then("❌").color(ChatColor.RED).style(ChatColor.BOLD).hover("&cClick to deny request.").action(() -> {
											requests.remove(associate.getClan());
											associate.getClan().broadcast(m -> true, "&cClan " + target.getName() + " denied our alliance request.");
										}).then("]").color(ChatColor.GRAY).build());
										String message2 = MessageFormat.format(StringLibrary.get("Response.clan-ally-out"), p.getName(), target.getColor() + target.getName());
										associate.getClan().broadcast(new FancyMessage(message2).then(" ").then("[").color(ChatColor.GRAY).then("❌").color(ChatColor.RED).style(ChatColor.BOLD).hover("&cClick to cancel this request out.").action(() -> {
											requests.remove(associate.getClan());
											associate.getClan().broadcast(m -> true, "&cAlliance request to " + target.getName() + " cancelled.");
											target.broadcast(m -> true, "&cThe request for alliance with us from " + associate.getClan().getName() + " has been cancelled.");
										}).then("]").color(ChatColor.GRAY).build());
									} else {
										msg.send(MessageFormat.format(StringLibrary.get("Response.clan-waiting"), target.getName()));
									}
								} else {
									msg.send(StringLibrary.get("Response.no-clearance"));
								}
							}
						}
					} else {
						msg.send(MessageFormat.format(StringLibrary.get("Response.invalid-name"), i));
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			if (equals(args[0], "enemy")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= 2) {
						Clan target = ClansAPI.getInstance().getClan(i);
						if (target != null) {
							if (associate.getClan().getEnemies().contains(target) || target.getEnemies().contains(associate.getClan())) {
								msg.send(MessageFormat.format(StringLibrary.get("Response.already-enemies"), target.getName()));
							} else {
								if (associate.getClan().getAllies().contains(target)) {
									associate.getClan().getAllies().remove(target);
									target.getAllies().remove(associate.getClan());
									target.broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.enemies-from-allies"), associate.getClan().getColor() + associate.getClan().getName()));
									associate.getClan().broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.enemies-from-allies"), target.getColor() + target.getName()));
								} else {
									target.broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.now-enemies"), associate.getClan().getColor() + associate.getClan().getName()));
									associate.getClan().broadcast(m -> true, MessageFormat.format(StringLibrary.get("Response.now-enemies"), target.getColor() + target.getName()));
								}
								associate.getClan().getEnemies().add(target);
								target.getEnemies().add(associate.getClan());
							}
						} else {
							msg.send(MessageFormat.format(StringLibrary.get("Response.invalid-name"), i));
						}
					} else {
						msg.send(StringLibrary.get("Response.no-clearance"));
					}
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
				return true;
			}

			return true;
		}

		if (args.length == 3) {
			String i = args[1];
			String i2 = args[2];
			if (equals(args[0], "join")) {
				if (ClansAPI.getInstance().getAssociate(p) == null) {
					Clan test = ClansAPI.getInstance().getClan(i);
					if (test != null) {
						if (test.getPassword() == null) {
							// join them
							test.getMembers().add(new OfflineAssociate(p.getName(), test, Clan.Rank.NORMAL));
							test.broadcast(associate1 -> true, StringUtils.use(MessageFormat.format(StringLibrary.get("Response.member-join"), p.getName())).translate(p));
						} else {
							if (test.getPassword().equals(i2)) {
								test.getMembers().add(new OfflineAssociate(p.getName(), test, Clan.Rank.NORMAL));
								test.broadcast(associate1 -> true, StringUtils.use(MessageFormat.format(StringLibrary.get("Response.member-join"), p.getName())).translate(p));
							} else {
								msg.send("&cPassword invalid!");
							}
							return true;
						}

					} else {
						msg.send(MessageFormat.format(StringLibrary.get("Response.invalid-name"), i));
					}
				} else {
					msg.send(StringLibrary.get("Response.already-in-clan"));
				}
				return true;
			}
			if (equals(args[0], "create")) {
				if (associate != null) {
					msg.send(StringLibrary.get("Response.already-in-clan"));
				} else {
					if (i.length() >= StringLibrary.getTagLimit()) {
						msg.send(MessageFormat.format(StringLibrary.get("Response.tag-too-long"), StringLibrary.getTagLimit()));
						return true;
					}
					Clan c = ClansAPI.getInstance().getClanManager().generateClan(p, i, i2);
					// TODO: announce creation
					msg.send(MessageFormat.format(StringLibrary.get("Response.clan-create"), c.getName(), "LOCKED"));
				}
				return true;
			}

			// begin bank section
			else if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate != null) {
					msg.send("&aValid syntax:\n &b/clan bank &7<&esend&7> &7<&eother_clan&7> &7<&aamount&7>");
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
			}

			// end bank section

			return true;
		}

		if (args.length == 4) {
			// begin bank section
			if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate != null) {
					if (equals(args[1], "send")) {
						// validate amount ahead of clan resolution
						final BigDecimal transferAmount;
						try {
							transferAmount = roundUserInput(new BigDecimal(args[3]));
						} catch (NumberFormatException ignored) {
							msg.send("&c" + args[3] + "&c is not a valid number.");
							return true;
						}
						if (transferAmount.signum() < 0) {
							msg.send("&cAmount cannot be negative!");
							return true;
						}
						// resolve target clan by name
						final Clan targetClan = ClansAPI.getInstance().getClan(args[2]);
						if (targetClan == null) {
							msg.send("&cThat clan does not exist.");
							return true;
						}
						try {
							associate.getClan().withdrawTo(targetClan, transferAmount);
						} catch (WithdrawalException e) {
							msg.send("&cUnable to send " + formatCurrency(e.getOriginalAmount()) + ". Does the bank have it?");
							return true;
						} catch (DepositException e) {
							msg.send("Sorry! We weren't able to transfer the funds. They remain with the clan.");
							return true;
						}
						msg.send("&2Transferred " + formatCurrency(transferAmount) + " from the clan to " + targetClan.getColor() + args[2] + ".");
						msg.send("&aYour new clan balance: &e" + formatCurrency(associate.getClan().getBalance()));
						return true;
					}
					msg.send("&aValid syntax:\n &b/clan bank &7<&esend&7> &7<&eother_clan&7> &7<&aamount&7>");
				} else {
					msg.send(StringLibrary.get("Response.not-in-clan"));
				}
			}

			// end bank section
		}

		return true;
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			return console(sender, label, args);
		}
		return player((Player) sender, label, args);
	}

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		final ArrayList<String> completions = new ArrayList<>();
		if (args.length == 1) {
			final String lowerCase = args[0].toLowerCase(Locale.ROOT);
			final boolean bankEnabled = ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"));
			for (String test : Arrays.asList("create", "leave", "info", "join", "claim", "unclaim", "base", "setbase", "tag", "color", "chat")) {
				if (test.toLowerCase(Locale.ROOT).startsWith(lowerCase)) {
					completions.add(test);
				}
			}
			if (bankEnabled && "bank".startsWith(lowerCase)) {
				completions.add("bank");
			}
			if (bankEnabled && "deposit".startsWith(lowerCase)) {
				completions.add("deposit");
			}
			if (bankEnabled && "withdraw".startsWith(lowerCase)) {
				completions.add("withdraw");
			}
		} else if (args.length == 2) {
			final String lowerCase0 = args[0].toLowerCase(Locale.ROOT);
			final String lowerCase1 = args[1].toLowerCase(Locale.ROOT);
			if ("bank".equals(lowerCase0) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
				if ("send".startsWith(lowerCase1)) {
					completions.add("send");
				}
			}
			if (("deposit".equals(lowerCase0) || "withdraw".equals(lowerCase0)) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
				if ("10".startsWith(lowerCase1)) {
					completions.add("10");
				}
			}
		} else if (args.length == 3) {
			final String lowerCase0 = args[0].toLowerCase(Locale.ROOT);
			final String lowerCase1 = args[1].toLowerCase(Locale.ROOT);
			final String lowerCase2 = args[2].toLowerCase(Locale.ROOT);
			if ("bank".equals(lowerCase0) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
				if ("send".equals(lowerCase1)) {
					return ClansAPI.getInstance().getClanManager().getClans()
							.map(Clan::getName)
							.filter(clanName -> clanName.startsWith(lowerCase2))
							.collect(Collectors.toList());
				}
			}
		} else if (args.length == 4) {
			final String lowerCase0 = args[0].toLowerCase(Locale.ROOT);
			final String lowerCase1 = args[1].toLowerCase(Locale.ROOT);
			final String lowerCase2 = args[2].toLowerCase(Locale.ROOT);
			final String lowerCase3 = args[3].toLowerCase(Locale.ROOT);
			if ("bank".equals(lowerCase0) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled"))) {
				if ("send".equals(lowerCase1)) {
					if (ClansAPI.getInstance().getClan(args[2]) != null) {
						if ("10".startsWith(lowerCase3)) {
							completions.add("10");
						}
					}
				}
			}
		}
		return completions;
	}

	private BigDecimal roundUserInput(BigDecimal original) {
		return original.setScale(2, RoundingMode.HALF_UP);
	}

	private String formatCurrency(BigDecimal amount) {
		return MathUtils.use(amount).formatCurrency(Locale.US);
	}


}
