package studio.fantasyit.maid_rpg_task.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Utility to look up enchantment levels by ResourceKey in 1.21.1.
 * Vanilla and TLM enchantments are both ResourceKey<Enchantment> now.
 */
public class EnchantUtil {

    public static int getLevel(Level level, ResourceKey<Enchantment> key, ItemStack stack) {
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        if (!registry.containsKey(key)) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(registry.getHolderOrThrow(key), stack);
    }
}
