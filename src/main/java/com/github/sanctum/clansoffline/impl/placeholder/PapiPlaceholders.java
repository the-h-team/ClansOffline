package com.github.sanctum.clansoffline.impl.placeholder;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.impl.pro.MapPoint;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PapiPlaceholders extends PlaceholderExpansion {

	final ClansAPI api = ClansAPI.getInstance();

	@Override
	public String getIdentifier() {
		return "clansfree";
	}

	@Override
	public String getAuthor() {
		return "Hempfest";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String onPlaceholderRequest(Player p, String parameter) {

		if (parameter.equals("land_chunk_map_line1")) {
			return MapPoint.getMapLine(p.getPlayer(), 5, 5, 0);
		}

		if (parameter.equals("land_chunk_map_line2")) {
			return MapPoint.getMapLine(p.getPlayer(), 5, 5, 1);
		}

		if (parameter.equals("land_chunk_map_line3")) {
			return MapPoint.getMapLine(p.getPlayer(), 5, 5, 2);
		}

		if (parameter.equals("land_chunk_map_line4")) {
			return MapPoint.getMapLine(p.getPlayer(), 5, 5, 3);
		}

		if (parameter.equals("land_chunk_map_line5")) {
			return MapPoint.getMapLine(p.getPlayer(), 5, 5, 4);
		}

		Clan.Associate associate = api.getAssociate(p);
		if (parameter.equals("clan_name")) {
			if (associate != null) {
				return associate.getClan().getName();
			} else return "N/A";
		}
		if (parameter.equals("clan_name_colored")) {
			if (associate != null) {
				return associate.getClan().getColor() + associate.getClan().getName();
			} else return "N/A";
		}
		if (parameter.equals("member_rank")) {
			if (associate != null) {
				return associate.getRank().toFull();
			} else return "N/A";
		}
		if (parameter.equals("members_online")) {
			if (associate != null) {
				return String.valueOf(associate.getClan().getMembers().stream().filter(associate1 -> associate1.getPlayer().isPresent() && associate1.getPlayer().get().isOnline()).count());
			} else return "0";
		}

		return super.onPlaceholderRequest(p, parameter);
	}
}
