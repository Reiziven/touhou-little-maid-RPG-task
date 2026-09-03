package studio.fantasyit.maid_rpg_task.menu;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import studio.fantasyit.maid_rpg_task.data.MaidReviveConfig;
import studio.fantasyit.maid_rpg_task.network.MaidConfigurePacket;
import studio.fantasyit.maid_rpg_task.registry.GuiRegistry;
import studio.fantasyit.maid_rpg_task.util.TranslateUtil;

public class MaidReviveConfigGui extends MaidTaskConfigGui<MaidReviveConfigGui.Container> {
    private MaidReviveConfig.Data currentData;

    public MaidReviveConfigGui(Container screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    public static class Container extends TaskConfigContainer {
        public Container(int id, Inventory inventory, int entityId) {
            super(GuiRegistry.MAID_REVIVE_CONFIG_GUI.get(), id, inventory, entityId);
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            if (this.maid == null) {
                return false;
            }
            if (!maid.isOwnedBy(player)) {
                return false;
            }
            if (!maid.isAlive() || maid.isSleeping()) {
                return false;
            }
            return player.canReach(this.maid, 3);
        }
    }

    @Override
    protected void initAdditionData() {
        this.currentData = this.maid.getOrCreateData(MaidReviveConfig.KEY, MaidReviveConfig.Data.getDefault());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        // Parent is abstract, nothing to call
    }

    @Override
    protected void initAdditionWidgets() {
        super.initAdditionWidgets();

        int startLeft = leftPos + 87;
        int startTop = topPos + 36;
        this.addRenderableWidget(new MaidConfigButton(startLeft, startTop + 0,
                Component.translatable("gui.maid_rpg_task.revive.ownerOnly"),
                TranslateUtil.getBooleanTranslate(this.currentData.ownerOnly()),
                button -> {
                    this.currentData.ownerOnly(false);
                    button.setValue(TranslateUtil.getBooleanTranslate(false));
                    MaidConfigurePacket.send(this.maid, MaidReviveConfig.LOCATION, "ownerOnly", "false");
                },
                button -> {
                    this.currentData.ownerOnly(true);
                    button.setValue(TranslateUtil.getBooleanTranslate(true));
                    MaidConfigurePacket.send(this.maid, MaidReviveConfig.LOCATION, "ownerOnly", "true");
                }
        ));
    }
}
