package studio.fantasyit.maid_rpg_task.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

@EventBusSubscriber(modid = MaidRpgTask.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Network {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                MaidConfigurePacket.TYPE,
                MaidConfigurePacket.CODEC,
                MaidConfigurePacket::handle
        );
        registrar.playToServer(
                SoulSpellRotatePacket.TYPE,
                SoulSpellRotatePacket.CODEC,
                SoulSpellRotatePacket::handle
        );
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @OnlyIn(Dist.CLIENT)
    public static Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }
}
