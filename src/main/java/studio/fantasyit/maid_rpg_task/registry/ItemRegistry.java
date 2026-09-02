package studio.fantasyit.maid_rpg_task.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.item.bauble.MasterSoulSpellItem;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MaidRpgTask.MODID);

    public static final RegistryObject<Item> MASTER_SOUL_SPELL =
            ITEMS.register("master_soul_spell", MasterSoulSpellItem::new);

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
