package studio.fantasyit.maid_rpg_task.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.*;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuShoot;
import com.github.tartaricacid.touhoulittlemaid.datagen.EnchantmentKeys;
import com.github.tartaricacid.touhoulittlemaid.item.ItemHakureiGohei;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import studio.fantasyit.maid_rpg_task.util.EnchantUtil;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.behavior.*;
import studio.fantasyit.maid_rpg_task.compat.PlayerRevive;
import studio.fantasyit.maid_rpg_task.compat.PlayerReviveProxy;
import studio.fantasyit.maid_rpg_task.menu.MaidReviveConfigGui;

import java.util.ArrayList;
import java.util.List;

public class MaidSupportTask implements IRangedAttackTask {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "support");

    @Override
    public ResourceLocation getUid() { return UID; }

    @Override
    public ItemStack getIcon() { return Items.POTION.getDefaultInstance(); }

    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) { return null; }

    @Override
    public boolean isEnable(EntityMaid maid) { return Config.enableSupportTask; }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) { return false; }

    @Override
    public boolean enablePanic(EntityMaid maid) { return false; }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        ArrayList<Pair<Integer, BehaviorControl<? super EntityMaid>>> ret = new ArrayList<>();
        if (PlayerRevive.isEnable()) {
            ret.add(Pair.of(1, PlayerReviveProxy.createBehavior()));
        }
        ret.add(Pair.of(1, new SupportEffectBehavior()));
        if (Config.enableFeedTask) {
            ret.add(Pair.of(5, new MaidFeedOwnerBehavior()));
        }
        if (Config.supportCanAttack) {
            ret.add(Pair.of(2, new MaidSwitchWeaponBehavior()));
            ret.add(Pair.of(5, StartAttacking.create(
                    e -> hasAnyWeapon(e) && (!isRangedEquipped(e) || hasArrows(e)),
                    IRangedAttackTask::findFirstValidAttackTarget)));
            ret.add(Pair.of(5, StopAttackingIfTargetInvalid.create(target ->
                    farAway(target, maid) || !hasAnyWeapon(maid) || (isRangedEquipped(maid) && !hasArrows(maid)))));
            ret.add(Pair.of(5, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(0.6f)));
            ret.add(Pair.of(4, new MaidAttackStrafingTask()));
            ret.add(Pair.of(4, new MaidShootTargetTask(10)));
            ret.add(Pair.of(4, new MaidCrossbowAttack()));
            ret.add(Pair.of(4, new GoheiAttackBehavior(10)));
            ret.add(Pair.of(5, MaidMeleeAttack.create(20)));
            ret.add(Pair.of(5, new MaidUseShieldTask()));
        }
        return ret;
    }

    @Override
    public MenuProvider getTaskConfigGuiProvider(EntityMaid maid) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() { return Component.literal(""); }

            @Override
            public AbstractContainerMenu createMenu(int index, Inventory playerInventory, Player player) {
                return new MaidReviveConfigGui.Container(index, playerInventory, maid.getId());
            }
        };
    }

    @Override
    public void performRangedAttack(EntityMaid maid, LivingEntity target, float v) {
        ItemStack main = maid.getMainHandItem();
        if (main.getItem() instanceof BowItem) {
            ItemStack arrow = findAndConsumeArrow(maid, main);
            if (arrow.isEmpty()) return;
            AbstractArrow entityArrow = ProjectileUtil.getMobArrow(maid, arrow, v, main);
            entityArrow.setBaseDamage(entityArrow.getBaseDamage() * (0.8 + v) + 4.0);
            entityArrow.setNoGravity(true);
            float dist = maid.distanceTo(target);
            entityArrow.shoot(target.getX() - maid.getX(), target.getEyeY() - maid.getEyeY(), target.getZ() - maid.getZ(),
                    Mth.clamp(dist / 10f, 1.6f, 3.2f), 1 - Mth.clamp(dist / 100f, 0, 0.9f));
            main.hurtAndBreak(1, maid, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            maid.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (maid.getRandom().nextFloat() * 0.4F + 0.8F));
            maid.level().addFreshEntity(entityArrow);
        } else if (main.getItem() instanceof CrossbowItem) {
            ItemStack arrow = findAndConsumeArrow(maid, main);
            if (arrow.isEmpty()) return;
            AbstractArrow entityArrow = ProjectileUtil.getMobArrow(maid, arrow, v, main);
            float dist = maid.distanceTo(target);
            entityArrow.shoot(target.getX() - maid.getX(), target.getEyeY() - maid.getEyeY(), target.getZ() - maid.getZ(),
                    Mth.clamp(dist / 8f, 2.6f, 3.6f), 1 - Mth.clamp(dist / 120f, 0, 0.9f));
            main.hurtAndBreak(1, maid, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            maid.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (maid.getRandom().nextFloat() * 0.4F + 0.8F));
            maid.level().addFreshEntity(entityArrow);
        } else if (ItemHakureiGohei.isGohei(main)) {
            maid.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent(livingEntities -> {
                long count = livingEntities.stream().filter(t -> enemyEntityTest(maid, target, t)).count();
                AttributeInstance ad = maid.getAttribute(Attributes.ATTACK_DAMAGE);
                float atk = ad != null ? (float) ad.getBaseValue() : 2.0f;
                int imp = EnchantUtil.getLevel(maid.level(), EnchantmentKeys.IMPEDING, main);
                int spd = EnchantUtil.getLevel(maid.level(), EnchantmentKeys.SPEEDY, main);
                int multi = EnchantUtil.getLevel(maid.level(), Enchantments.MULTISHOT, main);
                int ender = EnchantUtil.getLevel(maid.level(), EnchantmentKeys.ENDERS_ENDER, main);
                float dist = maid.distanceTo(target);
                float speed = (0.3f * (v + 1)) * (spd + 1) + Mth.clamp(dist / 40f - 0.4f, 0, 2.4f);
                float inaccuracy = 1 - Mth.clamp(dist / 100f, 0, 0.8f);
                var level = maid.level();
                if (count <= 1) {
                    if (multi > 0) {
                        DanmakuShoot.create().setWorld(level).setThrower(maid).setTarget(target).setRandomColor().setRandomType()
                                .setDamage(atk * (v + 1.2f)).setGravity(0).setVelocity(speed).setHurtEnderman(ender > 0)
                                .setInaccuracy(inaccuracy).setFanNum(3).setYawTotal(Math.PI / 12).setImpedingLevel(imp).fanShapedShot();
                    } else {
                        DanmakuShoot.create().setWorld(level).setThrower(maid).setTarget(target).setRandomColor().setRandomType()
                                .setDamage(atk * (v + 1)).setGravity(0).setVelocity(speed).setHurtEnderman(ender > 0)
                                .setInaccuracy(inaccuracy / 5).setImpedingLevel(imp).aimedShot();
                    }
                } else if (count <= 5) {
                    DanmakuShoot.create().setWorld(level).setThrower(maid).setTarget(target).setRandomColor().setRandomType()
                            .setDamage(atk * (v + 1.2f)).setGravity(0).setVelocity(speed).setHurtEnderman(ender > 0)
                            .setInaccuracy(inaccuracy / 5).setFanNum(8).setYawTotal(Math.PI / 3).setImpedingLevel(imp).fanShapedShot();
                } else {
                    DanmakuShoot.create().setWorld(level).setThrower(maid).setTarget(target).setRandomColor().setRandomType()
                            .setDamage(atk * (v + 1.5f)).setGravity(0).setVelocity(speed).setHurtEnderman(ender > 0)
                            .setInaccuracy(inaccuracy / 5).setFanNum(32).setYawTotal(2 * Math.PI / 3).setImpedingLevel(imp).fanShapedShot();
                }
                main.hurtAndBreak(1, maid, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            });
        }
    }

    private boolean hasAnyWeapon(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (isAssaultWeapon(main) || (isRangedWeapon(main) && hasAmmoFor(maid, main))) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (isAssaultWeapon(s) || (isRangedWeapon(s) && hasAmmoFor(maid, s))) return true;
        }
        return false;
    }

    private boolean isAssaultWeapon(ItemStack s) {
        Item i = s.getItem();
        return i instanceof SwordItem || i instanceof AxeItem || i instanceof TridentItem;
    }

    private boolean enemyEntityTest(EntityMaid shooter, LivingEntity target, LivingEntity test) {
        return shooter.canAttack(test) && target.getType().equals(test.getType()) && shooter.canSee(test);
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        return IRangedAttackTask.targetConditionsTest(maid, target, getRangedConfigRange(maid));
    }

    @Override
    public float searchRadius(EntityMaid maid) { return getSearchRange(maid); }

    @Override
    public AABB searchDimension(EntityMaid maid) {
        float range = getSearchRange(maid);
        return maid.hasRestriction() ? new AABB(maid.getRestrictCenter()).inflate(range) : maid.getBoundingBox().inflate(range);
    }

    private boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > getSearchRange(maid);
    }

    private float getSearchRange(EntityMaid maid) {
        return hasRangedWeapon(maid) ? getRangedConfigRange(maid).get() : maid.getRestrictRadius();
    }

    private ModConfigSpec.IntValue getRangedConfigRange(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (main.getItem() instanceof CrossbowItem) return MaidConfig.CROSS_BOW_RANGE;
        if (main.getItem() instanceof BowItem) return MaidConfig.BOW_RANGE;
        if (ItemHakureiGohei.isGohei(main)) return MaidConfig.DANMAKU_RANGE;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (s.getItem() instanceof CrossbowItem) return MaidConfig.CROSS_BOW_RANGE;
            if (s.getItem() instanceof BowItem) return MaidConfig.BOW_RANGE;
            if (ItemHakureiGohei.isGohei(s)) return MaidConfig.DANMAKU_RANGE;
        }
        return MaidConfig.BOW_RANGE;
    }

    private boolean hasRangedWeapon(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (isRangedWeapon(main) && hasAmmoFor(maid, main)) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (isRangedWeapon(inv.getStackInSlot(i)) && hasAmmoFor(maid, inv.getStackInSlot(i))) return true;
        }
        return false;
    }

    private boolean hasAmmoFor(EntityMaid maid, ItemStack weapon) {
        if (ItemHakureiGohei.isGohei(weapon)) return true;
        if (EnchantUtil.getLevel(maid.level(), net.minecraft.world.item.enchantment.Enchantments.INFINITY, weapon) > 0) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() instanceof ArrowItem) return true;
        }
        return false;
    }

    private boolean isRangedWeapon(ItemStack s) {
        return s.getItem() instanceof BowItem || s.getItem() instanceof CrossbowItem || ItemHakureiGohei.isGohei(s);
    }

    private boolean isRangedEquipped(EntityMaid maid) {
        ItemStack m = maid.getMainHandItem();
        return m.getItem() instanceof BowItem || m.getItem() instanceof CrossbowItem || ItemHakureiGohei.isGohei(m);
    }

    private boolean hasArrows(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (EnchantUtil.getLevel(maid.level(), net.minecraft.world.item.enchantment.Enchantments.INFINITY, main) > 0) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() instanceof ArrowItem) return true;
        }
        return false;
    }

    private ItemStack findAndConsumeArrow(EntityMaid maid, ItemStack bow) {
        boolean infinity = EnchantUtil.getLevel(maid.level(), net.minecraft.world.item.enchantment.Enchantments.INFINITY, bow) > 0;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof ArrowItem) {
                ItemStack used = stack.copy();
                used.setCount(1);
                if (!infinity) stack.shrink(1);
                return used;
            }
        }
        return ItemStack.EMPTY;
    }
}
