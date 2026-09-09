package studio.fantasyit.maid_rpg_task.registry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.client.renderer.*;
import studio.fantasyit.maid_rpg_task.menu.MaidReviveConfigGui;

@EventBusSubscriber(modid = MaidRpgTask.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientGuiRegistry {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GuiRegistry.MAID_REVIVE_CONFIG_GUI.get(), MaidReviveConfigGui::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.EARTH_SPIKE.get(), EarthSpikeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FRONT_SPIKE.get(), FrontSpikeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TORNADO.get(), TornadoRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_MAGIC_CIRCLE.get(), FireMagicCircleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICE_MAGIC_CIRCLE.get(), IceMagicCircleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICE_SPIKE.get(), IceSpikeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HEAL_MAGIC_CIRCLE.get(), HealMagicCircleRenderer::new);
    }
}
