package studio.fantasyit.maid_rpg_task.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.entity.*;

public class EntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MaidRpgTask.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HealMagicCircleEntity>> HEAL_MAGIC_CIRCLE =
            ENTITIES.register("heal_magic_circle",
                    () -> EntityType.Builder.<HealMagicCircleEntity>of(HealMagicCircleEntity::new, MobCategory.MISC)
                            .sized(2.0f, 0.5f).clientTrackingRange(16).build("heal_magic_circle"));

    public static final DeferredHolder<EntityType<?>, EntityType<EarthSpikeEntity>> EARTH_SPIKE =
            ENTITIES.register("earth_spike",
                    () -> EntityType.Builder.<EarthSpikeEntity>of(EarthSpikeEntity::new, MobCategory.MISC)
                            .sized(1.5f, 2.0f).clientTrackingRange(16).build("earth_spike"));

    public static final DeferredHolder<EntityType<?>, EntityType<FrontSpikeEntity>> FRONT_SPIKE =
            ENTITIES.register("front_spike",
                    () -> EntityType.Builder.<FrontSpikeEntity>of(FrontSpikeEntity::new, MobCategory.MISC)
                            .sized(1.5f, 2.0f).clientTrackingRange(16).build("front_spike"));

    public static final DeferredHolder<EntityType<?>, EntityType<TornadoEntity>> TORNADO =
            ENTITIES.register("tornado",
                    () -> EntityType.Builder.<TornadoEntity>of(TornadoEntity::new, MobCategory.MISC)
                            .sized(1.5f, 3.0f).clientTrackingRange(16).build("tornado"));

    public static final DeferredHolder<EntityType<?>, EntityType<FireMagicCircleEntity>> FIRE_MAGIC_CIRCLE =
            ENTITIES.register("fire_magic_circle",
                    () -> EntityType.Builder.<FireMagicCircleEntity>of(FireMagicCircleEntity::new, MobCategory.MISC)
                            .sized(2.0f, 0.5f).clientTrackingRange(16).build("fire_magic_circle"));

    public static final DeferredHolder<EntityType<?>, EntityType<IceMagicCircleEntity>> ICE_MAGIC_CIRCLE =
            ENTITIES.register("ice_magic_circle",
                    () -> EntityType.Builder.<IceMagicCircleEntity>of(IceMagicCircleEntity::new, MobCategory.MISC)
                            .sized(2.0f, 0.5f).clientTrackingRange(16).build("ice_magic_circle"));

    public static final DeferredHolder<EntityType<?>, EntityType<IceSpikeProjectile>> ICE_SPIKE =
            ENTITIES.register("ice_spike",
                    () -> EntityType.Builder.<IceSpikeProjectile>of(IceSpikeProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(16).build("ice_spike"));

    public static void init(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
