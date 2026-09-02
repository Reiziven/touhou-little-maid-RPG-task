package studio.fantasyit.maid_rpg_task.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import studio.fantasyit.maid_rpg_task.client.model.TornadoModel;
import studio.fantasyit.maid_rpg_task.entity.TornadoEntity;

public class TornadoRenderer extends GeoEntityRenderer<TornadoEntity> {

    private static final float SCALE = 8.0f;

    public TornadoRenderer(EntityRendererProvider.Context context) {
        super(context, new TornadoModel());
    }

    @Override
    public ResourceLocation getTextureLocation(TornadoEntity animatable) {
        return TornadoEntity.getTextureLocation();
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    TornadoEntity animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
