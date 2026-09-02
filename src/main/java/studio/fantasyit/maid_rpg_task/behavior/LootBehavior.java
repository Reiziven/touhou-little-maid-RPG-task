package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraftforge.registries.ForgeRegistries;
import studio.fantasyit.maid_rpg_task.Config;

import java.util.*;

/**
 * Handles the DPS Leech ability:
 * - 5% chance on each hit to steal one beneficial effect from the target.
 * - Stolen effects are applied to the maid capped at leechMaxStacks levels
 *   and expire after leechDurationTicks.
 * - Steal and cleanse each have independent blacklist/whitelist filters.
 * - Stolen effect limit is level 5 (amplifier 4); duration is 5 minutes (6000 ticks).
 * - Cleanse on the target has no stack limit.
 *
 * Hit processing is done via LootBehavior.tryLeech(), called from MaidEventHandler.
 */
public class LootBehavior extends Behavior<EntityMaid> {

    /** Stolen effect entries tracked per maid instance. */
    private final List<StolenEntry> stolenEffects = new ArrayList<>();

    public LootBehavior() {
        super(Map.of());
    }

    // -------------------------------------------------------------------------
    // Behavior lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return Config.dpsLeechEnabled;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return Config.dpsLeechEnabled;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        studio.fantasyit.maid_rpg_task.event.MaidEventHandler.lootBehaviors.put(maid.getUUID(), this);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!Config.dpsLeechEnabled) return;
        // Expire stolen effects and re-apply still-valid ones
        long now = level.getGameTime();
        stolenEffects.removeIf(e -> now >= e.expiresAt);
        reapplyStolen(maid, now);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        studio.fantasyit.maid_rpg_task.event.MaidEventHandler.lootBehaviors.remove(maid.getUUID());
        // Remove all stolen effects from the maid when she leaves DPS task
        for (StolenEntry entry : stolenEffects) {
            maid.removeEffect(entry.effect);
        }
        stolenEffects.clear();
    }

    // -------------------------------------------------------------------------
    // Public API — called from MaidEventHandler on hit
    // -------------------------------------------------------------------------

    /**
     * Attempt to steal one beneficial effect from {@code target} and apply it
     * to {@code maid}. Called on every hit; has a 5% proc chance.
     */
    public void tryLeech(EntityMaid maid, LivingEntity target, ServerLevel level) {
        if (!Config.dpsLeechEnabled) return;
        if (maid.getRandom().nextFloat() >= 0.05f) return; // 5% chance

        // Collect stealable beneficial effects from the target
        List<MobEffectInstance> candidates = new ArrayList<>();
        for (MobEffectInstance inst : target.getActiveEffects()) {
            MobEffect effect = inst.getEffect();
            if (effect.getCategory() != MobEffectCategory.BENEFICIAL) continue;
            if (!canSteal(effect)) continue;
            candidates.add(inst);
        }
        if (candidates.isEmpty()) return;

        // Pick a random one
        MobEffectInstance chosen = candidates.get(maid.getRandom().nextInt(candidates.size()));
        MobEffect effect = chosen.getEffect();

        // Cleanse from target (no stack limit)
        if (canCleanse(effect)) {
            target.removeEffect(effect);
        }

        // Check maid's current stolen stack count for this effect
        long currentStacks = stolenEffects.stream().filter(e -> e.effect == effect).count();
        if (currentStacks >= Config.dpsLeechMaxStacks) return; // at cap

        int amplifier = Math.min(chosen.getAmplifier(), 4); // cap at level 5 (amplifier 4)
        long expiresAt = level.getGameTime() + Config.dpsLeechDurationTicks;
        stolenEffects.add(new StolenEntry(effect, amplifier, expiresAt));

        // Apply immediately
        int remainingTicks = (int) Math.min(Config.dpsLeechDurationTicks, Integer.MAX_VALUE);
        maid.addEffect(new MobEffectInstance(effect, remainingTicks, amplifier, false, true));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void reapplyStolen(EntityMaid maid, long now) {
        for (StolenEntry entry : stolenEffects) {
            int remaining = (int) Math.max(1, entry.expiresAt - now);
            MobEffectInstance current = maid.getEffect(entry.effect);
            // Only re-apply if it's missing or about to expire (within 5 ticks)
            if (current == null || current.getDuration() < 5) {
                maid.addEffect(new MobEffectInstance(entry.effect, remaining, entry.amplifier, false, true));
            }
        }
    }

    /** Returns true if this effect is allowed to be stolen (respects blacklist/whitelist). */
    private boolean canSteal(MobEffect effect) {
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (id == null) return false;
        String idStr = id.toString();
        boolean inList = Config.dpsLeechStealList.contains(idStr);
        return Config.dpsLeechStealWhitelistMode ? inList : !inList;
    }

    /** Returns true if this effect should be cleansed from the target (respects blacklist/whitelist). */
    private boolean canCleanse(MobEffect effect) {
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (id == null) return false;
        String idStr = id.toString();
        boolean inList = Config.dpsLeechCleanseList.contains(idStr);
        return Config.dpsLeechCleanseWhitelistMode ? inList : !inList;
    }

    // -------------------------------------------------------------------------
    // Inner record
    // -------------------------------------------------------------------------

    private static class StolenEntry {
        final MobEffect effect;
        final int amplifier;
        final long expiresAt;

        StolenEntry(MobEffect effect, int amplifier, long expiresAt) {
            this.effect = effect;
            this.amplifier = amplifier;
            this.expiresAt = expiresAt;
        }
    }
}
