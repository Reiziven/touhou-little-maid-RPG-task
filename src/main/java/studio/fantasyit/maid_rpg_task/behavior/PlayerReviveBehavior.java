package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.data.MaidReviveConfig;
import studio.fantasyit.maid_rpg_task.util.InvUtil;
import studio.fantasyit.maid_rpg_task.util.WrappedMaidFakePlayer;
import team.creative.playerrevive.PlayerRevive;
import team.creative.playerrevive.api.IBleeding;
import team.creative.playerrevive.server.PlayerReviveServer;

import java.util.*;

public class PlayerReviveBehavior extends Behavior<EntityMaid> {
    protected static class TryAttackMaidGoal extends TargetGoal {
        private final EntityMaid maid;

        public TryAttackMaidGoal(Mob p_26140_, EntityMaid maid) {
            super(p_26140_, true);
            this.maid = maid;
        }

        @Override
        public boolean canUse() {
            return mob.canAttack(maid);
        }

        @Override
        public void start() {
            mob.setTarget(maid);
            super.start();
        }

        public boolean isMaid(EntityMaid maid) {
            return maid.getUUID().equals(this.maid.getUUID());
        }
    }

    private static final String REVIVE_COOLDOWN_KEY = "maid_rpg_task_revive_time";

    /** True when this instance is used by the master task — always revives (no cooldown), but if elixir present consumes its durability. */
    private final boolean isMaster;

    public PlayerReviveBehavior() {
        this(false);
    }

    public PlayerReviveBehavior(boolean isMaster) {
        super(Map.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT), 600);
        this.isMaster = isMaster;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Optional<NearestVisibleLivingEntities> memory = maid.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        boolean hasElixir = hasUltramarineElixir(maid);
        return memory.map(list -> list
                .find(entity -> entity instanceof Player)
                .map(ep -> (ServerPlayer) ep)
                .filter(sp -> PlayerReviveServer.getBleeding(sp).isBleeding())
                // support without elixir: skip players still on per-player cooldown
                .filter(sp -> isMaster || hasElixir || !Config.enableReviveCooldown || !isPlayerOnCooldown(sp, level))
                .anyMatch(sp -> true)
        ).orElse(false);
    }

    ServerPlayer targetPlayer;
    IBleeding bleeding;
    boolean startedRevive;
    Set<UUID> aggroEntities;

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        aggroEntities = new HashSet<>();
        startedRevive = false;
        boolean ownerOnly = maid.getOrCreateData(MaidReviveConfig.KEY, MaidReviveConfig.Data.getDefault()).ownerOnly();
        LivingEntity owner = maid.getOwner();
        boolean hasElixir = hasUltramarineElixir(maid);
        Optional<NearestVisibleLivingEntities> memory = maid.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        targetPlayer = memory.flatMap(list -> list
                .find(entity -> entity instanceof Player)
                .map(ep -> (ServerPlayer) ep)
                .filter(sp -> (owner != null && sp.is(owner)) || !ownerOnly)
                .filter(sp -> PlayerReviveServer.getBleeding(sp).isBleeding())
                // support without elixir: skip players still on per-player cooldown
                .filter(sp -> isMaster || hasElixir || !Config.enableReviveCooldown || !isPlayerOnCooldown(sp, level))
                .findFirst()
        ).orElse(null);
        if (targetPlayer != null) {
            bleeding = PlayerReviveServer.getBleeding(targetPlayer);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, targetPlayer, 0.5f, 2);
        }
        useTotemOfUndying(level, maid);
    }

    private void useTotemOfUndying(ServerLevel level, EntityMaid maid) {
        if (!Config.enableReviveTotem) return;
        ItemStack itemstack = InvUtil.tryExtractOneMatches(maid.getMaidBauble(), (stack) -> stack.is(Items.TOTEM_OF_UNDYING));
        if (!itemstack.isEmpty()) {
            targetPlayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING), 1);
            CriteriaTriggers.USED_TOTEM.trigger(targetPlayer, itemstack);

            targetPlayer.setHealth(1.0F);
            targetPlayer.removeAllEffects();
            targetPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2500, 1));
            targetPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 3000, 1));
            targetPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            level.broadcastEntityEvent(targetPlayer, (byte) 35);

            PlayerReviveServer.revive(targetPlayer);
        }
    }

    private void checkCanReviveAndStartRevive(ServerLevel level, EntityMaid maid) {
        if (PlayerRevive.CONFIG.revive.needReviveItem) {
            if (PlayerRevive.CONFIG.revive.consumeReviveItem && !bleeding.isItemConsumed()) {
                ItemStack extractedForConsume = InvUtil.tryExtractOneMatches(maid.getAvailableInv(true), PlayerRevive.CONFIG.revive.reviveItem::is);
                if (!PlayerRevive.CONFIG.revive.reviveItem.is(extractedForConsume)) {
                    targetPlayer = null;
                    return;
                }

                bleeding.setItemConsumed();
            }
        }

        PlayerReviveServer.removePlayerAsHelper(WrappedMaidFakePlayer.get(maid));
        bleeding.revivingPlayers().add(WrappedMaidFakePlayer.get(maid));
        aggroEntitiesAround(level, maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (targetPlayer == null) return false;
        if (targetPlayer.distanceTo(maid) > PlayerRevive.CONFIG.revive.maxDistance) return false;
        return bleeding.isBleeding();
    }

    protected void aggroEntitiesAround(ServerLevel level, EntityMaid maid) {
        if (!Config.enableReviveAggro) return;
        List<Monster> entities = level.getEntities(EntityTypeTest.forClass(Monster.class),
                AABB.ofSize(maid.position(), 16, 16, 16),
                entity -> true
        );
        for (Monster entity : entities) {
            if (!aggroEntities.contains(entity.getUUID())) {
                entity.targetSelector.addGoal(10, new TryAttackMaidGoal(entity, maid));
                aggroEntities.add(entity.getUUID());
            }
        }
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        super.tick(level, maid, p_22553_);
        if (p_22553_ % 20 == 0)
            BehaviorUtils.setWalkAndLookTargetMemories(maid, targetPlayer, 0.5f, 2);
        if (!startedRevive) {
            if (maid.distanceTo(targetPlayer) < PlayerRevive.CONFIG.revive.maxDistance) {
                checkCanReviveAndStartRevive(level, maid);
                startedRevive = true;
            }
        } else {
            if (p_22553_ % 20 == 0)
                aggroEntitiesAround(level, maid);
        }
    }

    @Override
    protected void stop(ServerLevel p_22548_, EntityMaid maid, long p_22550_) {
        PlayerReviveServer.removePlayerAsHelper(WrappedMaidFakePlayer.get(maid));
        if (targetPlayer != null && !bleeding.isBleeding()) {
            targetPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
            if (Config.enableReviveCooldown) {
                if (consumeUltramarineElixirDurability(maid)) {
                    // elixir consumed — no cooldown penalty for either task
                } else if (!isMaster) {
                    // support without elixir: record cooldown on the player
                    setPlayerReviveCooldown(targetPlayer, p_22548_.getGameTime());
                }
                // master without elixir: no cooldown, free revive
            }
        }
        for (UUID uuid : aggroEntities) {
            Entity entity = p_22548_.getEntity(uuid);
            if (entity instanceof Monster monster && entity.isAlive())
                monster.targetSelector.removeAllGoals(g -> g instanceof TryAttackMaidGoal tg && tg.isMaid(maid));
        }
    }

    /** Returns true if the maid has an Ultramarine Orb Elixir with remaining durability in its bauble slots. */
    private boolean hasUltramarineElixir(EntityMaid maid) {
        var baubleInv = maid.getMaidBauble();
        for (int i = 0; i < baubleInv.getSlots(); i++) {
            ItemStack stack = baubleInv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(InitItems.ULTRAMARINE_ORB_ELIXIR.get())
                    && stack.getDamageValue() < stack.getMaxDamage()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Damages the first Ultramarine Orb Elixir in the maid's bauble inventory by 1.
     * Removes it if fully depleted. Returns true if durability was consumed.
     */
    private boolean consumeUltramarineElixirDurability(EntityMaid maid) {
        var baubleInv = maid.getMaidBauble();
        for (int i = 0; i < baubleInv.getSlots(); i++) {
            ItemStack stack = baubleInv.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(InitItems.ULTRAMARINE_ORB_ELIXIR.get())) continue;
            stack.setDamageValue(stack.getDamageValue() + 1);
            if (stack.getDamageValue() >= stack.getMaxDamage()) {
                baubleInv.extractItem(i, 1, false);
            }
            return true;
        }
        return false;
    }

    /** Returns true if this player was revived recently and is still within the support cooldown window. */
    private boolean isPlayerOnCooldown(ServerPlayer player, ServerLevel level) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(REVIVE_COOLDOWN_KEY)) return false;
        return level.getGameTime() - data.getLong(REVIVE_COOLDOWN_KEY) < Config.supportReviveCooldown;
    }

    /** Stamps the current game time onto the player so support maids respect the cooldown. */
    private void setPlayerReviveCooldown(ServerPlayer player, long gameTime) {
        player.getPersistentData().putLong(REVIVE_COOLDOWN_KEY, gameTime);
    }
}
