package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.IceMagicCircleEntity;

public class IceMagicCircleModel extends GeoModel<IceMagicCircleEntity> {
    @Override
    public ResourceLocation getModelResource(IceMagicCircleEntity a) { return IceMagicCircleEntity.getModelLocation(); }
    @Override
    public ResourceLocation getTextureResource(IceMagicCircleEntity a) { return IceMagicCircleEntity.getTextureLocation(); }
    @Override
    public ResourceLocation getAnimationResource(IceMagicCircleEntity a) { return IceMagicCircleEntity.getAnimationLocation(); }
}
