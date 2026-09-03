package studio.fantasyit.maid_rpg_task.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import studio.fantasyit.maid_rpg_task.client.model.FireMagicCircleModel;
import studio.fantasyit.maid_rpg_task.entity.FireMagicCircleEntity;

public class FireMagicCircleRenderer extends GeoEntityRenderer<FireMagicCircleEntity> {

    private static final float SCALE = 2.0f;

    public FireMagicCircleRenderer(EntityRendererProvider.Context context) {
        super(context, new FireMagicCircleModel());
    }

    @Override
    public ResourceLocation getTextureLocation(FireMagicCircleEntity animatable) {
        return FireMagicCircleEntity.getTextureLocation();
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    FireMagicCircleEntity animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
