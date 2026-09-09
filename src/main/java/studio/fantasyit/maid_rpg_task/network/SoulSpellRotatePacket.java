package studio.fantasyit.maid_rpg_task.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;

import java.util.ArrayList;
import java.util.List;

public record SoulSpellRotatePacket(int direction) implements CustomPacketPayload {

    public static final Type<SoulSpellRotatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "soul_spell_rotate"));

    public static final StreamCodec<FriendlyByteBuf, SoulSpellRotatePacket> CODEC =
            StreamCodec.of(
                    (buf, msg) -> buf.writeByte(msg.direction()),
                    buf -> new SoulSpellRotatePacket(buf.readByte())
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SoulSpellRotatePacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!MasterSoulSpellEvent.hasMultiMaidData(stack)) {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
                if (!MasterSoulSpellEvent.hasMultiMaidData(stack)) return;
            }

            List<CompoundTag> list = MasterSoulSpellEvent.getMultiMaidList(stack);
            if (list.size() <= 1) return;

            List<CompoundTag> rotated = new ArrayList<>(list.size());
            if (msg.direction() > 0) {
                for (int i = 1; i < list.size(); i++) rotated.add(list.get(i));
                rotated.add(list.get(0));
            } else {
                rotated.add(list.get(list.size() - 1));
                for (int i = 0; i < list.size() - 1; i++) rotated.add(list.get(i));
            }
            MasterSoulSpellEvent.setMultiMaidList(stack, rotated);
        });
    }
}
