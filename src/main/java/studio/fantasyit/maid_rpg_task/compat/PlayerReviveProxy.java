package studio.fantasyit.maid_rpg_task.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.player.Player;
import studio.fantasyit.maid_rpg_task.behavior.PlayerReviveBehavior;
import team.creative.playerrevive.server.PlayerReviveServer;

/**
 * Loaded ONLY when PlayerRevive mod is present.
 * All references to PlayerRevive classes are isolated here.
 */
public class PlayerReviveProxy {
    public static BehaviorControl<EntityMaid> createBehavior() {
        return new PlayerReviveBehavior(false);
    }

    public static BehaviorControl<EntityMaid> createBehaviorForMaster() {
        return new PlayerReviveBehavior(true);
    }

    /**
     * @return true if the player is currently downed/bleeding-out (the "fallen" state).
     * Only call this after confirming {@link PlayerRevive#isEnable()}.
     */
    public static boolean isOwnerFallen(Player owner) {
        return PlayerReviveServer.isBleeding(owner);
    }
}
