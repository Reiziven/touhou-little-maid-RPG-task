package studio.fantasyit.maid_rpg_task.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import studio.fantasyit.maid_rpg_task.client.model.FrontSpikeModel;
import studio.fantasyit.maid_rpg_task.entity.FrontSpikeEntity;

public class FrontSpikeRenderer extends GeoEntityRenderer<FrontSpikeEntity> {

    public FrontSpikeRenderer(EntityRendererProvider.Context context) {
        super(context, new FrontSpikeModel());
    }

    @Override
    public ResourceLocation getTextureLocation(FrontSpikeEntity animatable) {
        return FrontSpikeEntity.getTextureLocation();
    }
}
