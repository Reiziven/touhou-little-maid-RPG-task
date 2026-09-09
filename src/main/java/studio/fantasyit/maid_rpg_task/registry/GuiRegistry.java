package studio.fantasyit.maid_rpg_task.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.menu.MaidReviveConfigGui;

public class GuiRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, MaidRpgTask.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MaidReviveConfigGui.Container>> MAID_REVIVE_CONFIG_GUI =
            MENU_TYPES.register("maid_revive_config_gui",
                    () -> IMenuTypeExtension.create((windowId, inv, data) ->
                            new MaidReviveConfigGui.Container(windowId, inv, data.readInt())));

    public static void init(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
