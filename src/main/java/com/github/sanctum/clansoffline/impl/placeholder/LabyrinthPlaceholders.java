package com.github.sanctum.clansoffline.impl.placeholder;

import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.clansoffline.impl.pro.MapPoint;
import com.github.sanctum.labyrinth.data.service.Constant;
import com.github.sanctum.labyrinth.placeholders.Placeholder;
import com.github.sanctum.labyrinth.placeholders.PlaceholderIdentifier;
import com.github.sanctum.labyrinth.placeholders.PlaceholderTranslation;
import com.github.sanctum.labyrinth.placeholders.PlaceholderVariable;
import java.text.NumberFormat;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LabyrinthPlaceholders implements PlaceholderTranslation {

	private final Placeholder[] placeholders = Constant.values(Placeholder.class, Placeholder.class).toArray(new Placeholder[0]);
	private final PlaceholderIdentifier identifier = () -> "clansfree";

	@Override
	public @Nullable String onTranslation(String parameter, PlaceholderVariable variable) {

		if (variable.exists() && variable.isPlayer()) {

			OfflinePlayer p = variable.getAsPlayer();

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

			Clan.Associate associate = ClansAPI.getInstance().getAssociate(p);

			if (associate == null) {
				return "";
			}

			if (parameter.equalsIgnoreCase("clan_name")) {
				return associate.getClan().getName();
			}

			if (parameter.equalsIgnoreCase("clan_power")) {
				return NumberFormat.getNumberInstance().format(associate.getClan().getPower());
			}


		}

		return null;
	}

	@Override
	public @NotNull Placeholder[] getPlaceholders() {
		return placeholders;
	}

	@Override
	public @Nullable PlaceholderIdentifier getIdentifier() {
		return this.identifier;
	}
}
