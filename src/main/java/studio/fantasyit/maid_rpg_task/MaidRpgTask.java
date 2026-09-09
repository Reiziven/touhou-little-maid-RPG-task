package studio.fantasyit.maid_rpg_task;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import studio.fantasyit.maid_rpg_task.registry.ConditionRegistry;
import studio.fantasyit.maid_rpg_task.registry.DataComponentRegistry;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;
import studio.fantasyit.maid_rpg_task.registry.GuiRegistry;
import studio.fantasyit.maid_rpg_task.registry.ItemRegistry;

@Mod(MaidRpgTask.MODID)
public class MaidRpgTask {

    public static final String MODID = "maid_rpg_task";

    public MaidRpgTask(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        boolean hasClothConfig = FMLLoader.getLoadingModList().getModFileById("cloth_config") != null;
        if (hasClothConfig) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                    (mc, screen) -> studio.fantasyit.maid_rpg_task.client.ClothConfigScreen.create(screen));
        } else {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
        GuiRegistry.init(modEventBus);
        DataComponentRegistry.init(modEventBus);
        EntityRegistry.init(modEventBus);
        EffectRegistry.init(modEventBus);
        ItemRegistry.init(modEventBus);
        ConditionRegistry.init(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTab);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Crafting condition registration via NeoForge is done via CraftingCondition registry
        // handled through the mod's crafting_conditions registration in data
    }

    private void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation tabLoc = event.getTabKey().location();
        if (tabLoc.getNamespace().equals("touhou_little_maid") && tabLoc.getPath().equals("main")) {
            event.accept(ItemRegistry.MASTER_SOUL_SPELL.get());
        }
    }
}
