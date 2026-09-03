package studio.fantasyit.maid_rpg_task.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import studio.fantasyit.maid_rpg_task.client.model.IceSpikeModel;
import studio.fantasyit.maid_rpg_task.entity.IceSpikeProjectile;

public class IceSpikeRenderer extends GeoEntityRenderer<IceSpikeProjectile> {

    public IceSpikeRenderer(EntityRendererProvider.Context context) {
        super(context, new IceSpikeModel());
    }

    @Override
    public ResourceLocation getTextureLocation(IceSpikeProjectile animatable) {
        return IceSpikeProjectile.getTextureLocation();
    }
}
