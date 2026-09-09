package studio.fantasyit.maid_rpg_task.menu;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.data.MaidReviveConfig;
import studio.fantasyit.maid_rpg_task.network.MaidConfigurePacket;
import studio.fantasyit.maid_rpg_task.registry.GuiRegistry;
import studio.fantasyit.maid_rpg_task.util.TranslateUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
    protected void initAdditionWidgets() {
        super.initAdditionWidgets();

        int startLeft = leftPos + 87;
        int startTop = topPos + 36;

        addToggle(startLeft, startTop, "gui.maid_rpg_task.revive.ownerOnly",
                this.currentData::ownerOnly, this.currentData::ownerOnly, "ownerOnly");
        startTop += 13;

        addToggle(startLeft, startTop, "gui.maid_rpg_task.heal.owner",
                this.currentData::healOwner, this.currentData::healOwner, "healOwner");
        startTop += 13;

        addToggle(startLeft, startTop, "gui.maid_rpg_task.heal.allies",
                this.currentData::healAllies, this.currentData::healAllies, "healAllies");
        startTop += 13;

        addToggle(startLeft, startTop, "gui.maid_rpg_task.heal.self",
                this.currentData::healSelf, this.currentData::healSelf, "healSelf");
        startTop += 13;

        addToggle(startLeft, startTop, "gui.maid_rpg_task.buff.owner",
                this.currentData::buffOwner, this.currentData::buffOwner, "buffOwner");
        startTop += 13;

        addToggle(startLeft, startTop, "gui.maid_rpg_task.buff.allies",
                this.currentData::buffAllies, this.currentData::buffAllies, "buffAllies");
        startTop += 13;

        addToggle(startLeft, startTop, "gui.maid_rpg_task.buff.self",
                this.currentData::buffSelf, this.currentData::buffSelf, "buffSelf");
        startTop += 13;

        // Feed-the-owner is a brand-new feature — only show the toggle when the
        // mod-wide feature switch is enabled, mirroring how other addition guis
        // hide buttons for disabled features (see MaidLoggingConfigGui).
        if (Config.enableFeedTask) {
            addToggle(startLeft, startTop, "gui.maid_rpg_task.feed.owner",
                    this.currentData::feedOwner, this.currentData::feedOwner, "feedOwner");
            startTop += 13;
        }
    }

    /**
     * Adds a single yes/no {@link MaidConfigButton} bound to a boolean field on {@link #currentData},
     * syncing the change to the server via {@link MaidConfigurePacket}.
     *
     * @param getter   reads the current value from {@link #currentData}
     * @param setter   writes the new value back onto {@link #currentData}
     * @param dataName the {@link MaidReviveConfig.Data#setConfigValue} key for this field
     */
    private void addToggle(int x, int y, String translationKey,
                            Supplier<Boolean> getter,
                            Consumer<Boolean> setter,
                            String dataName) {
        this.addRenderableWidget(new MaidConfigButton(x, y,
                Component.translatable(translationKey),
                TranslateUtil.getBooleanTranslate(getter.get()),
                button -> {
                    setter.accept(false);
                    button.setValue(TranslateUtil.getBooleanTranslate(false));
                    MaidConfigurePacket.send(this.maid, MaidReviveConfig.LOCATION, dataName, "false");
                },
                button -> {
                    setter.accept(true);
                    button.setValue(TranslateUtil.getBooleanTranslate(true));
                    MaidConfigurePacket.send(this.maid, MaidReviveConfig.LOCATION, dataName, "true");
                }
        ));
    }
}
