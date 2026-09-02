package studio.fantasyit.maid_rpg_task.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.behavior.LootBehavior;
import studio.fantasyit.maid_rpg_task.effect.ReducedHealingEffect;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;
import studio.fantasyit.maid_rpg_task.task.MaidDPSTask;
import studio.fantasyit.maid_rpg_task.task.MaidMasterTask;
import studio.fantasyit.maid_rpg_task.task.MaidTankTask;

import java.util.*;

@Mod.EventBusSubscriber
public class MaidEventHandler {

    /** Accumulated redirected damage to apply to the tank at end of tick. */
    private static final Map<UUID, Float> pendingDamage = new HashMap<>();

    /**
     * Re-entry guard: when we apply redirected damage to the tank via hurt(),
     * that fires LivingHurtEvent again. This flag prevents processing that
     * secondary event so the tank doesn't get double-reduced.
     */
    private static boolean applyingRedirect = false;

    /**
     * Registry of LootBehavior instances, keyed by maid UUID.
     * Populated by LootBehavior itself via register/unregister.
     */
    public static final Map<UUID, LootBehavior> lootBehaviors = new HashMap<>();

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();

        // --- Leech proc: check if attacker is a DPS maid ---
        if (Config.dpsLeechEnabled && !applyingRedirect) {
            DamageSource src = event.getSource();
            if (src.getEntity() instanceof EntityMaid attackerMaid
                    && attackerMaid.getTask() != null
                    && (attackerMaid.getTask().getUid().equals(MaidDPSTask.UID)
                        || attackerMaid.getTask().getUid().equals(MaidMasterTask.UID))
                    && attackerMaid.level() instanceof ServerLevel sLevel) {
                LootBehavior loot = lootBehaviors.get(attackerMaid.getUUID());
                if (loot != null) {
                    loot.tryLeech(attackerMaid, target, sLevel);
                }
            }
        }

        // --- Case 1: tank is hit directly — apply her own personal damage reduction ---
        if (target instanceof EntityMaid tankMaid
                && tankMaid.getTask() != null
                && tankMaid.getTask().getUid().equals(MaidTankTask.UID)
                && !applyingRedirect) {

            double reduction = Config.tankDirectReduction;
            if (reduction > 0.0) {
                event.setAmount(event.getAmount() * (float)(1.0 - reduction));
            }
            return; // no redirect needed when the tank is the direct target
        }

        // Skip events we ourselves triggered (redirect application)
        if (applyingRedirect) return;

        // --- Case 2: an ally (owner or pet) is hit — redirect a portion to the tank ---
        Player owner;
        if (target instanceof Player player) {
            owner = player;
        } else if (target instanceof TamableAnimal pet && pet.getOwner() instanceof Player p) {
            owner = p;
        } else {
            owner = null;
        }

        if (owner == null || !(target.level() instanceof ServerLevel level)) return;

        AABB box = owner.getBoundingBox().inflate(16);
        List<EntityMaid> maids = level.getEntitiesOfClass(
                EntityMaid.class, box,
                maid -> maid.isAlive()
                        && maid.getOwner() == owner
                        && maid.getTask() != null
                        && (maid.getTask().getUid().equals(MaidTankTask.UID)
                            || maid.getTask().getUid().equals(MaidMasterTask.UID))
        );

        if (maids.isEmpty()) return;

        // Choose closest maid
        EntityMaid maid = maids.stream()
                .min(Comparator.comparingDouble(m -> m.distanceToSqr(target)))
                .orElse(maids.get(0));

        float originalDamage = event.getAmount();

        // Ally receives their configured fraction of the original hit
        float allyReceives = originalDamage * (float) Config.allyDamageTaken;
        event.setAmount(allyReceives);

        float tankReceives = originalDamage * (float) Config.tankAbsorbs;
        pendingDamage.merge(maid.getUUID(), tankReceives, Float::sum);
    }

    /** Reduce healing on entities affected by ReducedHealing effect. */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(EffectRegistry.REDUCED_HEALING.get())) {
            int amplifier = entity.getEffect(EffectRegistry.REDUCED_HEALING.get()).getAmplifier();
            event.setAmount(event.getAmount() * ReducedHealingEffect.healMultiplier(amplifier));
        }
    }

    /** Apply redirected damage to the tank at end of server tick, guarded against re-entry. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingDamage.isEmpty()) return;

        MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        applyingRedirect = true;
        try {
            for (ServerLevel level : server.getAllLevels()) {
                for (EntityMaid maid : level.getEntitiesOfClass(EntityMaid.class,
                        new AABB(-30000, -300, -30000, 30000, 300, 30000))) {
                    Float damage = pendingDamage.get(maid.getUUID());
                    if (damage == null || damage <= 0) continue;
                    if (maid.isAlive()) {
                        DamageSource source = level.damageSources().generic();
                        if (!maid.isInvulnerableTo(source)) {
                            maid.hurt(source, damage);
                        }
                    }
                }
            }
        } finally {
            applyingRedirect = false;
            pendingDamage.clear();
        }
    }
}
