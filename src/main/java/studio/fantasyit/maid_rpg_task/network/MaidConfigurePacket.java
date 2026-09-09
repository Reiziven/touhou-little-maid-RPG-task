package studio.fantasyit.maid_rpg_task.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.data.IConfigSetter;
import studio.fantasyit.maid_rpg_task.data.MaidConfigKeys;

public record MaidConfigurePacket(int maidId, ResourceLocation key, String name, String value)
        implements CustomPacketPayload {

    public static final Type<MaidConfigurePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "maid_configure"));

    public static final StreamCodec<FriendlyByteBuf, MaidConfigurePacket> CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeInt(msg.maidId());
                        buf.writeUtf(msg.key().toString());
                        buf.writeUtf(msg.name());
                        buf.writeUtf(msg.value());
                    },
                    buf -> new MaidConfigurePacket(
                            buf.readInt(),
                            ResourceLocation.tryParse(buf.readUtf()),
                            buf.readUtf(),
                            buf.readUtf()
                    )
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MaidConfigurePacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender.level().getEntity(msg.maidId()) instanceof EntityMaid entityMaid) {
                if (MaidConfigKeys.getValue(entityMaid, msg.key()) instanceof IConfigSetter ics) {
                    ics.setConfigValue(msg.name(), msg.value());
                }
            }
        });
    }

    public static void send(EntityMaid maid, ResourceLocation key, String name, String value) {
        Network.sendToServer(new MaidConfigurePacket(maid.getId(), key, name, value));
    }
}
