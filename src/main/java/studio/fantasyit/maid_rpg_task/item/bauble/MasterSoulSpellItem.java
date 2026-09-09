package studio.fantasyit.maid_rpg_task.item.bauble;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class MasterSoulSpellItem extends Item {

    public MasterSoulSpellItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.equip")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.store_header")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.store_multi")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.store_single")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.summon_header")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.summon_all")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.summon_one")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.order_header")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.order_keys")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.maid_rpg_task.master_soul_spell.tooltip.hint")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
