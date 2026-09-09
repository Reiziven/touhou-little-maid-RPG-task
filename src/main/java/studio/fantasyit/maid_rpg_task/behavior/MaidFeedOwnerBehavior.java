package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFeedOwnerTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.data.MaidReviveConfig;

/**
 * Lets Support/Master maids feed their owner, on top of Touhou Little Maid's own
 * {@link MaidFeedOwnerTask} brain behavior (walk to owner, pick food by priority, feed it).
 * <p>
 * We don't reimplement any of that — we only gate it behind:
 * <ul>
 *     <li>the mod-wide {@link Config#enableFeedTask} feature switch, and</li>
 *     <li>the per-maid "Feed owner" toggle in {@link MaidReviveConfig.Data#feedOwner()}.</li>
 * </ul>
 * The "don't feed a fallen owner" rule lives in {@link OwnerFeedPolicy#isFood}, not here.
 */
public class MaidFeedOwnerBehavior extends MaidFeedOwnerTask {
    private static final int CLOSE_ENOUGH_DIST = 2;
    private static final float WALK_SPEED = 0.6f;

    public MaidFeedOwnerBehavior() {
        super(new OwnerFeedPolicy(), CLOSE_ENOUGH_DIST, WALK_SPEED);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!Config.enableFeedTask) return false;
        MaidReviveConfig.Data data = maid.getOrCreateData(MaidReviveConfig.KEY, MaidReviveConfig.Data.getDefault());
        if (!data.feedOwner()) return false;
        return super.checkExtraStartConditions(level, maid);
    }
}
