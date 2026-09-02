package studio.fantasyit.maid_rpg_task.client.tooltip;

import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.YsmCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil;
import com.github.tartaricacid.touhoulittlemaid.util.ParseI18n;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.joml.Quaternionf;
import studio.fantasyit.maid_rpg_task.tooltip.MultiMaidTooltip;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.github.tartaricacid.touhoulittlemaid.client.event.SpecialMaidRenderEvent.EASTER_EGG_MODEL;
import static com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil.clearMaidDataResidue;

@OnlyIn(Dist.CLIENT)
public class ClientMultiMaidTooltip implements ClientTooltipComponent {

    private static final int CELL_HEIGHT = 70; // same height as TLM's single maid tooltip
    private static final int CELL_PADDING = 8; // gap between columns

    private final List<MaidEntry> entries;

    public ClientMultiMaidTooltip(MultiMaidTooltip tooltip) {
        this.entries = new ArrayList<>();
        for (MultiMaidTooltip.Entry e : tooltip.maids()) {
            MaidModelInfo info = CustomPackLoader.MAID_MODELS.getInfo(e.modelId()).orElse(null);
            MutableComponent name = resolveName(info, e);
            this.entries.add(new MaidEntry(info, e, name));
        }
    }

    private static MutableComponent resolveName(MaidModelInfo info, MultiMaidTooltip.Entry e) {
        if (YsmCompat.isInstalled() && e.ysmMaidInfo().isYsmModel()) {
            MutableComponent n = e.ysmMaidInfo().name();
            return n.equals(Component.empty()) ? Component.literal(stripModelId(e.ysmMaidInfo().modelId())) : n;
        }
        if (info != null) {
            return Component.translatable(ParseI18n.getI18nKey(info.getName()));
        }
        // Fallback: strip namespace and slashes from raw model ID
        return Component.literal(stripModelId(e.modelId()));
    }

    /** Strips the namespace prefix and converts path separators to spaces.
     *  e.g. "touhou_little_maid:hakurei/reimu" → "hakurei reimu" */
    public static String stripModelId(String modelId) {
        if (StringUtils.isBlank(modelId)) return "?";
        // Remove namespace (everything up to and including ':')
        int colon = modelId.indexOf(':');
        String path = colon >= 0 ? modelId.substring(colon + 1) : modelId;
        // Take only the last path segment after the final '/'
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        // Replace underscores with spaces for readability
        return name.replace('_', ' ');
    }

    @Override
    public int getHeight() {
        return CELL_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        int total = 0;
        for (MaidEntry e : entries) {
            total += getCellWidth(font, e);
        }
        return Math.max(total, 50);
    }

    /** Width of one maid column: max of name text width and minimum model width, plus padding. */
    private int getCellWidth(Font font, MaidEntry e) {
        int nameWidth = font.width(e.name.copy().withStyle(ChatFormatting.GRAY));
        if (StringUtils.isNotBlank(e.data.customName())) {
            MutableComponent cn = Component.Serializer.fromJson(e.data.customName());
            if (cn != null) nameWidth = font.width(cn.copy().withStyle(ChatFormatting.GRAY));
        }
        return Math.max(nameWidth, 40) + CELL_PADDING;
    }

    @Override
    public void renderImage(Font font, int pX, int pY, GuiGraphics guiGraphics) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return;

        double rot = ((System.currentTimeMillis() / 25.0) % 360);
        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        pose.mul(new Quaternionf().rotateY((float) Math.toRadians(rot)));

        int cursorX = pX;
        for (MaidEntry entry : entries) {
            if (entry.info == null) {
                cursorX += getCellWidth(font, entry);
                continue;
            }

            int cellWidth = getCellWidth(font, entry);
            int posX = cursorX + cellWidth / 2;
            int posY = pY + CELL_HEIGHT - 6;

            // Draw name / custom name
            MutableComponent label;
            if (StringUtils.isNotBlank(entry.data.customName())) {
                MutableComponent cn = Component.Serializer.fromJson(entry.data.customName());
                label = cn != null ? cn : entry.name;
            } else {
                label = entry.name;
            }
            guiGraphics.drawString(font, label.withStyle(ChatFormatting.GRAY), cursorX, pY + 2, 0xFFFFFF);

            // Render maid entity
            EntityMaid maid = getMaidEntity(world);
            if (maid != null) {
                clearMaidDataResidue(maid, false);

                if (StringUtils.isNotBlank(entry.data.customName())) {
                    MutableComponent cn = Component.Serializer.fromJson(entry.data.customName());
                    maid.setCustomName(cn);
                }

                if (entry.info.getEasterEgg() != null) {
                    maid.setModelId(EASTER_EGG_MODEL);
                } else {
                    maid.setModelId(entry.info.getModelId().toString());
                }

                if (YsmCompat.isInstalled() && entry.data.ysmMaidInfo().isYsmModel()) {
                    maid.setIsYsmModel(true);
                    maid.setYsmModel(entry.data.ysmMaidInfo().modelId(),
                            entry.data.ysmMaidInfo().textureId(),
                            entry.data.ysmMaidInfo().name());
                } else {
                    maid.setIsYsmModel(false);
                }

                float scale = (float) (20 * entry.info.getRenderItemScale());
                guiGraphics.enableScissor(cursorX, posY - 50, cursorX + cellWidth, posY);
                InventoryScreen.renderEntityInInventory(guiGraphics, posX, posY, (int) scale, pose, null, maid);
                guiGraphics.disableScissor();
            }

            cursorX += cellWidth;
        }
    }

    @Nullable
    private static EntityMaid getMaidEntity(Level world) {
        try {
            return (EntityMaid) EntityCacheUtil.ENTITY_CACHE.get(EntityMaid.TYPE, () -> {
                var e = EntityMaid.TYPE.create(world);
                return Objects.requireNonNullElseGet(e, () -> new EntityMaid(world));
            });
        } catch (ExecutionException | ClassCastException e) {
            return null;
        }
    }

    private record MaidEntry(
            @Nullable MaidModelInfo info,
            MultiMaidTooltip.Entry data,
            MutableComponent name
    ) {}
}
