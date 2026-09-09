package studio.fantasyit.maid_rpg_task.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.util.MagicDamageHelper;
import studio.fantasyit.maid_rpg_task.util.SpellTargetHelper;

import java.util.List;
import java.util.UUID;

/**
 * Earth Spike Spiral variant — summoned at maid's position.
 * - Targets standing on the field take 2.5 damage every 10 ticks (0.5s), for up to 200 ticks (10s).
 * - Total potential damage: 50.
 * - On first contact per target: applies -20% armor debuff for 60 seconds (1200 ticks).
 * - All damage bypasses resistance and 50% armor.
 * - Summoned when target is within 4 blocks AND there are multiple targets nearby.
 */
public class EarthSpikeEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Integer> TICKS_ALIVE =
            SynchedEntityData.defineId(EarthSpikeEntity.class, EntityDataSerializers.INT);

    /** 10 seconds = 200 ticks */
    public static final int LIFETIME_TICKS = 200;
    /** 2.5 damage per 0.5s tick */
    public static final float DAMAGE_PER_HALF_SECOND = 3.0f;
    /** Damage tick interval */
    public static final int DAMAGE_INTERVAL = 10;
    /** Damage radius (horizontal) */
    public static final double DAMAGE_RANGE = 3.5;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private UUID ownerPlayerUuid;

    public EarthSpikeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner, UUID ownerPlayerUuid) {
        this.ownerUuid = owner.getUUID();
        this.ownerPlayerUuid = ownerPlayerUuid;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TICKS_ALIVE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        int ticks = this.entityData.get(TICKS_ALIVE) + 1;
        this.entityData.set(TICKS_ALIVE, ticks);

        // Deal damage every DAMAGE_INTERVAL ticks to targets standing in range
        if (ticks % DAMAGE_INTERVAL == 0) {
            List<LivingEntity> targets = getStandingTargets();

            for (LivingEntity target : targets) {
                // Refresh armor shred if not already active
                if (!target.hasEffect(studio.fantasyit.maid_rpg_task.registry.EffectRegistry.ARMOR_SHRED.get())) {
                    MagicDamageHelper.applyEarthArmorDebuff(target);
                }
                MagicDamageHelper.dealMagicDamage(target, this.damageSources().magic(), DAMAGE_PER_HALF_SECOND);
                // Slowness III for 1 second, invisible particles
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false));
            }
        }

        if (ticks >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    private List<LivingEntity> getStandingTargets() {
        AABB box = new AABB(
                getX() - DAMAGE_RANGE, getY() - 0.5, getZ() - DAMAGE_RANGE,
                getX() + DAMAGE_RANGE, getY() + 0.5, getZ() + DAMAGE_RANGE
        );
        LivingEntity maid = findMaid();
        return level().getEntitiesOfClass(LivingEntity.class, box,
                e -> SpellTargetHelper.isValidTarget(e, maid, ownerUuid, ownerPlayerUuid));
    }

    private LivingEntity findMaid() {
        if (ownerUuid == null) return null;
        return level().getEntitiesOfClass(LivingEntity.class,
                new AABB(getX() - 64, getY() - 32, getZ() - 64, getX() + 64, getY() + 32, getZ() + 64),
                e -> e.getUUID().equals(ownerUuid)).stream().findFirst().orElse(null);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(TICKS_ALIVE, tag.getInt("TicksAlive"));
        if (tag.hasUUID("OwnerUuid")) this.ownerUuid = tag.getUUID("OwnerUuid");
        if (tag.hasUUID("OwnerPlayerUuid")) this.ownerPlayerUuid = tag.getUUID("OwnerPlayerUuid");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksAlive", this.entityData.get(TICKS_ALIVE));
        if (ownerUuid != null) tag.putUUID("OwnerUuid", ownerUuid);
        if (ownerPlayerUuid != null) tag.putUUID("OwnerPlayerUuid", ownerPlayerUuid);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(
                    RawAnimation.begin().then("earth-spike.animation", Animation.LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static ResourceLocation getModelLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "geo/earth_spike_spiral.geo.json");
    }

    public static ResourceLocation getTextureLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "textures/entity/spiral_spike.png");
    }

    public static ResourceLocation getAnimationLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "animations/earth_spike.animation.json");
    }
}
