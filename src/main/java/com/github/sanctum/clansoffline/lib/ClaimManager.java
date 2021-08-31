package com.github.sanctum.clansoffline.lib;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.impl.ClanDataFile;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class ClaimManager extends Manager<Claim>{

	private final List<Claim> CLAIMS = new LinkedList<>();

	@Override
	public boolean load(@NotNull Claim claim) {
		return CLAIMS.add(claim);
	}

	@Override
	public boolean remove(@NotNull Claim claim) {
		ClanDataFile file = claim.getOwner().getData();
		file.set("claims." + claim.getId().toString(), null);
		return CLAIMS.remove(claim);
	}

	public UniformedComponents<Claim> getClaims() {
		return UniformedComponents.accept(CLAIMS);
	}

	public Claim get(Predicate<Claim> predicate) {
		for (Claim c : CLAIMS) {
			if (predicate.test(c)) {
				return c;
			}
		}
		return null;
	}

	public boolean isInClaim(Chunk c) {
		return get(cl -> cl.getChunk().equals(c)) != null;
	}

	public boolean isInClaim(Location loc) {
		return get(cl -> cl.getChunk().equals(loc.getChunk())) != null;
	}

}
