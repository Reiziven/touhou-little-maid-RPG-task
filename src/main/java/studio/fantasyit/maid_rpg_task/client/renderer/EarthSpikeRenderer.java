package studio.fantasyit.maid_rpg_task.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import studio.fantasyit.maid_rpg_task.client.model.EarthSpikeModel;
import studio.fantasyit.maid_rpg_task.entity.EarthSpikeEntity;

public class EarthSpikeRenderer extends GeoEntityRenderer<EarthSpikeEntity> {

    public EarthSpikeRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthSpikeModel());
    }

    @Override
    public ResourceLocation getTextureLocation(EarthSpikeEntity animatable) {
        return EarthSpikeEntity.getTextureLocation();
    }
}
