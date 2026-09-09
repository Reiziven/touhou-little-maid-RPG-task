package studio.fantasyit.maid_rpg_task.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Shared target-validity check for all elemental spells.
 * A valid target is one that:
 *  - Is alive
 *  - Is NOT the maid itself
 *  - Is NOT the maid's owner
 *  - Is NOT a player (allies of the owner)
 *  - Is NOT a tamed animal owned by the same owner
 *  - The maid {@code canAttack()} it (i.e. maid is hostile toward it)
 */
public final class SpellTargetHelper {

    private SpellTargetHelper() {}

    /**
     * @param entity    Candidate target
     * @param maid      The maid caster (may be null, skips canAttack check)
     * @param maidUuid  UUID of the maid (may be null)
     * @param ownerUuid UUID of the maid's owner (may be null)
     */
    public static boolean isValidTarget(LivingEntity entity, LivingEntity maid,
                                        UUID maidUuid, UUID ownerUuid) {
        if (!entity.isAlive()) return false;
        if (maidUuid != null && entity.getUUID().equals(maidUuid)) return false;
        if (ownerUuid != null && entity.getUUID().equals(ownerUuid)) return false;
        // Spare all players (owner + allies)
        if (entity instanceof Player) return false;
        // Spare tamed animals belonging to the same owner
        if (ownerUuid != null && entity instanceof TamableAnimal ta
                && ownerUuid.equals(ta.getOwnerUUID())) return false;
        // Use maid's hostility check if available
        if (maid != null) return maid.canAttack(entity);
        return true;
    }
}
