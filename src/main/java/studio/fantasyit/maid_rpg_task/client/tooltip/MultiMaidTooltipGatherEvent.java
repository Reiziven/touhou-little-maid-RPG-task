package studio.fantasyit.maid_rpg_task.client.tooltip;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.YsmCompat;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.apache.commons.lang3.StringUtils;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;
import studio.fantasyit.maid_rpg_task.tooltip.MultiMaidTooltip;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class MultiMaidTooltipGatherEvent {

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!MasterSoulSpellEvent.hasMultiMaidData(stack)) return;

        List<CompoundTag> list = MasterSoulSpellEvent.getMultiMaidList(stack);
        if (list.isEmpty()) return;

        List<MultiMaidTooltip.Entry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag maidData = list.get(i);
            String modelId = maidData.getString(EntityMaid.MODEL_ID_TAG);
            if (StringUtils.isBlank(modelId)) continue;

            String customName = maidData.contains("CustomName", Tag.TAG_STRING)
                    ? maidData.getString("CustomName") : "";

            YsmMaidInfo ysmInfo = YsmCompat.isInstalled()
                    ? YsmCompat.getYsmMaidInfo(maidData) : YsmMaidInfo.EMPTY;

            entries.add(new MultiMaidTooltip.Entry(modelId, customName, ysmInfo));
        }

        if (entries.isEmpty()) return;

        event.getTooltipElements().removeIf(e -> e.right().isPresent());
        event.getTooltipElements().add(Either.right(new MultiMaidTooltip(entries)));
    }
}

