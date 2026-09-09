package studio.fantasyit.maid_rpg_task.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;
import studio.fantasyit.maid_rpg_task.network.Network;
import studio.fantasyit.maid_rpg_task.network.SoulSpellRotatePacket;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class SoulSpellKeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS && event.getAction() != GLFW.GLFW_REPEAT) return;

        int key = event.getKey();
        int dir;
        if (key == GLFW.GLFW_KEY_UP) dir = -1;
        else if (key == GLFW.GLFW_KEY_DOWN) dir = 1;
        else return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        ItemStack mainHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack stack = MasterSoulSpellEvent.hasMultiMaidData(mainHand) ? mainHand
                : MasterSoulSpellEvent.hasMultiMaidData(mc.player.getItemInHand(InteractionHand.OFF_HAND))
                ? mc.player.getItemInHand(InteractionHand.OFF_HAND) : ItemStack.EMPTY;

        if (stack.isEmpty()) return;

        rotateClientSide(stack, dir);
        SoulSpellHud.showMaidName(SoulSpellHud.readFirstMaidName(stack));
        Network.sendToServer(new SoulSpellRotatePacket(dir));
    }

    private static void rotateClientSide(ItemStack stack, int dir) {
        List<CompoundTag> list = MasterSoulSpellEvent.getMultiMaidList(stack);
        if (list.size() <= 1) return;
        List<CompoundTag> rotated = new ArrayList<>(list.size());
        if (dir > 0) {
            for (int i = 1; i < list.size(); i++) rotated.add(list.get(i));
            rotated.add(list.get(0));
        } else {
            rotated.add(list.get(list.size() - 1));
            for (int i = 0; i < list.size() - 1; i++) rotated.add(list.get(i));
        }
        MasterSoulSpellEvent.setMultiMaidList(stack, rotated);
    }
}
