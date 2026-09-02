package com.harekuto.dreadstalker.client.model;

import com.harekuto.dreadstalker.DreadstalkerMod;
import com.harekuto.dreadstalker.entity.DreadstalkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DreadstalkerModel<T extends DreadstalkerEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(DreadstalkerMod.MODID + ":dreadstalker"), "main");

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart ribs;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftHand;
    private final ModelPart rightHand;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;

    public DreadstalkerModel(ModelPart root) {
        this.root = root;
        torso = root.getChild("torso");
        head = torso.getChild("head");
        jaw = head.getChild("jaw");
        ribs = torso.getChild("ribs");
        leftArm = torso.getChild("left_arm");
        rightArm = torso.getChild("right_arm");
        leftForearm = leftArm.getChild("left_forearm");
        rightForearm = rightArm.getChild("right_forearm");
        leftHand = leftForearm.getChild("left_hand");
        rightHand = rightForearm.getChild("right_hand");
        leftLeg = root.getChild("left_leg");
        rightLeg = root.getChild("right_leg");
        leftFoot = leftLeg.getChild("left_foot");
        rightFoot = rightLeg.getChild("right_foot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("pelvis",
                CubeListBuilder.create().texOffs(72, 46)
                        .addBox(-4.2F, -2.2F, -2.6F, 8.4F, 5.0F, 5.2F, new CubeDeformation(0.08F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition torso = root.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-5.0F, -18.0F, -3.0F, 10.0F, 19.0F, 6.0F, new CubeDeformation(0.16F))
                        .texOffs(98, 44).addBox(-3.0F, -19.0F, -2.0F, 6.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        torso.addOrReplaceChild("spine",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-1.0F, -17.0F, 3.0F, 2.0F, 18.0F, 2.0F)
                        .texOffs(48, 0).addBox(-0.7F, -20.0F, 3.2F, 1.4F, 4.0F, 1.4F),
                PartPose.ZERO);

        torso.addOrReplaceChild("shoulders",
                CubeListBuilder.create().texOffs(72, 36)
                        .addBox(-8.0F, -18.0F, -2.0F, 16.0F, 3.0F, 4.0F)
                        .texOffs(102, 54).addBox(5.4F, -20.2F, -0.8F, 4.0F, 5.0F, 3.4F, new CubeDeformation(0.15F)),
                PartPose.ZERO);

        torso.addOrReplaceChild("left_shoulder_spike",
                CubeListBuilder.create().texOffs(104, 34).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(7.0F, -18.0F, 1.0F, -0.35F, 0.28F, -0.35F));
        torso.addOrReplaceChild("right_shoulder_spike",
                CubeListBuilder.create().texOffs(104, 34).addBox(-0.8F, -0.8F, 0.0F, 1.6F, 1.6F, 4.6F),
                PartPose.offsetAndRotation(-7.2F, -17.0F, 1.2F, -0.28F, -0.22F, 0.28F));

        PartDefinition ribs = torso.addOrReplaceChild("ribs", CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < 6; i++) {
            float y = -14.5F + i * 2.35F;
            float length = 6.0F - i * 0.18F;
            ribs.addOrReplaceChild("rib_l" + i,
                    CubeListBuilder.create().texOffs(48, 18).addBox(0.0F, -1.0F, -1.0F, length, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(-1.0F, y, -2.3F, 0.0F, 0.0F, -0.31F - i * 0.012F));
            ribs.addOrReplaceChild("rib_r" + i,
                    CubeListBuilder.create().texOffs(48, 18).addBox(-length, -1.0F, -1.0F, length, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(1.0F, y, -2.3F, 0.0F, 0.0F, 0.31F + i * 0.012F));
        }

        for (int i = 0; i < 5; i++) {
            torso.addOrReplaceChild("spike" + i,
                    CubeListBuilder.create().texOffs(104, 34).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.4F + i * 0.35F),
                    PartPose.offsetAndRotation((i % 2 == 0 ? 0.35F : -0.35F), -16.0F + i * 3.4F, 3.0F,
                            0.15F + i * 0.045F, (i % 2 == 0 ? 0.08F : -0.08F), 0.0F));
        }

        PartDefinition head = torso.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.22F))
                        .texOffs(54, 0).addBox(3.6F, -7.2F, -3.8F, 2.2F, 4.0F, 4.2F, new CubeDeformation(0.08F)),
                PartPose.offsetAndRotation(0.0F, -19.0F, -1.0F, -0.04F, 0.0F, 0.0F));

        head.addOrReplaceChild("brow",
                CubeListBuilder.create().texOffs(40, 28).addBox(-5.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, -5.1F, 0.05F, 0.0F, 0.0F));

        head.addOrReplaceChild("eyes",
                CubeListBuilder.create().texOffs(112, 0)
                        .addBox(-3.8F, -5.8F, -5.45F, 2.3F, 2.1F, 0.8F)
                        .texOffs(112, 6)
                        .addBox(1.5F, -5.8F, -5.45F, 2.3F, 2.1F, 0.8F),
                PartPose.ZERO);

        PartDefinition jaw = head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-4.3F, 0.0F, -5.2F, 8.6F, 6.5F, 9.2F, new CubeDeformation(0.05F)),
                PartPose.offset(0.0F, -0.15F, 0.0F));

        for (int i = 0; i < 5; i++) {
            float x = -3.2F + i * 1.6F;
            float tooth = (i == 0 || i == 4) ? 3.7F : 3.15F;
            head.addOrReplaceChild("top_tooth" + i,
                    CubeListBuilder.create().texOffs(120, 18).addBox(-0.45F, 0.0F, -0.45F, 0.9F, tooth, 0.9F),
                    PartPose.offsetAndRotation(x, -0.4F, -5.45F, -0.10F, 0.0F, (i - 2) * 0.025F));
            jaw.addOrReplaceChild("bottom_tooth" + i,
                    CubeListBuilder.create().texOffs(124, 18).addBox(-0.45F, -3.0F, -0.45F, 0.9F, 3.0F, 0.9F),
                    PartPose.offsetAndRotation(x, 0.8F, -5.45F, 0.10F, 0.0F, (2 - i) * 0.025F));
        }

        PartDefinition leftArm = torso.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 60).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.10F)),
                PartPose.offsetAndRotation(7.0F, -15.0F, 0.0F, 0.0F, 0.0F, -0.16F));
        PartDefinition rightArm = torso.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(16, 60).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.06F)),
                PartPose.offsetAndRotation(-7.0F, -15.0F, 0.0F, 0.0F, 0.0F, 0.16F));

        PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create().texOffs(32, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 19.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.16F, 0.0F, 0.0F));
        PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create().texOffs(48, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 19.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.16F, 0.0F, 0.0F));

        PartDefinition leftHand = leftForearm.addOrReplaceChild("left_hand",
                CubeListBuilder.create().texOffs(78, 60).addBox(-2.6F, -0.8F, -2.6F, 5.2F, 4.0F, 5.2F),
                PartPose.offset(0.0F, 18.0F, -0.3F));
        PartDefinition rightHand = rightForearm.addOrReplaceChild("right_hand",
                CubeListBuilder.create().texOffs(98, 60).addBox(-2.6F, -0.8F, -2.6F, 5.2F, 4.0F, 5.2F),
                PartPose.offset(0.0F, 18.0F, -0.3F));

        for (int f = 0; f < 4; f++) {
            float x = (f - 1.5F) * 1.12F;
            float length = 7.5F + (f == 1 || f == 2 ? 1.3F : 0.3F);
            leftHand.addOrReplaceChild("claw" + f,
                    CubeListBuilder.create().texOffs(64, 60).addBox(-0.42F, 0.0F, -0.42F, 0.84F, length, 0.84F),
                    PartPose.offsetAndRotation(x, 2.4F, -1.1F, -0.38F, 0.0F, (f - 1.5F) * 0.035F));
            rightHand.addOrReplaceChild("claw" + f,
                    CubeListBuilder.create().texOffs(68, 60).addBox(-0.42F, 0.0F, -0.42F, 0.84F, length, 0.84F),
                    PartPose.offsetAndRotation(x, 2.4F, -1.1F, -0.38F, 0.0F, (1.5F - f) * 0.035F));
        }

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(76, 0).addBox(-2.1F, 0.0F, -2.1F, 4.2F, 27.0F, 4.2F, new CubeDeformation(0.05F)),
                PartPose.offset(3.0F, -1.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(94, 0).addBox(-2.1F, 0.0F, -2.1F, 4.2F, 27.0F, 4.2F, new CubeDeformation(0.03F)),
                PartPose.offset(-3.0F, -1.0F, 0.0F));

        leftLeg.addOrReplaceChild("left_foot",
                CubeListBuilder.create().texOffs(76, 28).addBox(-2.6F, -1.2F, -5.6F, 5.2F, 3.2F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 26.0F, 1.0F, 0.08F, -0.05F, 0.0F));
        rightLeg.addOrReplaceChild("right_foot",
                CubeListBuilder.create().texOffs(98, 28).addBox(-2.6F, -1.2F, -5.6F, 5.2F, 3.2F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 26.0F, 1.0F, 0.08F, 0.05F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        float lookYaw = Mth.clamp(netHeadYaw, -70.0F, 70.0F) * Mth.DEG_TO_RAD;
        float lookPitch = Mth.clamp(headPitch, -45.0F, 50.0F) * Mth.DEG_TO_RAD;
        head.yRot = lookYaw;
        head.xRot = -0.04F + lookPitch * 0.64F;

        float walk = Mth.sin(limbSwing * 0.62F) * limbSwingAmount;
        float step = Mth.cos(limbSwing * 0.62F) * limbSwingAmount;
        leftLeg.xRot = walk * 0.90F;
        rightLeg.xRot = -walk * 0.90F;
        leftFoot.xRot += -walk * 0.16F;
        rightFoot.xRot += walk * 0.16F;
        leftArm.xRot = -walk * 0.44F - 0.20F;
        rightArm.xRot = walk * 0.44F - 0.20F;
        leftArm.zRot = -0.16F - step * 0.025F;
        rightArm.zRot = 0.16F + step * 0.025F;
        leftForearm.xRot = -0.24F - Mth.cos(ageInTicks * 0.09F) * 0.08F;
        rightForearm.xRot = -0.24F + Mth.cos(ageInTicks * 0.09F) * 0.08F;
        leftHand.zRot = -0.03F + Mth.sin(ageInTicks * 0.07F) * 0.025F;
        rightHand.zRot = 0.03F - Mth.sin(ageInTicks * 0.07F) * 0.025F;

        float breathe = Mth.sin(ageInTicks * 0.075F);
        torso.zRot = Mth.sin(ageInTicks * 0.045F) * 0.035F;
        torso.xRot = -0.10F + breathe * 0.022F;
        head.zRot = Mth.sin(ageInTicks * 0.072F) * 0.055F;
        jaw.xRot = 0.20F + Mth.sin(ageInTicks * 0.115F) * 0.11F;
        ribs.yScale = 1.0F + Mth.sin(ageInTicks * 0.12F) * 0.032F;
        ribs.xScale = 1.0F + breathe * 0.016F;

        int state = entity.getHorrorState();
        if (state == DreadstalkerEntity.STATE_STALKING && !entity.isObserved()) {
            torso.xRot -= 0.08F;
            head.zRot += Mth.sin(ageInTicks * 0.13F) * 0.08F;
            leftArm.xRot -= 0.16F;
            rightArm.xRot -= 0.26F;
            jaw.xRot = 0.34F + Mth.sin(ageInTicks * 0.14F) * 0.12F;
        } else if (state == DreadstalkerEntity.STATE_HUNTING) {
            torso.xRot -= 0.16F;
            leftArm.xRot -= 0.42F;
            rightArm.xRot -= 0.34F;
            leftForearm.xRot -= 0.18F;
            rightForearm.xRot -= 0.22F;
            jaw.xRot = 0.55F;
        } else if (state == DreadstalkerEntity.STATE_RAGE) {
            torso.xRot -= 0.22F;
            torso.zRot += Mth.sin(ageInTicks * 0.31F) * 0.065F;
            head.zRot += Mth.sin(ageInTicks * 0.37F) * 0.09F;
            leftArm.xRot -= 0.62F;
            rightArm.xRot -= 0.62F;
            jaw.xRot = 0.78F + Mth.sin(ageInTicks * 0.24F) * 0.08F;
        }

        float attack = Mth.sin(this.attackTime * Mth.PI);
        if (attack > 0.001F) {
            float slam = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * Mth.PI);
            leftArm.xRot = -1.18F - slam * 0.72F;
            rightArm.xRot = -0.88F - slam * 0.92F;
            leftArm.zRot = -0.28F;
            rightArm.zRot = 0.18F;
            leftForearm.xRot = -0.52F - attack * 0.62F;
            rightForearm.xRot = -0.45F - attack * 0.72F;
            jaw.xRot = 0.86F;
            torso.xRot = -0.18F + attack * 0.22F;
            head.xRot -= attack * 0.14F;
        }

        if (entity.hurtTime > 0) {
            float hurt = entity.hurtTime / 10.0F;
            torso.zRot += Mth.sin(hurt * Mth.PI) * 0.10F;
            head.yRot -= Mth.sin(hurt * Mth.PI) * 0.12F;
        }

        if (entity.isObserved()) {
            torso.zRot *= 0.08F;
            torso.xRot = -0.08F;
            head.zRot = Mth.sin(ageInTicks * 0.34F) * 0.012F;
            jaw.xRot = 0.045F;
            leftForearm.xRot = -0.18F;
            rightForearm.xRot = -0.18F;
            leftHand.zRot = -0.02F;
            rightHand.zRot = 0.02F;
        }

        if (entity.deathTime > 0) {
            float death = Mth.clamp(entity.deathTime / 20.0F, 0.0F, 1.0F);
            torso.zRot += death * 1.18F;
            torso.xRot += death * 0.34F;
            head.zRot -= death * 0.58F;
            jaw.xRot = 0.95F;
            leftArm.zRot -= death * 0.65F;
            rightArm.zRot += death * 0.48F;
            leftLeg.zRot -= death * 0.20F;
            rightLeg.zRot += death * 0.16F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
