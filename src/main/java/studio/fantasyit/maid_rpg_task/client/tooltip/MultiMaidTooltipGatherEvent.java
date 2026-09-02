package studio.fantasyit.maid_rpg_task.client.tooltip;

import com.github.tartaricacid.touhoulittlemaid.compat.ysm.YsmCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;
import studio.fantasyit.maid_rpg_task.tooltip.MultiMaidTooltip;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the multi-maid preview into the tooltip when hovering a
 * SMART_SLAB_HAS_MAID that contains our mss_multi_maids data.
 *
 * Fires on the FORGE event bus (client-side).
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MultiMaidTooltipGatherEvent {

    private static final String MULTI_MAIDS_KEY = "mss_multi_maids";

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!MasterSoulSpellEvent.hasMultiMaidData(stack)) return;

        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        ListTag list = tag.getList(MULTI_MAIDS_KEY, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return;

        List<MultiMaidTooltip.Entry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag maidData = list.getCompound(i);
            String modelId = maidData.getString(EntityMaid.MODEL_ID_TAG);
            if (StringUtils.isBlank(modelId)) continue;

            String customName = "";
            if (maidData.contains("CustomName", Tag.TAG_STRING)) {
                customName = maidData.getString("CustomName");
            }

            YsmMaidInfo ysmInfo = YsmCompat.isInstalled()
                    ? YsmCompat.getYsmMaidInfo(maidData)
                    : YsmMaidInfo.EMPTY;

            entries.add(new MultiMaidTooltip.Entry(modelId, customName, ysmInfo));
        }

        if (entries.isEmpty()) return;

        // Remove any existing single-maid TooltipComponent (TLM's ItemMaidTooltip)
        // so we don't show duplicates — keep only text entries (Left) before adding ours
        event.getTooltipElements().removeIf(e -> e.right().isPresent());

        // Add our multi-maid component
        event.getTooltipElements().add(Either.right(new MultiMaidTooltip(entries)));
    }
}
