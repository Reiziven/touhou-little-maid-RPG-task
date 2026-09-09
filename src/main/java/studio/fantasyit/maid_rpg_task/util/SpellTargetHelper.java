package studio.fantasyit.maid_rpg_task.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class SpellTargetHelper {

    private SpellTargetHelper() {}

    public static boolean isValidTarget(LivingEntity entity, LivingEntity maid,
                                        UUID maidUuid, UUID ownerUuid) {
        if (!entity.isAlive()) return false;
        if (maidUuid != null && entity.getUUID().equals(maidUuid)) return false;
        if (ownerUuid != null && entity.getUUID().equals(ownerUuid)) return false;
        if (entity instanceof Player) return false;
        if (ownerUuid != null && entity instanceof TamableAnimal ta
                && ownerUuid.equals(ta.getOwnerUUID())) return false;
        if (maid != null) return maid.canAttack(entity);
        return true;
    }
}
