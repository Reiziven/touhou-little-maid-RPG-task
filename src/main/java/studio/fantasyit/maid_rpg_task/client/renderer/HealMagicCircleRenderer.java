package studio.fantasyit.maid_rpg_task.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_rpg_task.entity.HealMagicCircleEntity;

/** No-op renderer — the entity is invisible and particle-only. */
public class HealMagicCircleRenderer extends EntityRenderer<HealMagicCircleEntity> {

    public HealMagicCircleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HealMagicCircleEntity entity) {
        return null;
    }
}
