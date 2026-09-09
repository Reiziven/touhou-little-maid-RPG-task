package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskFeedOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_rpg_task.compat.PlayerRevive;
import studio.fantasyit.maid_rpg_task.compat.PlayerReviveProxy;

/**
 * Reuses Touhou Little Maid's own {@link TaskFeedOwner} feed logic (food priority,
 * eating animation/sound, etc.) as-is, only overriding {@link #isFood(ItemStack, Player)}
 * so the maid refuses to feed the owner while they are downed/bleeding-out from
 * Player Revive — feeding a fallen player makes no sense and could interfere with revival.
 * <p>
 * All references to PlayerRevive's own classes stay isolated inside {@link PlayerReviveProxy}
 * so this class loads fine even when PlayerRevive isn't installed.
 */
public class OwnerFeedPolicy extends TaskFeedOwner {
    @Override
    public boolean isFood(ItemStack stack, Player owner) {
        if (PlayerRevive.isEnable() && PlayerReviveProxy.isOwnerFallen(owner)) {
            return false;
        }
        return super.isFood(stack, owner);
    }
}
