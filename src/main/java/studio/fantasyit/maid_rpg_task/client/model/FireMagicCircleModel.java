package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.FireMagicCircleEntity;

public class FireMagicCircleModel extends GeoModel<FireMagicCircleEntity> {
    @Override
    public ResourceLocation getModelResource(FireMagicCircleEntity a) { return FireMagicCircleEntity.getModelLocation(); }
    @Override
    public ResourceLocation getTextureResource(FireMagicCircleEntity a) { return FireMagicCircleEntity.getTextureLocation(); }
    @Override
    public ResourceLocation getAnimationResource(FireMagicCircleEntity a) { return FireMagicCircleEntity.getAnimationLocation(); }
}
