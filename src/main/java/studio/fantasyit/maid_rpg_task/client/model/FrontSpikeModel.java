package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.FrontSpikeEntity;

public class FrontSpikeModel extends GeoModel<FrontSpikeEntity> {

    @Override
    public ResourceLocation getModelResource(FrontSpikeEntity animatable) {
        return FrontSpikeEntity.getModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(FrontSpikeEntity animatable) {
        return FrontSpikeEntity.getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(FrontSpikeEntity animatable) {
        return FrontSpikeEntity.getAnimationLocation();
    }
}
