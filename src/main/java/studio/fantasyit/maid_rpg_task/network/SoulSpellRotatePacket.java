package studio.fantasyit.maid_rpg_task.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;

import java.util.function.Supplier;

/**
 * Sent client → server when the player presses UP or DOWN while holding the soul spell.
 * direction = +1 (next) or -1 (prev)
 */
public class SoulSpellRotatePacket {

    private static final String MULTI_MAIDS_KEY = "mss_multi_maids";
    private final int direction;

    public SoulSpellRotatePacket(int direction) {
        this.direction = direction;
    }

    public SoulSpellRotatePacket(FriendlyByteBuf buf) {
        this.direction = buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(direction);
    }

    public static void handle(SoulSpellRotatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Check main hand then off hand
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!MasterSoulSpellEvent.hasMultiMaidData(stack)) {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
                if (!MasterSoulSpellEvent.hasMultiMaidData(stack)) return;
            }

            CompoundTag tag = stack.getOrCreateTag();
            ListTag list = tag.getList(MULTI_MAIDS_KEY, Tag.TAG_COMPOUND);
            if (list.size() <= 1) return;

            // Rotate: +1 moves first to last (next), -1 moves last to first (prev)
            ListTag rotated = new ListTag();
            if (msg.direction > 0) {
                // next: [0,1,2,3] → [1,2,3,0]
                for (int i = 1; i < list.size(); i++) rotated.add(list.get(i));
                rotated.add(list.get(0));
            } else {
                // prev: [0,1,2,3] → [3,0,1,2]
                rotated.add(list.get(list.size() - 1));
                for (int i = 0; i < list.size() - 1; i++) rotated.add(list.get(i));
            }
            tag.put(MULTI_MAIDS_KEY, rotated);
        });
        ctx.get().setPacketHandled(true);
    }
}
