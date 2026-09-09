package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_rpg_task.Config;

import java.util.*;

public class LootBehavior extends Behavior<EntityMaid> {

    private final List<StolenEntry> stolenEffects = new ArrayList<>();

    public LootBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return Config.dpsLeechEnabled;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return Config.dpsLeechEnabled;
    }

    @Override
    protected boolean timedOut(long gameTime) { return false; }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        studio.fantasyit.maid_rpg_task.event.MaidEventHandler.lootBehaviors.put(maid.getUUID(), this);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!Config.dpsLeechEnabled) return;
        long now = level.getGameTime();
        stolenEffects.removeIf(e -> now >= e.expiresAt);
        reapplyStolen(maid, now);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        studio.fantasyit.maid_rpg_task.event.MaidEventHandler.lootBehaviors.remove(maid.getUUID());
        for (StolenEntry entry : stolenEffects) maid.removeEffect(entry.effect);
        stolenEffects.clear();
    }

    public void tryLeech(EntityMaid maid, LivingEntity target, ServerLevel level) {
        if (!Config.dpsLeechEnabled) return;
        if (maid.getRandom().nextFloat() >= 0.05f) return;

        List<MobEffectInstance> candidates = new ArrayList<>();
        for (MobEffectInstance inst : target.getActiveEffects()) {
            MobEffect effect = inst.getEffect().value();
            if (effect.getCategory() != MobEffectCategory.BENEFICIAL) continue;
            if (!canSteal(inst.getEffect())) continue;
            candidates.add(inst);
        }
        if (candidates.isEmpty()) return;

        MobEffectInstance chosen = candidates.get(maid.getRandom().nextInt(candidates.size()));
        var effectHolder = chosen.getEffect();
        MobEffect effect = effectHolder.value();

        if (canCleanse(effectHolder)) target.removeEffect(effectHolder);

        long currentStacks = stolenEffects.stream().filter(e -> e.effect == effectHolder).count();
        if (currentStacks >= Config.dpsLeechMaxStacks) return;

        int amplifier = Math.min(chosen.getAmplifier(), 4);
        long expiresAt = level.getGameTime() + Config.dpsLeechDurationTicks;
        stolenEffects.add(new StolenEntry(effectHolder, amplifier, expiresAt));

        int remainingTicks = (int) Math.min(Config.dpsLeechDurationTicks, Integer.MAX_VALUE);
        maid.addEffect(new MobEffectInstance(effectHolder, remainingTicks, amplifier, false, true));
    }

    private void reapplyStolen(EntityMaid maid, long now) {
        for (StolenEntry entry : stolenEffects) {
            int remaining = (int) Math.max(1, entry.expiresAt - now);
            MobEffectInstance current = maid.getEffect(entry.effect);
            if (current == null || current.getDuration() < 5) {
                maid.addEffect(new MobEffectInstance(entry.effect, remaining, entry.amplifier, false, true));
            }
        }
    }

    private boolean canSteal(net.minecraft.core.Holder<MobEffect> effectHolder) {
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effectHolder.value());
        if (id == null) return false;
        String idStr = id.toString();
        boolean inList = Config.dpsLeechStealList.contains(idStr);
        return Config.dpsLeechStealWhitelistMode ? inList : !inList;
    }

    private boolean canCleanse(net.minecraft.core.Holder<MobEffect> effectHolder) {
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effectHolder.value());
        if (id == null) return false;
        String idStr = id.toString();
        boolean inList = Config.dpsLeechCleanseList.contains(idStr);
        return Config.dpsLeechCleanseWhitelistMode ? inList : !inList;
    }

    private static class StolenEntry {
        final net.minecraft.core.Holder<MobEffect> effect;
        final int amplifier;
        final long expiresAt;

        StolenEntry(net.minecraft.core.Holder<MobEffect> effect, int amplifier, long expiresAt) {
            this.effect = effect;
            this.amplifier = amplifier;
            this.expiresAt = expiresAt;
        }
    }
}
