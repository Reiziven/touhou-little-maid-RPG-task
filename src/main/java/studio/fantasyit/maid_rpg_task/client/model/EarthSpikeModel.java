package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.EarthSpikeEntity;

public class EarthSpikeModel extends GeoModel<EarthSpikeEntity> {
    @Override
    public ResourceLocation getModelResource(EarthSpikeEntity a) { return EarthSpikeEntity.getModelLocation(); }
    @Override
    public ResourceLocation getTextureResource(EarthSpikeEntity a) { return EarthSpikeEntity.getTextureLocation(); }
    @Override
    public ResourceLocation getAnimationResource(EarthSpikeEntity a) { return EarthSpikeEntity.getAnimationLocation(); }
}
