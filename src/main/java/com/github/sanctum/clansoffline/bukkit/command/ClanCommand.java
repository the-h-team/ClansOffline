package com.github.sanctum.clansoffline.bukkit.command;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.bukkit.StringLibrary;
import com.github.sanctum.clansoffline.impl.ClanBase;
import com.github.sanctum.clansoffline.impl.OfflineClaim;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
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

			return true;
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




}
