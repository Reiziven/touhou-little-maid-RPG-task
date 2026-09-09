package studio.fantasyit.maid_rpg_task.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import studio.fantasyit.maid_rpg_task.entity.TornadoEntity;

public class TornadoModel extends GeoModel<TornadoEntity> {
    @Override
    public ResourceLocation getModelResource(TornadoEntity a) { return TornadoEntity.getModelLocation(); }
    @Override
    public ResourceLocation getTextureResource(TornadoEntity a) { return TornadoEntity.getTextureLocation(); }
    @Override
    public ResourceLocation getAnimationResource(TornadoEntity a) { return TornadoEntity.getAnimationLocation(); }
}
