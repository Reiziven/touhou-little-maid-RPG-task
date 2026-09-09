package studio.fantasyit.maid_rpg_task.entity;

import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_rpg_task.Config;

import java.util.UUID;

/**
 * Heal circle — three visual styles, chosen by Config.supportHealStyle:
 *
 *   "spiral"        — DustColorTransition arms (deep crimson → pink) that spiral inward.
 *                     Color transitions immediately start pink-ward since the lifetime of
 *                     each dust particle is short (~10 ticks), so early arms are red and
 *                     later ones are pink.
 *
 *   "end_rod"       — classic white END_ROD spiral arms (original design).
 *
 *   "instant_health" — static ring of INSTANT_EFFECT particles that pulses outward then
 *                      snaps to a tight ring at the moment of heal.
 *
 * All styles:
 *   - Heal fires at tick 40 (2 s in).
 *   - Small HEART + INSTANT_EFFECT burst at heal moment.
 *   - No fade particles after heal.
 *   - Total lifetime: 50 ticks.
 */
public class HealMagicCircleEntity extends Entity {

    public static final int LIFETIME_TICKS = 50;
    private static final int HEAL_TICK = 35;

    private static final double START_RADIUS = 2.0;
    private static final int ARMS = 6;

    // Dust spiral colors — transition starts crimson, ends vivid pink
    private static final org.joml.Vector3f COL_FROM = new org.joml.Vector3f(0.80f, 0.05f, 0.15f);
    private static final org.joml.Vector3f COL_TO   = new org.joml.Vector3f(1.00f, 0.45f, 0.65f);

    private UUID targetUuid;
    private float healAmount;
    private boolean hasHealed = false;
    private int ticksAlive = 0;

    public HealMagicCircleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    // resolved style for this instance — set once at spawn time
    private String resolvedStyle = "spiral";

    public void setTarget(LivingEntity target, float healAmount) {
        this.targetUuid = target.getUUID();
        this.healAmount = healAmount;
        this.moveTo(target.getX(), target.getY(), target.getZ());
        // Resolve cycle at spawn so the style is stable for this circle's lifetime
        String cfg = Config.supportHealStyle;
        if ("cycle".equals(cfg)) {
            resolvedStyle = random.nextBoolean() ? "spiral" : "end_rod";
        } else {
            resolvedStyle = cfg;
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) { return false; }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        LivingEntity target = findTarget();
        if (target != null) this.setPos(target.getX(), target.getY(), target.getZ());

        if (!level().isClientSide) {
            String style = resolvedStyle;
            if (ticksAlive <= HEAL_TICK) {
                switch (style) {
                    case "end_rod"      -> spawnEndRodSpiral();
                    case "entity_effect"-> spawnEntityEffectRing(target);
                    default             -> spawnDustSpiral();
                }
            }

            if (!hasHealed && ticksAlive >= HEAL_TICK) {
                applyHeal(target);
                hasHealed = true;
            }
        }

        if (ticksAlive >= LIFETIME_TICKS) this.discard();
    }

    // -------------------------------------------------------------------------
    // Heal application
    // -------------------------------------------------------------------------

    private void applyHeal(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        target.heal(healAmount);

        if (!(level() instanceof ServerLevel sl)) return;

        sl.playSound(null, target.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.5f, 1.5f);

        // Burst: alternating HEART and INSTANT_EFFECT in a tight ring
        for (int i = 0; i < ARMS * 2; i++) {
            double angle = (2 * Math.PI / (ARMS * 2)) * i;
            double px = target.getX() + Math.cos(angle) * 0.55;
            double pz = target.getZ() + Math.sin(angle) * 0.55;
            double py = target.getY() + 0.7;
            if (i % 2 == 0) {
                sl.sendParticles(ParticleTypes.HEART, px, py, pz, 1, 0, 0.04, 0, 0);
            } else {
                sl.sendParticles(ParticleTypes.INSTANT_EFFECT, px, py, pz, 1, 0, 0, 0, 0);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Style: "spiral" — DustColorTransition, deep crimson → vivid pink
    // Each arm uses a slightly different size so the transition reads quickly
    // -------------------------------------------------------------------------

    private void spawnDustSpiral() {
        if (!(level() instanceof ServerLevel sl)) return;

        float progress = ticksAlive / (float) HEAL_TICK;
        double radius = START_RADIUS * (1.0 - progress);
        double baseAngle = ticksAlive * (2 * Math.PI / 35.0); // one rotation in 35 ticks

        // Bias: later progress = particles spawned closer to pink.
        // We control this by interpolating the FROM color toward TO so early ticks
        // are red and later ticks are already halfway to pink at spawn.
        float bias = Math.min(progress * 1.4f, 1.0f); // reaches 1 by ~70% through
        org.joml.Vector3f fromBiased = new org.joml.Vector3f(
                COL_FROM.x + (COL_TO.x - COL_FROM.x) * bias,
                COL_FROM.y + (COL_TO.y - COL_FROM.y) * bias,
                COL_FROM.z + (COL_TO.z - COL_FROM.z) * bias
        );
        var transition = new DustColorTransitionOptions(fromBiased, COL_TO, 1.4f);

        for (int arm = 0; arm < ARMS; arm++) {
            double angle = baseAngle + (2 * Math.PI / ARMS) * arm;
            double px = getX() + Math.cos(angle) * radius;
            double pz = getZ() + Math.sin(angle) * radius;
            double vx = (getX() - px) * 0.04;
            double vz = (getZ() - pz) * 0.04;
            sl.sendParticles(transition, px, getY() + 0.05, pz, 1, vx, 0.01, vz, 0.0);
        }
    }

    // -------------------------------------------------------------------------
    // Style: "end_rod" — white spiral arms
    // -------------------------------------------------------------------------

    private void spawnEndRodSpiral() {
        if (!(level() instanceof ServerLevel sl)) return;

        float progress = ticksAlive / (float) HEAL_TICK;
        double radius = START_RADIUS * (1.0 - progress);
        double baseAngle = ticksAlive * (2 * Math.PI / 35.0);

        for (int arm = 0; arm < ARMS; arm++) {
            double angle = baseAngle + (2 * Math.PI / ARMS) * arm;
            double px = getX() + Math.cos(angle) * radius;
            double pz = getZ() + Math.sin(angle) * radius;
            double vx = (getX() - px) * 0.04;
            double vz = (getZ() - pz) * 0.04;
            sl.sendParticles(ParticleTypes.END_ROD, px, getY() + 0.05, pz, 1, vx, 0.01, vz, 0.0);
        }
    }

    // -------------------------------------------------------------------------
    // Style: "entity_effect" — hollow ring of Instant Health colored ENTITY_EFFECT
    // particles (#F82423 = 0.973, 0.141, 0.137). count=0 makes delta become RGB.
    // Ring stays at a fixed radius with a slow rotation so it reads as a circle.
    // -------------------------------------------------------------------------

    private void spawnEntityEffectRing(LivingEntity target) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (target == null) return;

        // Fixed radius — hollow circle around the target's feet
        double radius = 1.5;
        int points = 16;
        // Slow rotation so it doesn't look completely static
        double baseAngle = ticksAlive * (Math.PI / 60.0);

        // Instant Health red: #F82423 → R=0.973, G=0.141, B=0.137
        double r = 0.973, g = 0.141, b = 0.137;

        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (2 * Math.PI / points) * i;
            double px = target.getX() + Math.cos(angle) * radius;
            double pz = target.getZ() + Math.sin(angle) * radius;
            // count=0: delta XYZ is interpreted as RGB color by the client
            sl.sendParticles(ParticleTypes.ENTITY_EFFECT,
                    px, target.getY() + 0.1, pz,
                    0, r, g, b, 1.0);
        }
    }

    // -------------------------------------------------------------------------

    private LivingEntity findTarget() {
        if (targetUuid == null) return null;
        AABB box = new AABB(getX() - 64, getY() - 32, getZ() - 64,
                getX() + 64, getY() + 32, getZ() + 64);
        return level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.getUUID().equals(targetUuid))
                .stream().findFirst().orElse(null);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ticksAlive = tag.getInt("TicksAlive");
        this.hasHealed = tag.getBoolean("HasHealed");
        this.healAmount = tag.getFloat("HealAmount");
        if (tag.hasUUID("TargetUuid")) this.targetUuid = tag.getUUID("TargetUuid");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksAlive", this.ticksAlive);
        tag.putBoolean("HasHealed", this.hasHealed);
        tag.putFloat("HealAmount", this.healAmount);
        if (targetUuid != null) tag.putUUID("TargetUuid", targetUuid);
    }
}
