package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.FireMagicCircleEntity;

public class FireMagicCircleModel extends GeoModel<FireMagicCircleEntity> {

    @Override
    public ResourceLocation getModelResource(FireMagicCircleEntity animatable) {
        return FireMagicCircleEntity.getModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(FireMagicCircleEntity animatable) {
        return FireMagicCircleEntity.getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(FireMagicCircleEntity animatable) {
        return FireMagicCircleEntity.getAnimationLocation();
    }
}
