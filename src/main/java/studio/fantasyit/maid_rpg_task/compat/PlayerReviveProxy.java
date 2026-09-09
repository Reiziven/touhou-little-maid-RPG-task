package studio.fantasyit.maid_rpg_task.compat;

import net.minecraft.world.entity.player.Player;
import team.creative.playerrevive.server.PlayerReviveServer;

/**
 * Loaded ONLY when PlayerRevive mod is present.
 * All references to PlayerRevive classes are isolated here, so callers can safely
 * reference this proxy's methods behind a {@link PlayerRevive#isEnable()} check
 * without risking classloading errors when PlayerRevive isn't installed.
 */
public class PlayerReviveProxy {
    /**
     * @return true if the player is currently downed/bleeding-out (the "fallen" state).
     * Only call this after confirming {@link PlayerRevive#isEnable()}.
     */
    public static boolean isOwnerFallen(Player owner) {
        return PlayerReviveServer.isBleeding(owner);
    }
}
