package studio.fantasyit.maid_rpg_task.client;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import studio.fantasyit.maid_rpg_task.client.tooltip.ClientMultiMaidTooltip;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;

/**
 * Renders a temporary HUD message showing the selected maid name after rotating.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SoulSpellHud {

    private static final int DISPLAY_TICKS = 60; // 3 seconds
    private static String pendingName = null;
    private static int ticksLeft = 0;

    /** Call from client key handler to set the display name. */
    public static void showMaidName(String name) {
        pendingName = name;
        ticksLeft = DISPLAY_TICKS;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (pendingName == null || ticksLeft <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Fade out in last 20 ticks
        float alpha = ticksLeft <= 20 ? ticksLeft / 20f : 1.0f;
        int a = (int) (alpha * 255) << 24;
        int color = a | 0xFFFFAA; // warm yellow, no alpha in lower bits

        Component msg = Component.literal("❧ ")
                .append(Component.literal(pendingName).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" ❧"));

        int textWidth = mc.font.width(msg);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 + 30;

        graphics.drawString(mc.font, msg, x, y, color, true);

        ticksLeft--;
        if (ticksLeft <= 0) {
            pendingName = null;
        }
    }

    /**
     * Reads the name of the first maid in the multi-maid list from an item stack.
     */
    public static String readFirstMaidName(ItemStack stack) {
        if (!stack.hasTag()) return "?";
        CompoundTag tag = stack.getTag();
        if (tag == null) return "?";
        ListTag list = tag.getList("mss_multi_maids", Tag.TAG_COMPOUND);
        if (list.isEmpty()) return "?";
        CompoundTag first = list.getCompound(0);

        // Try custom name first
        if (first.contains("CustomName", Tag.TAG_STRING)) {
            String json = first.getString("CustomName");
            if (StringUtils.isNotBlank(json)) {
                try {
                    Component c = Component.Serializer.fromJson(json);
                    if (c != null) return c.getString();
                } catch (Exception ignored) {}
            }
        }
        // Fall back to model ID — strip namespace and path for a clean display name
        String modelId = first.getString(EntityMaid.MODEL_ID_TAG);
        if (StringUtils.isNotBlank(modelId)) {
            return ClientMultiMaidTooltip.stripModelId(modelId);
        }
        return "?";
    }
}
