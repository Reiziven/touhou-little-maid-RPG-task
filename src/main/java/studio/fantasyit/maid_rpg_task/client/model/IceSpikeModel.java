package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.IceSpikeProjectile;

public class IceSpikeModel extends GeoModel<IceSpikeProjectile> {
    @Override
    public ResourceLocation getModelResource(IceSpikeProjectile a) { return IceSpikeProjectile.getModelLocation(); }
    @Override
    public ResourceLocation getTextureResource(IceSpikeProjectile a) { return IceSpikeProjectile.getTextureLocation(); }
    @Override
    public ResourceLocation getAnimationResource(IceSpikeProjectile a) { return IceSpikeProjectile.getAnimationLocation(); }
}
