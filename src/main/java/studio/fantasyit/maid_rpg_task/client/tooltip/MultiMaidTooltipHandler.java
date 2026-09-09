package studio.fantasyit.maid_rpg_task.client.tooltip;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.tooltip.MultiMaidTooltip;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = MaidRpgTask.MODID)
public class MultiMaidTooltipHandler {

    @SubscribeEvent
    public static void onRegisterTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MultiMaidTooltip.class, ClientMultiMaidTooltip::new);
    }
}
