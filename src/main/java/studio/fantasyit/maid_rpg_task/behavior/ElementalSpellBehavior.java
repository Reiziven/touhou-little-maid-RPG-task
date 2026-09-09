package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.data.MaidMageData;
import studio.fantasyit.maid_rpg_task.entity.*;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;

import java.util.Optional;

public class ElementalSpellBehavior extends Behavior<EntityMaid> {

    private static final double EARTH_MELEE_RANGE = 6.0;
    private static final int SPELL_EARTH   = 0;
    private static final int SPELL_ICE     = 1;
    private static final int SPELL_TORNADO = 2;
    private static final int SPELL_FIRE    = 3;
    private static final int SPELL_COUNT   = 4;

    public ElementalSpellBehavior() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
    }

    @Override
    protected boolean timedOut(long gameTime) { return false; }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        MaidMageData.Data data = maid.getOrCreateData(MaidMageData.KEY, MaidMageData.Data.getDefault());

        int cooldown = data.getElementalCooldown();
        if (cooldown > 0) {
            data.setElementalCooldown(cooldown - 1);
            return;
        }

        Optional<LivingEntity> targetOpt = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (targetOpt.isEmpty()) return;
        LivingEntity target = targetOpt.get();
        if (!target.isAlive()) return;

        castNextSpell(level, maid, target, data);
        int min = Config.mageSpellCooldownMin;
        int max = Config.mageSpellCooldownMax;
        data.setElementalCooldown(min + maid.getRandom().nextInt(Math.max(1, max - min + 1)));
    }

    private void castNextSpell(ServerLevel level, EntityMaid maid, LivingEntity target, MaidMageData.Data data) {
        int spell = data.getSpellIndex() % SPELL_COUNT;
        switch (spell) {
            case SPELL_EARTH   -> castEarth(level, maid, target);
            case SPELL_ICE     -> castIce(level, maid, target);
            case SPELL_TORNADO -> castTornado(level, maid, target);
            case SPELL_FIRE    -> castFire(level, maid, target);
        }
        data.setSpellIndex(spell + 1);
    }

    private void castTornado(ServerLevel level, EntityMaid maid, LivingEntity target) {
        TornadoEntity tornado = new TornadoEntity(EntityRegistry.TORNADO.get(), level);
        tornado.moveTo(target.getX(), target.getY(), target.getZ(), maid.getYRot(), 0);
        tornado.setMaid(maid);
        level.addFreshEntity(tornado);
    }

    private void castEarth(ServerLevel level, EntityMaid maid, LivingEntity target) {
        if (maid.distanceTo(target) <= EARTH_MELEE_RANGE) castEarthSpiral(level, maid);
        else castEarthFront(level, maid, target);
    }

    private void castEarthSpiral(ServerLevel level, EntityMaid maid) {
        EarthSpikeEntity spike = new EarthSpikeEntity(EntityRegistry.EARTH_SPIKE.get(), level);
        spike.moveTo(maid.getX(), maid.getY(), maid.getZ(), maid.getYRot(), 0);
        spike.setOwner(maid, maid.getOwnerUUID());
        level.addFreshEntity(spike);
    }

    private void castEarthFront(ServerLevel level, EntityMaid maid, LivingEntity target) {
        FrontSpikeEntity spike = new FrontSpikeEntity(EntityRegistry.FRONT_SPIKE.get(), level);
        spike.moveTo(target.getX(), target.getY(), target.getZ(), maid.getYRot(), 0);
        spike.setOwner(maid, maid.getOwnerUUID());
        level.addFreshEntity(spike);
    }

    private void castFire(ServerLevel level, EntityMaid maid, LivingEntity target) {
        double dx = target.getX() - maid.getX();
        double dz = target.getZ() - maid.getZ();
        float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        FireMagicCircleEntity circle = new FireMagicCircleEntity(EntityRegistry.FIRE_MAGIC_CIRCLE.get(), level);
        circle.moveTo(maid.getX(), maid.getY() + maid.getBbHeight() * 0.5, maid.getZ(), yaw, 0);
        circle.setOwner(maid, maid.getOwnerUUID());
        circle.setLockedTarget(target);
        level.addFreshEntity(circle);
    }

    private void castIce(ServerLevel level, EntityMaid maid, LivingEntity target) {
        double dx = target.getX() - maid.getX();
        double dz = target.getZ() - maid.getZ();
        float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        IceMagicCircleEntity circle = new IceMagicCircleEntity(EntityRegistry.ICE_MAGIC_CIRCLE.get(), level);
        circle.moveTo(maid.getX(), maid.getY() + maid.getBbHeight() * 0.5, maid.getZ(), yaw, 0);
        circle.setOwner(maid, maid.getOwnerUUID());
        level.addFreshEntity(circle);
    }
}
