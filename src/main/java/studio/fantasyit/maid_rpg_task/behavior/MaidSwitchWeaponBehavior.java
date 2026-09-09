package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.item.ItemHakureiGohei;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_rpg_task.util.EnchantUtil;

import java.util.Optional;
import java.util.function.Predicate;

public class MaidSwitchWeaponBehavior extends Behavior<EntityMaid> {

    public MaidSwitchWeaponBehavior() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Optional<LivingEntity> targetOpt = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (targetOpt.isEmpty()) return false;

        LivingEntity target = targetOpt.get();
        boolean inMeleeRange = maid.isWithinMeleeAttackRange(target);
        ItemStack mainHand = maid.getMainHandItem();
        ItemStack offHand = maid.getOffhandItem();

        if (isRangedWeapon(offHand) && hasAmmoForWeapon(maid, offHand)) {
            return true;
        }

        if (!inMeleeRange) {
            if (isRangedWeapon(mainHand) && hasAmmoForWeapon(maid, mainHand)) return false;
            return findWeaponSlot(maid, s -> isRangedWeapon(s) && hasAmmoForWeapon(maid, s)) != -1;
        } else {
            if (isAssaultWeapon(mainHand)) return false;
            if (findWeaponSlot(maid, this::isAssaultWeapon) != -1) return true;
            return isAssaultWeapon(offHand);
        }
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Optional<LivingEntity> targetOpt = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (targetOpt.isEmpty()) return;

        LivingEntity target = targetOpt.get();
        boolean inMeleeRange = maid.isWithinMeleeAttackRange(target);

        handleOffhandRanged(maid);

        if (!inMeleeRange) switchToRangedWeapon(maid);
        else switchToMeleeWeapon(maid);
    }

    private int findWeaponSlot(EntityMaid maid, Predicate<ItemStack> predicate) {
        CombinedInvWrapper handler = maid.getAvailableInv(true);
        for (int i = 0; i < handler.getSlots(); i++) {
            if (predicate.test(handler.getStackInSlot(i))) return i;
        }
        return -1;
    }

    private int findWeaponSlotExcluding(EntityMaid maid, Predicate<ItemStack> predicate, ItemStack exclude) {
        CombinedInvWrapper handler = maid.getAvailableInv(true);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!ItemStack.isSameItemSameComponents(stack, exclude) && predicate.test(stack)) return i;
        }
        return -1;
    }

    private void switchToRangedWeapon(EntityMaid maid) {
        handleOffhandRanged(maid);
        CombinedInvWrapper handler = maid.getAvailableInv(true);
        int bestSlot = findWeaponSlotExcluding(maid,
                s -> isRangedWeapon(s) && hasAmmoForWeapon(maid, s), maid.getOffhandItem());
        if (bestSlot != -1) {
            ItemStack toEquip = handler.getStackInSlot(bestSlot);
            ItemStack current = maid.getMainHandItem();
            handler.setStackInSlot(bestSlot, current.copy());
            maid.setItemInHand(InteractionHand.MAIN_HAND, toEquip.copy());
            return;
        }
        ItemStack off = maid.getOffhandItem();
        if (isRangedWeapon(off) && hasAmmoForWeapon(maid, off)) {
            ItemStack current = maid.getMainHandItem();
            maid.setItemInHand(InteractionHand.MAIN_HAND, off.copy());
            maid.setItemInHand(InteractionHand.OFF_HAND, current.copy());
        }
    }

    private void switchToMeleeWeapon(EntityMaid maid) {
        CombinedInvWrapper handler = maid.getAvailableInv(true);
        int bestSlot = findWeaponSlotExcluding(maid, this::isAssaultWeapon, maid.getOffhandItem());
        if (bestSlot != -1) {
            ItemStack toEquip = handler.getStackInSlot(bestSlot);
            ItemStack current = maid.getMainHandItem();
            handler.setStackInSlot(bestSlot, current.copy());
            maid.setItemInHand(InteractionHand.MAIN_HAND, toEquip.copy());
            return;
        }
        ItemStack off = maid.getOffhandItem();
        if (isAssaultWeapon(off)) {
            ItemStack current = maid.getMainHandItem();
            maid.setItemInHand(InteractionHand.MAIN_HAND, off.copy());
            maid.setItemInHand(InteractionHand.OFF_HAND, current.copy());
        }
    }

    private void handleOffhandRanged(EntityMaid maid) {
        ItemStack off = maid.getOffhandItem();
        if (off.isEmpty() || !isRangedWeapon(off)) return;
        CombinedInvWrapper handler = maid.getAvailableInv(true);
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                handler.setStackInSlot(i, off.copy());
                maid.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                return;
            }
        }
        ItemStack current = maid.getMainHandItem();
        maid.setItemInHand(InteractionHand.MAIN_HAND, off.copy());
        maid.setItemInHand(InteractionHand.OFF_HAND, current.copy());
    }

    private boolean hasAmmoForWeapon(EntityMaid maid, ItemStack weapon) {
        if (ItemHakureiGohei.isGohei(weapon)) return true;
        if (!(weapon.getItem() instanceof BowItem) && !(weapon.getItem() instanceof CrossbowItem)) return true;
        if (EnchantUtil.getLevel(maid.level(), net.minecraft.world.item.enchantment.Enchantments.INFINITY, weapon) > 0) return true;
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

    private boolean isAssaultWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem;
    }
}
