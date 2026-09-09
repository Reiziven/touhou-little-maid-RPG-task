package studio.fantasyit.maid_rpg_task;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import studio.fantasyit.maid_rpg_task.condition.BaubleCraftableCondition;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;
import studio.fantasyit.maid_rpg_task.registry.GuiRegistry;
import studio.fantasyit.maid_rpg_task.registry.ItemRegistry;

@Mod(MaidRpgTask.MODID)
public class MaidRpgTask {

    public static final String MODID = "maid_rpg_task";

    @SuppressWarnings("removal")
    public MaidRpgTask() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        GuiRegistry.init(modEventBus);
        EntityRegistry.init(modEventBus);
        EffectRegistry.init(modEventBus);
        ItemRegistry.init(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTab);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                CraftingHelper.register(BaubleCraftableCondition.Serializer.INSTANCE)
        );
    }

    private void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation tabLoc = event.getTabKey().location();
        // TLM main tab — this is a TLM addon so it belongs here
        if (tabLoc.getNamespace().equals("touhou_little_maid") && tabLoc.getPath().equals("main")) {
            event.accept(ItemRegistry.MASTER_SOUL_SPELL.get());
        }
    }
}
