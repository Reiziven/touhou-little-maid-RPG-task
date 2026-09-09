package studio.fantasyit.maid_rpg_task.client;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.apache.commons.lang3.StringUtils;
import studio.fantasyit.maid_rpg_task.client.tooltip.ClientMultiMaidTooltip;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;

import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class SoulSpellHud {

    private static final int DISPLAY_TICKS = 60;
    private static String pendingName = null;
    private static int ticksLeft = 0;

    public static void showMaidName(String name) {
        pendingName = name;
        ticksLeft = DISPLAY_TICKS;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if (pendingName == null || ticksLeft <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float alpha = ticksLeft <= 20 ? ticksLeft / 20f : 1.0f;
        int a = (int)(alpha * 255) << 24;
        int color = a | 0xFFFFAA;

        Component msg = Component.literal("❧ ")
                .append(Component.literal(pendingName).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" ❧"));

        int textWidth = mc.font.width(msg);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 + 30;

        graphics.drawString(mc.font, msg, x, y, color, true);

        ticksLeft--;
        if (ticksLeft <= 0) pendingName = null;
    }

    public static String readFirstMaidName(ItemStack stack) {
        List<CompoundTag> list = MasterSoulSpellEvent.getMultiMaidList(stack);
        if (list.isEmpty()) return "?";
        CompoundTag first = list.get(0);

        if (first.contains("CustomName", Tag.TAG_STRING)) {
            String json = first.getString("CustomName");
            if (StringUtils.isNotBlank(json)) {
                try {
                    Component c = net.minecraft.client.Minecraft.getInstance().level != null
                            ? Component.Serializer.fromJson(json, net.minecraft.client.Minecraft.getInstance().level.registryAccess())
                            : null;
                    if (c != null) return c.getString();
                } catch (Exception ignored) {}
            }
        }
        String modelId = first.getString(EntityMaid.MODEL_ID_TAG);
        if (StringUtils.isNotBlank(modelId)) return ClientMultiMaidTooltip.stripModelId(modelId);
        return "?";
    }
}
