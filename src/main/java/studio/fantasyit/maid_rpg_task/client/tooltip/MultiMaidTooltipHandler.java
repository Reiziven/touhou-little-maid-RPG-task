package studio.fantasyit.maid_rpg_task.client.tooltip;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;
import studio.fantasyit.maid_rpg_task.tooltip.MultiMaidTooltip;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MultiMaidTooltipHandler {

    /** Register our ClientTooltipComponent factory (MOD bus). */
    @SubscribeEvent
    public static void onRegisterTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MultiMaidTooltip.class, ClientMultiMaidTooltip::new);
    }
}
