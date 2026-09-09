package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.TornadoEntity;

public class TornadoModel extends GeoModel<TornadoEntity> {

    @Override
    public ResourceLocation getModelResource(TornadoEntity animatable) {
        return TornadoEntity.getModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(TornadoEntity animatable) {
        return TornadoEntity.getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(TornadoEntity animatable) {
        return TornadoEntity.getAnimationLocation();
    }
}
