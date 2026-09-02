package studio.fantasyit.maid_rpg_task.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.*;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuShoot;
import com.github.tartaricacid.touhoulittlemaid.init.InitEnchantments;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.behavior.GoheiAttackBehavior;
import studio.fantasyit.maid_rpg_task.behavior.MaidSwitchWeaponBehavior;
import studio.fantasyit.maid_rpg_task.behavior.PlayerReviveBehavior;
import studio.fantasyit.maid_rpg_task.behavior.SupportEffectBehavior;
import studio.fantasyit.maid_rpg_task.compat.PlayerRevive;
import studio.fantasyit.maid_rpg_task.menu.MaidReviveConfigGui;

import java.util.ArrayList;
import java.util.List;

public class MaidSupportTask implements IRangedAttackTask {
    public static final ResourceLocation UID = new ResourceLocation(MaidRpgTask.MODID, "support");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.POTION.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid entityMaid) {
        return null;
    }

    @Override
    public boolean isEnable(EntityMaid maid) {
        return Config.enableReviveTask;
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }

    @Override
    public boolean enablePanic(EntityMaid maid) {
        return false;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        ArrayList<Pair<Integer, BehaviorControl<? super EntityMaid>>> ret = new ArrayList<>();
        if (PlayerRevive.isEnable()) {
            ret.add(Pair.of(1, new PlayerReviveBehavior()));
        }
        ret.add(Pair.of(1, new SupportEffectBehavior()));
        if (Config.supportCanAttack) {
            ret.add(Pair.of(2, new MaidSwitchWeaponBehavior()));
            ret.add(Pair.of(5, StartAttacking.create(
                    e -> hasAnyWeapon(e) && (!isRangedEquipped(e) || hasArrows(e)),
                    IRangedAttackTask::findFirstValidAttackTarget)));
            ret.add(Pair.of(5, StopAttackingIfTargetInvalid.create(
                    target -> farAway(target, entityMaid)
                            || !hasAnyWeapon(entityMaid)
                            || (isRangedEquipped(entityMaid) && !hasArrows(entityMaid)))));
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
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int index, Inventory playerInventory, Player player) {
                return new MaidReviveConfigGui.Container(index, playerInventory, maid.getId());
            }
        };
    }

    // -----------------------------------------------------------------------
    // IRangedAttackTask — mirrors DPS task logic exactly
    // -----------------------------------------------------------------------

    @Override
    public void performRangedAttack(EntityMaid entityMaid, LivingEntity livingEntity, float v) {
        ItemStack mainHandItem = entityMaid.getMainHandItem();
        if (mainHandItem.getItem() instanceof BowItem) {
            ItemStack arrow = findAndConsumeArrow(entityMaid, mainHandItem);
            if (arrow.isEmpty()) return;
            AbstractArrow entityArrow = ProjectileUtil.getMobArrow(entityMaid, arrow, v);
            entityArrow = ((BowItem) mainHandItem.getItem()).customArrow(entityArrow);
            entityArrow.setBaseDamage(entityArrow.getBaseDamage() * (0.8 + v));
            double x = livingEntity.getX() - entityMaid.getX();
            double y = livingEntity.getEyeY() - entityMaid.getEyeY();
            double z = livingEntity.getZ() - entityMaid.getZ();
            float distance = entityMaid.distanceTo(livingEntity);
            float velocity = Mth.clamp(distance / 10f, 1.6f, 3.2f);
            float inaccuracy = 1 - Mth.clamp(distance / 100f, 0, 0.9f);
            entityArrow.setBaseDamage(entityArrow.getBaseDamage() + 4.0D);
            entityArrow.setNoGravity(true);
            entityArrow.shoot(x, y, z, velocity, inaccuracy);
            mainHandItem.hurtAndBreak(1, entityMaid, (maid) -> maid.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            entityMaid.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (entityMaid.getRandom().nextFloat() * 0.4F + 0.8F));
            entityMaid.level().addFreshEntity(entityArrow);
        } else if (mainHandItem.getItem() instanceof CrossbowItem) {
            ItemStack arrow = findAndConsumeArrow(entityMaid, mainHandItem);
            if (arrow.isEmpty()) return;
            AbstractArrow entityArrow = ProjectileUtil.getMobArrow(entityMaid, arrow, v);
            double x = livingEntity.getX() - entityMaid.getX();
            double y = livingEntity.getEyeY() - entityMaid.getEyeY();
            double z = livingEntity.getZ() - entityMaid.getZ();
            float distance = entityMaid.distanceTo(livingEntity);
            float velocity = Mth.clamp(distance / 8f, 2.6f, 3.6f);
            float inaccuracy = 1 - Mth.clamp(distance / 120f, 0, 0.9f);
            entityArrow.shoot(x, y, z, velocity, inaccuracy);
            mainHandItem.hurtAndBreak(1, entityMaid, (maid) -> maid.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            entityMaid.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (entityMaid.getRandom().nextFloat() * 0.4F + 0.8F));
            entityMaid.level().addFreshEntity(entityArrow);
        } else if (ItemHakureiGohei.isGohei(mainHandItem)) {
            entityMaid.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent(livingEntities -> {
                long entityCount = livingEntities.stream().filter(test -> enemyEntityTest(entityMaid, livingEntity, test)).count();
                var level = entityMaid.level();
                AttributeInstance attackDamage = entityMaid.getAttribute(Attributes.ATTACK_DAMAGE);
                float attackValue = 2.0f;
                if (attackDamage != null) attackValue = (float) attackDamage.getBaseValue();
                int impedingLevel = EnchantmentHelper.getTagEnchantmentLevel(InitEnchantments.IMPEDING.get(), mainHandItem);
                int speedyLevel = EnchantmentHelper.getTagEnchantmentLevel(InitEnchantments.SPEEDY.get(), mainHandItem);
                int multiShotLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MULTISHOT, mainHandItem);
                int endersEnderLevel = EnchantmentHelper.getTagEnchantmentLevel(InitEnchantments.ENDERS_ENDER.get(), mainHandItem);
                float speed = (0.3f * (v + 1)) * (speedyLevel + 1);
                boolean hurtEnderman = endersEnderLevel > 0;
                float distance = entityMaid.distanceTo(livingEntity);
                speed = speed + Mth.clamp(distance / 40f - 0.4f, 0, 2.4f);
                float inaccuracy = 1 - Mth.clamp(distance / 100f, 0, 0.8f);
                if (entityCount <= 1) {
                    if (multiShotLevel > 0) {
                        DanmakuShoot.create().setWorld(level).setThrower(entityMaid)
                                .setTarget(livingEntity).setRandomColor().setRandomType()
                                .setDamage(attackValue * (v + 1.2f)).setGravity(0)
                                .setVelocity(speed).setHurtEnderman(hurtEnderman)
                                .setInaccuracy(inaccuracy).setFanNum(3).setYawTotal(Math.PI / 12)
                                .setImpedingLevel(impedingLevel).fanShapedShot();
                    } else {
                        DanmakuShoot.create().setWorld(level).setThrower(entityMaid)
                                .setTarget(livingEntity).setRandomColor().setRandomType()
                                .setDamage(attackValue * (v + 1)).setGravity(0)
                                .setVelocity(speed).setHurtEnderman(hurtEnderman)
                                .setInaccuracy(inaccuracy / 5).setImpedingLevel(impedingLevel)
                                .aimedShot();
                    }
                } else if (entityCount <= 5) {
                    DanmakuShoot.create().setWorld(level).setThrower(entityMaid)
                            .setTarget(livingEntity).setRandomColor().setRandomType()
                            .setDamage(attackValue * (v + 1.2f)).setGravity(0)
                            .setVelocity(speed).setHurtEnderman(hurtEnderman)
                            .setInaccuracy(inaccuracy / 5).setFanNum(8).setYawTotal(Math.PI / 3)
                            .setImpedingLevel(impedingLevel).fanShapedShot();
                } else {
                    DanmakuShoot.create().setWorld(level).setThrower(entityMaid)
                            .setTarget(livingEntity).setRandomColor().setRandomType()
                            .setDamage(attackValue * (v + 1.5f)).setGravity(0)
                            .setVelocity(speed).setHurtEnderman(hurtEnderman)
                            .setInaccuracy(inaccuracy / 5).setFanNum(32).setYawTotal(2 * Math.PI / 3)
                            .setImpedingLevel(impedingLevel).fanShapedShot();
                }
                mainHandItem.hurtAndBreak(1, entityMaid, (maid) -> maid.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            });
        }
    }

    /** True if the maid has any usable weapon — melee OR ranged with ammo. */
    private boolean hasAnyWeapon(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (isAssaultWeapon(main)) return true;
        if (isRangedWeapon(main) && hasAmmoFor(maid, main)) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (isAssaultWeapon(s)) return true;
            if (isRangedWeapon(s) && hasAmmoFor(maid, s)) return true;
        }
        return false;
    }

    private boolean isAssaultWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem;
    }

    private boolean enemyEntityTest(EntityMaid shooter, LivingEntity target, LivingEntity test) {
        return shooter.canAttack(test) && target.getType().equals(test.getType()) && shooter.canSee(test);
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        return IRangedAttackTask.targetConditionsTest(maid, target, getRangedConfigRange(maid));
    }

    @Override
    public float searchRadius(EntityMaid maid) {
        return getSearchRange(maid);
    }

    @Override
    public AABB searchDimension(EntityMaid maid) {
        float range = getSearchRange(maid);
        if (maid.hasRestriction()) {
            return new AABB(maid.getRestrictCenter()).inflate(range);
        }
        return maid.getBoundingBox().inflate(range);
    }

    private boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > getSearchRange(maid);
    }

    private float getSearchRange(EntityMaid maid) {
        return hasRangedWeapon(maid) ? getRangedConfigRange(maid).get() : maid.getRestrictRadius();
    }

    private ForgeConfigSpec.IntValue getRangedConfigRange(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (main.getItem() instanceof CrossbowItem) return MaidConfig.CROSS_BOW_RANGE;
        if (main.getItem() instanceof BowItem)      return MaidConfig.BOW_RANGE;
        if (ItemHakureiGohei.isGohei(main))         return MaidConfig.DANMAKU_RANGE;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (s.getItem() instanceof CrossbowItem) return MaidConfig.CROSS_BOW_RANGE;
            if (s.getItem() instanceof BowItem)      return MaidConfig.BOW_RANGE;
            if (ItemHakureiGohei.isGohei(s))         return MaidConfig.DANMAKU_RANGE;
        }
        return MaidConfig.BOW_RANGE;
    }

    private boolean hasRangedWeapon(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (isRangedWeapon(main) && hasAmmoFor(maid, main)) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (isRangedWeapon(s) && hasAmmoFor(maid, s)) return true;
        }
        return false;
    }

    private boolean hasAmmoFor(EntityMaid maid, ItemStack weapon) {
        if (ItemHakureiGohei.isGohei(weapon)) return true;
        if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, weapon) > 0) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() instanceof ArrowItem) return true;
        }
        return false;
    }

    private boolean isRangedWeapon(ItemStack stack) {
        return stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || ItemHakureiGohei.isGohei(stack);
    }

    private boolean isRangedEquipped(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        return main.getItem() instanceof BowItem
                || main.getItem() instanceof CrossbowItem
                || ItemHakureiGohei.isGohei(main);
    }

    private boolean hasArrows(EntityMaid maid) {
        ItemStack main = maid.getMainHandItem();
        if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, main) > 0) return true;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() instanceof ArrowItem) return true;
        }
        return false;
    }

    private ItemStack findAndConsumeArrow(EntityMaid maid, ItemStack bow) {
        boolean infinity = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
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
