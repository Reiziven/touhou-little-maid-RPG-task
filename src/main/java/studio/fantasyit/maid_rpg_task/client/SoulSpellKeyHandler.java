package studio.fantasyit.maid_rpg_task.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;
import studio.fantasyit.maid_rpg_task.network.Network;
import studio.fantasyit.maid_rpg_task.network.SoulSpellRotatePacket;

/**
 * Client-side: intercepts UP/DOWN key presses while holding the soul spell item.
 * Rotates the maid list locally for instant feedback and sends packet to sync server.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SoulSpellKeyHandler {

    private static final String MULTI_MAIDS_KEY = "mss_multi_maids";

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Only on PRESS, not repeat/release
        if (event.getAction() != GLFW.GLFW_PRESS && event.getAction() != GLFW.GLFW_REPEAT) return;

        int key = event.getKey();
        int dir;
        if (key == GLFW.GLFW_KEY_UP) {
            dir = -1; // move to prev (bring last to front)
        } else if (key == GLFW.GLFW_KEY_DOWN) {
            dir = 1;  // move to next (send front to back)
        } else {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return; // ignore when GUI is open

        // Find the soul spell item in main or off hand
        ItemStack mainHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack stack = MasterSoulSpellEvent.hasMultiMaidData(mainHand) ? mainHand
                : MasterSoulSpellEvent.hasMultiMaidData(mc.player.getItemInHand(InteractionHand.OFF_HAND))
                ? mc.player.getItemInHand(InteractionHand.OFF_HAND) : ItemStack.EMPTY;

        if (stack.isEmpty()) return;

        // Rotate the client-side NBT immediately for instant tooltip feedback
        rotateClientSide(stack, dir);

        // Show HUD
        SoulSpellHud.showMaidName(SoulSpellHud.readFirstMaidName(stack));

        // Sync to server
        Network.INSTANCE.sendToServer(new SoulSpellRotatePacket(dir));
    }

    private static void rotateClientSide(ItemStack stack, int dir) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(MULTI_MAIDS_KEY, Tag.TAG_COMPOUND);
        if (list.size() <= 1) return;

        ListTag rotated = new ListTag();
        if (dir > 0) {
            for (int i = 1; i < list.size(); i++) rotated.add(list.get(i));
            rotated.add(list.get(0));
        } else {
            rotated.add(list.get(list.size() - 1));
            for (int i = 0; i < list.size() - 1; i++) rotated.add(list.get(i));
        }
        tag.put(MULTI_MAIDS_KEY, rotated);
    }
}
