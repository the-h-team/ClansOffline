package com.github.sanctum.clansoffline.bukkit.command;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bank.entities.EconomyProvisionPlayerEconomyEntity;
import com.github.sanctum.clansoffline.bank.exceptions.DepositException;
import com.github.sanctum.clansoffline.bank.exceptions.WithdrawalException;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.impl.ClanBase;
import com.github.sanctum.clansoffline.impl.OfflineClaim;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanCommand extends Command {

	public ClanCommand() {
		super("clan");
		setAliases(Arrays.asList("c", "cl"));
		setDescription("The primary alias to clan commands.");
		setPermission("clans.use");
	}

	public boolean console(@NotNull CommandSender c, @NotNull String label, @NotNull String[] args) {


		return true;
	}

	boolean equals(String text, String...options) {
		for (String s : options) {
			if (text.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	public boolean player(@NotNull Player p, @NotNull String label, @NotNull String[] args) {

		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p);
		Message msg = Message.form(p).setPrefix(ClansAPI.getInstance().getPrefix().getJoined());

		List<World> w = StringLibrary.getList("Clans.list.worlds").stream().filter(s -> Bukkit.getWorld(s) != null).map(Bukkit::getWorld).collect(Collectors.toList());

		if (!w.contains(p.getWorld())) {
			msg.send("&cClan features are disabled for this world.");
			return true;
		}

		if (args.length == 0) {

			return true;
		}

		if (args.length == 1) {

			if (equals(args[0], "create")) {
				if (associate != null) {
					msg.send("&cYou are already in a clan!");
				} else {
					msg.send("&aPlease specify a name!");
				}
				return true;
			}

			if (equals(args[0], "leave")) {
				if (associate != null) {
					if (associate.getRank().getLevel() == 3) {
						Bukkit.broadcastMessage("Clan " + associate.getClan().getName() + " has fallen!");
					} else {
						associate.getClan().broadcast(m -> true, "&6" + p.getName() + " &4left the clan!");
					}
					associate.getClan().remove(associate);
				} else {
					msg.send("&cYou are not in a clan!");
				}
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
					if(associate.getChat().equalsIgnoreCase("ALLY")) {
						associate.setChat("GLOBAL");
						msg.send("&aNow in global chat!");
						return true;
					}
				} else {

				}
				return true;
			}

			if (equals(args[0], "claim")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= StringLibrary.getClearance("claim")) {

						if (!ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
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

			if (equals(args[0], "unclaim")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= StringLibrary.getClearance("un-claim")) {

						if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
							Claim c = ClansAPI.getInstance().getClaimManager().get(cl -> cl.getLocation().equals(p.getLocation()));
							if (c.getOwner().equals(associate.getClan())) {
								ClansAPI.getInstance().getClaimManager().remove(c);
								associate.getClan().broadcast(a -> true, "&3We no longer own chunk X:" + p.getLocation().getChunk().getX() + " Z:" + p.getLocation().getChunk().getZ());
							} else {
								// we dont own it. Test settings for raid shield then try to unclaim
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

			if (equals(args[0], "info", "i")) {
				if (associate != null) {

				} else {
					msg.send("&cYou are not in a clan!");
				}
				return true;
			}

			if (equals(args[0], "base")) {
				if (associate != null) {
					ClanBase base = associate.getClan().getBase();
					if (base.getComponent().getStorage() != null) {
						p.teleport(base.getComponent().getStorage());
					} else {
						msg.send("&cNo clan base found!");
					}

				} else {
					msg.send("&cYou are not in a clan!");
				}

				return true;
			}

			if (equals(args[0], "setbase")) {
				if (associate != null) {
					if (associate.getRank().getLevel() >= StringLibrary.getClearance("setbase")) {
						associate.getClan().getBase().update(p.getLocation());
						msg.send("&6Clan base location updated!");
					} else {
						msg.send("&cYou don't have clearance.");
					}

				} else {
					msg.send("&cYou are not in a clan!");
				}
				return true;
			}

			// begin bank section
			if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate != null) {
					msg.send("&aWelcome to the clan bank.");
					msg.send("The clan balance seems to be " + formatCurrency(associate.getClan().getBalance()));
				} else {
					msg.send("&cYou are not in a clan!");
				}
				return true;
			}

			else if (equals(args[0], "deposit", "withdraw")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate != null) {
					msg.send("&aValid syntax:\n &b/clan &7<&edeposit&7|&ewithdraw&7> &7<&aamount&7>");
				} else {
					msg.send("&cYou are not in a clan!");
				}
				return true;
			}

			// end bank section

			return true;
		}

		if (args.length == 2) {
			String i = args[1];
			if (equals(args[0], "create")) {
				if (associate != null) {
					msg.send("&cYou are already in a clan!");
				} else {
					Clan c = ClansAPI.getInstance().getClanManager().generateClan(p, i);
					msg.send("&6Clan " + c.getName() + " has been created!");
				}
				return true;
			}

			// begin bank section
			else if (equals(args[0], "deposit", "withdraw")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate == null) {
					msg.send("&cYou are not in a clan!");
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
			}
			else if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				msg.send("&aValid syntax:\n &b/clan bank &7<&esend&7> &7<&eother_clan&7> &7<&aamount&7>");
			}

			// end bank section

			return true;
		}

		if (args.length == 3) {
			String i = args[1];
			String i2 = args[2];
			if (equals(args[0], "create")) {
				if (associate != null) {
					msg.send("&cYou are already in a clan!");
				} else {
					Clan c = ClansAPI.getInstance().getClanManager().generateClan(p, i, i2);
					msg.send("&6Clan " + c.getName() + " has been created with password " + c.getPassword());
				}
				return true;
			}

			// begin bank section
			else if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
					msg.send("&cClan banks are disabled on this server.");
					return true;
				}
				if (associate != null) {
					msg.send("&aValid syntax:\n &b/clan bank &7<&esend&7> &7<&eother_clan&7> &7<&aamount&7>");
				} else {
					msg.send("&cYou are not in a clan!");
				}
			}

			// end bank section

			return true;
		}

		if (args.length == 4) {
			// begin bank section
			if (equals(args[0], "bank")) {
				if (!ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
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
					msg.send("&cYou are not in a clan!");
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
		return player((Player)sender, label, args);
	}

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		final ArrayList<String> completions = new ArrayList<>();
		if (args.length == 1) {
			final String lowerCase = args[0].toLowerCase(Locale.ROOT);
			final boolean bankEnabled = ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true));
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
			if ("bank".equals(lowerCase0) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
				if ("send".startsWith(lowerCase1)) {
					completions.add("send");
				}
			}
			if (("deposit".equals(lowerCase0) || "withdraw".equals(lowerCase0)) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
				if ("10".startsWith(lowerCase1)) {
					completions.add("10");
				}
			}
		} else if (args.length == 3) {
			final String lowerCase0 = args[0].toLowerCase(Locale.ROOT);
			final String lowerCase1 = args[1].toLowerCase(Locale.ROOT);
			final String lowerCase2 = args[2].toLowerCase(Locale.ROOT);
			if ("bank".equals(lowerCase0) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
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
			if ("bank".equals(lowerCase0) && ClansAPI.getInstance().getMain().read(f -> f.getBoolean("Clans.bank.enabled", true))) {
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
		return NumberFormat.getCurrencyInstance(Locale.US).format(amount.doubleValue());
	}


}
