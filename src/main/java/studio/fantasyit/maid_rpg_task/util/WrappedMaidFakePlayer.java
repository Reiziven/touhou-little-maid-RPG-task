package studio.fantasyit.maid_rpg_task.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WrappedMaidFakePlayer extends FakePlayer {
    public static class WrappedMaidInventory extends Inventory {
        private final EntityMaid maid;

        public WrappedMaidInventory(EntityMaid maid, WrappedMaidFakePlayer fakePlayer) {
            super(fakePlayer);
            this.maid = maid;
        }

        @Override
        public @NotNull ItemStack getSelected() {
            return maid.getMainHandItem();
        }

        @Override
        public float getDestroySpeed(BlockState blockState) {
            return maid.getMainHandItem().getDestroySpeed(blockState);
        }
    }

    private static final ConcurrentHashMap<UUID, WrappedMaidFakePlayer> cache = new ConcurrentHashMap<>();
    private final EntityMaid maid;

    public static WrappedMaidFakePlayer get(EntityMaid maid) {
        if (cache.containsKey(maid.getUUID())) {
            WrappedMaidFakePlayer existing = cache.get(maid.getUUID());
            if (!existing.maid.isAlive()) {
                cache.remove(maid.getUUID());
            } else {
                return existing;
            }
        }
        WrappedMaidFakePlayer fakePlayer = new WrappedMaidFakePlayer(maid);
        cache.put(maid.getUUID(), fakePlayer);
        return fakePlayer;
    }

    private WrappedMaidFakePlayer(EntityMaid maid) {
        super((ServerLevel) maid.level(), new GameProfile(UUID.randomUUID(), maid.getName().getString()));
        this.maid = maid;
    }

    private final WrappedMaidInventory wrappedInventory = null; // lazy init below

    @Override
    public net.minecraft.world.entity.player.Inventory getInventory() {
        // Return a wrapped inventory that delegates mainhand to the maid
        return new WrappedMaidInventory(maid, this);
    }

    @Override
    public boolean removeEffect(net.minecraft.core.Holder<MobEffect> effect) {
        if (maid == null) return false;
        return maid.removeEffect(effect);
    }

    @Nullable
    @Override
    public MobEffectInstance removeEffectNoUpdate(net.minecraft.core.Holder<MobEffect> effect) {
        if (maid == null) return super.removeEffectNoUpdate(effect);
        return maid.removeEffectNoUpdate(effect);
    }

    @Override
    public boolean removeAllEffects() {
        if (maid == null) return false;
        return maid.removeAllEffects();
    }

    @Override
    public boolean addEffect(MobEffectInstance instance, @Nullable Entity source) {
        if (maid == null) return super.addEffect(instance, source);
        return maid.addEffect(instance, source);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance instance) {
        if (maid == null) return super.canBeAffected(instance);
        return maid.canBeAffected(instance);
    }

    @Override
    public void forceAddEffect(MobEffectInstance instance, @Nullable Entity source) {
        maid.forceAddEffect(instance, source);
    }

    @Nullable
    @Override
    public MobEffectInstance getEffect(net.minecraft.core.Holder<MobEffect> effect) {
        if (maid == null) return super.getEffect(effect);
        return maid.getEffect(effect);
    }

    @Override
    public Collection<MobEffectInstance> getActiveEffects() {
        if (maid == null) return super.getActiveEffects();
        return maid.getActiveEffects();
    }

    @Override
    public Map<net.minecraft.core.Holder<MobEffect>, MobEffectInstance> getActiveEffectsMap() {
        if (maid == null) return super.getActiveEffectsMap();
        return maid.getActiveEffectsMap();
    }

    @Override
    public boolean hasEffect(net.minecraft.core.Holder<MobEffect> effect) {
        if (maid == null) return super.hasEffect(effect);
        return maid.hasEffect(effect);
    }

    @Override
    public ItemStack getMainHandItem() {
        if (maid == null) return ItemStack.EMPTY;
        return maid.getMainHandItem();
    }

    @Override
    public ItemStack getItemInHand(InteractionHand hand) {
        if (maid == null) return super.getItemInHand(hand);
        return maid.getItemInHand(hand);
    }

    @Override
    public void setItemInHand(InteractionHand hand, ItemStack stack) {
        if (maid == null) return;
        maid.setItemInHand(hand, stack);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        if (maid == null) return;
        maid.setItemSlot(slot, stack);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        if (maid == null) return ItemStack.EMPTY;
        return maid.getItemBySlot(slot);
    }

    @Override
    public boolean isEyeInFluid(TagKey<Fluid> tag) {
        if (maid == null) return false;
        return maid.isEyeInFluid(tag);
    }

    @Override
    public boolean onGround() {
        if (maid == null) return false;
        return maid.onGround();
    }

    @Override
    public Level level() {
        if (maid == null) return super.level();
        return maid.level();
    }

    @Override
    public ServerLevel serverLevel() {
        if (maid == null) return super.serverLevel();
        return (ServerLevel) maid.level();
    }

    @Override
    public BlockPos blockPosition() {
        if (maid == null) return BlockPos.ZERO;
        return maid.blockPosition();
    }

    @Override
    public Vec3 position() {
        if (maid == null) return Vec3.ZERO;
        return maid.position();
    }

    @Override
    public float distanceTo(Entity entity) {
        if (maid == null) return super.distanceTo(entity);
        return maid.distanceTo(entity);
    }

    @Override
    public double distanceToSqr(double x, double y, double z) {
        if (maid == null) return super.distanceToSqr(x, y, z);
        return maid.distanceToSqr(x, y, z);
    }

    @Override
    public double distanceToSqr(Vec3 vec) {
        if (maid == null) return super.distanceToSqr(vec);
        return maid.distanceToSqr(vec);
    }

    @Override
    public void teleportTo(double x, double y, double z) {
        if (maid == null) return;
        maid.teleportTo(x, y, z);
    }

    @Override
    public boolean teleportTo(ServerLevel level, double x, double y, double z,
                               Set<RelativeMovement> relatives, float yaw, float pitch) {
        if (maid == null) return false;
        return maid.teleportTo(level, x, y, z, relatives, yaw, pitch);
    }

    @Override
    public void teleportRelative(double dx, double dy, double dz) {
        if (maid == null) return;
        maid.teleportRelative(dx, dy, dz);
    }

    @Override
    public void moveTo(double x, double y, double z) {
        if (maid == null) return;
        maid.moveTo(x, y, z);
    }

    @Override
    public ChunkPos chunkPosition() {
        if (maid == null) return new ChunkPos(0, 0);
        return maid.chunkPosition();
    }
}
