package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.IceSpikeProjectile;

public class IceSpikeModel extends GeoModel<IceSpikeProjectile> {

    @Override
    public ResourceLocation getModelResource(IceSpikeProjectile animatable) {
        return IceSpikeProjectile.getModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(IceSpikeProjectile animatable) {
        return IceSpikeProjectile.getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(IceSpikeProjectile animatable) {
        return IceSpikeProjectile.getAnimationLocation();
    }
}
