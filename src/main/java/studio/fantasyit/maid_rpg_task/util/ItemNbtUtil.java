package studio.fantasyit.maid_rpg_task.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

/**
 * Helper to read/write CompoundTag data on ItemStacks using the 1.21.1
 * DataComponent system (CustomData) instead of the removed getOrCreateTag() API.
 */
public class ItemNbtUtil {

    /** Get the custom NBT tag for an ItemStack, or an empty CompoundTag if absent. */
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return new CompoundTag();
        return data.copyTag();
    }

    /** Returns true if the stack has custom NBT data containing the given key. */
    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.copyTag().isEmpty();
    }

    /** Write a CompoundTag back onto an ItemStack. */
    public static void setTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /** Get a ListTag from item NBT by key, or empty ListTag if absent. */
    public static ListTag getList(ItemStack stack, String key, int type) {
        CompoundTag tag = getTag(stack);
        if (tag.contains(key, type)) return tag.getList(key, type);
        return new ListTag();
    }

    /** Put a ListTag into item NBT by key. */
    public static void putList(ItemStack stack, String key, ListTag list) {
        CompoundTag tag = getTag(stack);
        tag.put(key, list);
        setTag(stack, tag);
    }
}

