package studio.fantasyit.maid_rpg_task.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

import java.util.List;

/**
 * Real 1.21.1 data components for maid_rpg_task, replacing the old
 * "stash raw NBT inside minecraft:custom_data" workaround (ItemNbtUtil).
 * <p>
 * MULTI_MAIDS holds the saved data of every maid bundled inside a single
 * Master Soul Spell smart slab. Index 0 is always the "selected"/primary
 * maid, and is mirrored into TouhouLittleMaid's own MAID_INFO component
 * (see MasterSoulSpellEvent#syncPrimaryMaidData) so TLM's tooltip image
 * and anything else reading MAID_INFO keeps working unmodified.
 */
public class DataComponentRegistry {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MaidRpgTask.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<CompoundTag>>> MULTI_MAIDS =
            DATA_COMPONENTS.register("mss_multi_maids", () -> DataComponentType.<List<CompoundTag>>builder()
                    .persistent(CompoundTag.CODEC.listOf())
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()))
                    .build());

    public static void init(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
