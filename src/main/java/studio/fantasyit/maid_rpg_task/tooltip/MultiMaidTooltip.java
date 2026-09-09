package studio.fantasyit.maid_rpg_task.tooltip;

import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public record MultiMaidTooltip(List<Entry> maids) implements TooltipComponent {
    public record Entry(String modelId, String customName, YsmMaidInfo ysmMaidInfo) {}
}
