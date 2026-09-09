package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.EarthSpikeEntity;

public class EarthSpikeModel extends GeoModel<EarthSpikeEntity> {

    @Override
    public ResourceLocation getModelResource(EarthSpikeEntity animatable) {
        return EarthSpikeEntity.getModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(EarthSpikeEntity animatable) {
        return EarthSpikeEntity.getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(EarthSpikeEntity animatable) {
        return EarthSpikeEntity.getAnimationLocation();
    }
}
