package studio.fantasyit.maid_rpg_task.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import studio.fantasyit.maid_rpg_task.client.model.IceMagicCircleModel;
import studio.fantasyit.maid_rpg_task.entity.IceMagicCircleEntity;

public class IceMagicCircleRenderer extends GeoEntityRenderer<IceMagicCircleEntity> {

    private static final float SCALE = 2.0f;

    public IceMagicCircleRenderer(EntityRendererProvider.Context context) {
        super(context, new IceMagicCircleModel());
    }

    @Override
    public ResourceLocation getTextureLocation(IceMagicCircleEntity animatable) {
        return IceMagicCircleEntity.getTextureLocation();
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    IceMagicCircleEntity animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
