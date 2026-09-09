package studio.fantasyit.maid_rpg_task.tooltip;

import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

/**
 * Tooltip data for the multi-maid smart slab.
 * Holds one entry per stored maid (model ID, custom name, ysm info).
 */
public record MultiMaidTooltip(List<Entry> maids) implements TooltipComponent {

    public record Entry(String modelId, String customName, YsmMaidInfo ysmMaidInfo) {}
}
