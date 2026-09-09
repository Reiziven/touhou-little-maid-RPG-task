package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.SphereShieldEntity;

public class SphereShieldModel extends GeoModel<SphereShieldEntity> {

    @Override
    public ResourceLocation getModelResource(SphereShieldEntity animatable) {
        return SphereShieldEntity.getModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SphereShieldEntity animatable) {
        return SphereShieldEntity.getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SphereShieldEntity animatable) {
        return SphereShieldEntity.getAnimationLocation();
    }
}
