package com.harekuto.dreadstalker.client.render;

import com.harekuto.dreadstalker.DreadstalkerMod;
import com.harekuto.dreadstalker.client.model.DreadstalkerModel;
import com.harekuto.dreadstalker.entity.DreadstalkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class DreadstalkerRenderer extends MobRenderer<DreadstalkerEntity, DreadstalkerModel<DreadstalkerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DreadstalkerMod.MODID + ":textures/entity/dreadstalker.png");
    private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(DreadstalkerMod.MODID + ":textures/entity/dreadstalker_eyes.png");

    public DreadstalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadstalkerModel<>(context.bakeLayer(DreadstalkerModel.LAYER)), 0.72F);
        this.addLayer(new EyesLayer<DreadstalkerEntity, DreadstalkerModel<DreadstalkerEntity>>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(EYES_TEXTURE);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(DreadstalkerEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(DreadstalkerEntity entity, PoseStack poseStack, float partialTick) {
        float pulse = entity.getHorrorState() == DreadstalkerEntity.STATE_RAGE
                ? 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.12F) * 0.006F
                : 1.0F;
        poseStack.scale(1.08F * pulse, 1.08F * pulse, 1.08F * pulse);
    }
}
