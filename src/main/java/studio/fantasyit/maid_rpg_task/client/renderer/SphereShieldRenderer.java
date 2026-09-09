package studio.fantasyit.maid_rpg_task.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.client.model.SphereShieldModel;
import studio.fantasyit.maid_rpg_task.entity.SphereShieldEntity;

public class SphereShieldRenderer extends GeoEntityRenderer<SphereShieldEntity> {

    // geo visible_bounds_width=7 Blockbench units; 16 units = 1 block → 7/16 ≈ 0.44 blocks at scale 1
    private static final float GEO_SIZE_BLOCKS = 7f / 16f;

    public SphereShieldRenderer(EntityRendererProvider.Context context) {
        super(context, new SphereShieldModel());
    }

    @Override
    public ResourceLocation getTextureLocation(SphereShieldEntity animatable) {
        return SphereShieldEntity.getTextureLocation();
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    SphereShieldEntity animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
        LivingEntity maid = animatable.level().getEntitiesOfClass(LivingEntity.class,
                animatable.getBoundingBox().inflate(64, 32, 64),
                e -> e.getUUID().equals(animatable.getMaidUuid())).stream().findFirst().orElse(null);
        float maidHeight = maid != null ? maid.getBbHeight() : 1.8f;
        float scale = (maidHeight * Config.mageShieldScale) / GEO_SIZE_BLOCKS;
        poseStack.scale(scale, scale, scale);
    }
}
