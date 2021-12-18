package com.github.sanctum.clansoffline.impl.pro;

import com.github.sanctum.clansoffline.api.Claim;
import com.github.sanctum.clansoffline.api.Clan;
import com.github.sanctum.clansoffline.api.ClansAPI;
import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.library.HUID;
import com.google.common.base.Strings;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a clan map data point. Stores a nullable clanId String and a
 * ChunkPosition for further processing.
 */
public class MapPoint {
	public final String clanId;
	public Applicable appliance = null;
	public final ChunkPosition chunkPosition;
	protected String color = ChatColor.GRAY.toString();
	protected char representation = '-';
	protected String hover = null;

	/**
	 * Main constructor for ordinary MapPoints.
	 * <p>See {@link #center(String, ChunkPosition)} for simple factory-based
	 * {@link #isCenter()} override.</p>
	 *
	 * @param clanId        clanId at this point (can be null)
	 * @param chunkPosition chunk coordinate data at this point
	 */
	public MapPoint(@Nullable String clanId, ChunkPosition chunkPosition) {
		this.clanId = clanId;
		this.chunkPosition = chunkPosition;
	}

	/**
	 * Get the Clan object at this point, if applicable.
	 *
	 * @return Clan or null
	 */
	public Clan getClan() {
		if (clanId == null) return null;
		return ClansAPI.getInstance().getClan(HUID.fromString(clanId));
	}

	/**
	 * Get the name of the Clan at this point, if applicable.
	 *
	 * @return name of the clan or null
	 */
	public String getClanName() {
		if (clanId == null) return null;
		return ClansAPI.getInstance().getClan(HUID.fromString(clanId)).getName();
	}

	/**
	 * Set the display color of this point on the map.
	 *
	 * @param color to display representation
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * Set an operation to be ran when this map point is clicked on.
	 *
	 * @param appliance The information to run when the map point is clicked on.
	 */
	public void setAppliance(Applicable appliance) {
		this.appliance = appliance;
	}

	/**
	 * Get the display color set for this point on the map.
	 *
	 * @return color string (default gray)
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Set the character to represent this point on the map.
	 * Defaults to '-' for vacant points, 'A'... for claims
	 *
	 * @param character 1-width character
	 */
	public void setRepresentation(char character) {
		this.representation = character;
	}

	/**
	 * Gets the character which will be used to represent this point on the map.
	 *
	 * @return char ('-' default)
	 */
	public char getRepresentation() {
		return representation;
	}

	/**
	 * Set the hover message for this MapPoint.
	 *
	 * <p>A few placeholders are available:</p>
	 * <p>%clanTag% = Clan name</p>
	 * <p>%clanPower% = Clan power</p>
	 *
	 * @param textOnHover Formatted String with placeholders
	 */
	public void setHover(String textOnHover) {
		this.hover = textOnHover;
	}

	/**
	 * Get the direct hover text currently set without placeholder replacements.
	 *
	 * @return this.hover (can be null)
	 */
	@Nullable
	public String getRawHover() {
		return hover;
	}

	/**
	 * Get the fully-formatted hover text, if it has been set.
	 *
	 * @return text or null
	 */
	@Nullable
	public String getHover() {
		if (hover == null) return null;
		if (!hover.contains("%")) return hover;
		String hover = this.hover;
		if (hover.contains("%clanTag%")) {
			hover = hover.replace("%clanTag%", getClanName());
		}
		if (hover.contains("%clanPower%")) {
			hover = hover.replace("%clanPower%", NumberFormat.getNumberInstance().format(getClan().getPower()));
        }
		if (hover.contains("%chunkX%")) {
			hover = hover.replace("%chunkX%", String.valueOf(chunkPosition.x));
		}
		if (hover.contains("%chunkZ%")) {
			hover = hover.replace("%chunkZ%", String.valueOf(chunkPosition.z));
		}
        /*if (hover.contains("%allyStatus%")) {
            // TODO: need reference to player?
        }*/
		return hover;
	}

	/**
	 * Returns true if this point represents the player's chunk (the map center).
	 *
	 * @return false, if center player true
	 */
	public boolean isCenter() {
		return false;
	}

	/**
	 * Static factory to create object with isCenter() override instead of dedicating
	 * another instance field and an alternate constructor.
	 * <p>(There is, after all, only one time when we'll need {@link #isCenter()}
	 * to return true)</p>
	 *
	 * @param clanId        clanId (can be null)
	 * @param chunkPosition Data on chunk coordinates
	 * @return new MapPoint with {@link #isCenter()} return true
	 */
	public static MapPoint center(@Nullable String clanId, ChunkPosition chunkPosition) {
		return new MapPoint(clanId, chunkPosition) {
			@Override
			public boolean isCenter() {
				return true;
			}
		};
	}

    public static Optional<BlockFace> chooseDirection(float degree) {
        // scale to increments of 5
        if (degree < 0f) {
            degree += 360f;
        }
        if (degree >= 320f || degree <= 40f) {
            return Optional.of(BlockFace.SOUTH);
        } else if (degree >= 60f && degree <= 130f) {
            return Optional.of(BlockFace.WEST);
        } else if (degree >= 140f && degree <= 220f) {
            return Optional.of(BlockFace.NORTH);
        } else if (degree >= 230f && degree <= 310f) {
            return Optional.of(BlockFace.EAST);
        }
        return Optional.empty();
    }

    static int[] calculate_rot(BlockFace blockFace, int init_X, int init_Z, int x_offset, int z_offset, int playerX, int playerZ) {
        final int x, z;
        if (blockFace == BlockFace.WEST) {
            x = z_offset - init_Z;
            z = -x_offset + init_X;
        } else if (blockFace == BlockFace.NORTH) {
            x = -init_X + x_offset;
            z = -init_Z + z_offset;
        } else if (blockFace == BlockFace.EAST) {
            x = -z_offset + init_Z;
            z = x_offset - init_X;
        } else { // SOUTH
            x = init_X - x_offset;
            z = init_Z - z_offset;
        }
        return new int[]{playerX - x, playerZ - z};
    }

    public static MapPoint[][] getMap(final Player player, int height, int width) {
        // negative Z = NORTH
        final float yaw = player.getLocation().getYaw();
        final Optional<BlockFace> optional = CompletableFuture.supplyAsync(() -> chooseDirection(yaw)).join();
        if (!optional.isPresent()) {
            return null;
        }
        final BlockFace compassDirection = optional.get();
        final Chunk chunk = player.getLocation().getChunk();
        final int playerChunkX = chunk.getX();
        final int playerChunkZ = chunk.getZ();
        final List<Claim> chunkMap = new ArrayList<>(ClansAPI.getInstance().getClaimManager().getClaims().list());
        final Map<ChunkPosition, String> clanChunks = new HashMap<>(); // key = chunk, value = clanId
        for (Claim entry : chunkMap) {
            if (Math.abs(entry.getChunk().getX() - playerChunkX) >= 16) {
                continue;
            }
            if (Math.abs(entry.getChunk().getZ() - playerChunkZ) >= 16) {
                continue;
            }
            clanChunks.put(new ChunkPosition(new int[]{entry.getChunk().getX(), entry.getChunk().getZ()}), entry.getOwner().getId().toString());
        }
        final ChunkPosition playerChunk = new ChunkPosition(playerChunkX, playerChunkZ);

        final MapPoint[][] mapPoints = new MapPoint[width][height];
        final int x_offset = height / 2;
        final int z_offset = width / 2;

        for (int z = 0; z < width; ++z) {
            for (int x = 0; x < height; ++x) {
                final ChunkPosition test = new ChunkPosition(calculate_rot(compassDirection, x, z, x_offset, z_offset, playerChunkX, playerChunkZ));
                String clanId = clanChunks.get(test);
                if (test.equals(playerChunk)) {
                    mapPoints[z][x] = MapPoint.center(clanId, test);
                    continue;
                }
                mapPoints[z][x] = new MapPoint(clanId, test); // chunk data now always stored with MapPoint
            }
        }

        for (MapPoint[] points : mapPoints) {
            for (MapPoint point : points) {
                if (point.isCenter()) {
					point.setColor("&7");
					if (point.getClan() != null) {
						Clan.Associate a = ClansAPI.getInstance().getAssociate(player);
						if (a != null) {
							if (a.getClan().equals(point.getClan())) {
								point.setColor(a.getClan().getColor());
							}
						}
					}
                    point.setRepresentation('⬤');
                } else {
                    if (point.getClan() != null) {
                        if (ClansAPI.getInstance().getAssociate(player) != null) {
                            Clan.Associate a = ClansAPI.getInstance().getAssociate(player);
                            String color = "&f";
                            if (point.getClan().getAllies().contains(a.getClan())) {
                                color = "&a";
                            }
                            if (point.getClan().getEnemies().contains(a.getClan())) {
                                color = "&c";
                            }
							if (point.getClan().equals(a.getClan())) {
								color = a.getClan().getColor();
							}
                            point.setColor(color);
                        } else {
                            point.setColor("&f");
                        }
                        point.setRepresentation('⬛');
                    } else {
                        point.setColor("&f");
                        point.setRepresentation('⬜');
                    }
                }
            }
        }
        return mapPoints;
    }

    public static String getMapLine(final Player player, int height, int width, int line) {
        MapPoint[][] parent = getMap(player, height, width);
        if (parent != null) {
            MapPoint[] line1 = parent[Math.max(Math.min(parent.length, line), 0)];

            return Arrays.stream(line1).map(po -> po.getColor() + po.getRepresentation()).collect(Collectors.joining());
        }
        return Strings.repeat("⬜", width);
    }

}
