package dev.felnull.imp.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.felnull.imp.block.MusicManagerBlock;
import dev.felnull.imp.blockentity.BoomboxBlockEntity;
import dev.felnull.imp.client.model.IMPModels;
import dev.felnull.imp.client.renderer.item.AntennaItemRenderer;
import dev.felnull.imp.item.IMPItems;
import dev.felnull.imp.util.IMPItemUtil;
import dev.felnull.otyacraftengine.client.model.SpecialModelLoader;
import dev.felnull.otyacraftengine.client.renderer.blockentity.AbstractBlockEntityRenderer;
import dev.felnull.otyacraftengine.client.util.OERenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class BoomboxBlockEntityRenderer extends AbstractBlockEntityRenderer<BoomboxBlockEntity> {
    private static final Minecraft mc = Minecraft.getInstance();

    protected BoomboxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BoomboxBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float handleRaised = blockEntity.getHandleRaisedProgress(f) / (float) blockEntity.getHandleRaisedAll();
        float lidOpen = blockEntity.getLidOpenProgress(f) / (float) blockEntity.getLidOpenProgressAll();
        var state = blockEntity.getBlockState();
        renderBoombox(poseStack, multiBufferSource, state.getValue(MusicManagerBlock.FACING), i, j, f, handleRaised, lidOpen, blockEntity.getButtons(), blockEntity.getCassetteTape(), blockEntity.getAntenna(), blockEntity.getParabolicAntennaProgress(f), blockEntity.getAntennaProgress(f) / 30f, blockEntity.isChangeCassetteTape(), blockEntity.getOldCassetteTape());
    }

    public static void renderBoombox(PoseStack poseStack, MultiBufferSource multiBufferSource, Direction direction, int i, int j, float f, float handleRaised, float lidOpen, BoomboxBlockEntity.Buttons buttons, ItemStack cassetteTape, ItemStack antenna, float parabolicAntennaRoted, float antennaPar, boolean changeCassetteTape, ItemStack oldCassetteTape) {
        var spml = SpecialModelLoader.getInstance();
        var vc = multiBufferSource.getBuffer(Sheets.cutoutBlockSheet());

        var handleM = spml.getModel(IMPModels.BOOMBOX_HANDLE);
        var lidM = spml.getModel(IMPModels.BOOMBOX_LID);
        var buttonsM = spml.getModel(IMPModels.BOOMBOX_BUTTONS);

        poseStack.pushPose();
        OERenderUtil.poseRotateDirection(poseStack, direction, 1);

        if (lidOpen != 0) {
            poseStack.pushPose();
            OERenderUtil.poseTrans16(poseStack, 7.7, 3.225, 6);
            OERenderUtil.poseScaleAll(poseStack, 0.72f);
            mc.getItemRenderer().renderStatic(changeCassetteTape ? oldCassetteTape : cassetteTape, ItemTransforms.TransformType.FIXED, i, j, poseStack, multiBufferSource, 0);
            poseStack.popPose();
        }

        renderAntenna(poseStack, multiBufferSource, i, j, antenna, parabolicAntennaRoted, antennaPar);

        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 1, 8, 6);
        OERenderUtil.poseTrans16(poseStack, 0.5, 0.5, 0.5);
        OERenderUtil.poseRotateX(poseStack, (1f - handleRaised) * 90f);
        OERenderUtil.poseTrans16(poseStack, -0.5, -0.5, -0.5);
        OERenderUtil.renderModel(poseStack, vc, handleM, i, j);
        poseStack.popPose();

        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 5.5, 1.5, 5);
        OERenderUtil.poseTrans16(poseStack, 0.125, 0.125, 0.125);
        OERenderUtil.poseRotateX(poseStack, lidOpen * -40f);
        OERenderUtil.poseTrans16(poseStack, -0.125, -0.125, -0.125);
        OERenderUtil.renderModel(poseStack, vc, lidM, i, j);
        poseStack.popPose();

        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 12.25, 9, 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 11.25, 9 - (buttons.radio() ? 0.5 : 0), 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 10.25, 9 - (buttons.start() ? 0.5 : 0), 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 9.25, 9 - (buttons.pause() ? 0.5 : 0), 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 8.25, 9 - (buttons.stop() ? 0.5 : 0), 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 7.25, 9 - (buttons.loop() ? 0.5 : 0), 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();

        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 5.55, 9, 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 4.55, 9, 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 3.55, 9 - (buttons.volMute() ? 0.5 : 0), 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();
        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, 2.55, 9 - (buttons.volMax() ? 0.5 : 0), 5.75);
        OERenderUtil.renderModel(poseStack, vc, buttonsM, i, j);
        poseStack.popPose();


        poseStack.popPose();
    }

    private static void renderAntenna(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, ItemStack antenna, float roted, float antennaPar) {
        if (!IMPItemUtil.isAntenna(antenna)) return;
        if (antenna.is(IMPItems.ANTENNA)) {
            poseStack.pushPose();
            float ws = 0.025f / 2f;
            OERenderUtil.poseTrans16(poseStack, 0.25, 9, 10.25);
            poseStack.translate(ws, ws, ws);
            OERenderUtil.poseRotateZ(poseStack, 90);
            poseStack.translate(-ws, -ws, -ws);
            OERenderUtil.poseScaleAll(poseStack, 0.75f);
            AntennaItemRenderer.renderAntenna(poseStack, multiBufferSource, i, j, (-0.5f + Math.max(antennaPar, 0.5f)) * 2f, -90 + 30 * Math.min(antennaPar, 0.5f) * 2f);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            OERenderUtil.poseTrans16(poseStack, 0.85, 8, 10.1);
            OERenderUtil.poseScaleAll(poseStack, 0.72f);
            OERenderUtil.poseRotateX(poseStack, 35);
            OERenderUtil.poseRotateZ(poseStack, 35);
            OERenderUtil.poseRotateY(poseStack, roted);
            OERenderUtil.poseTrans16(poseStack, 0, 1.3, 0);
            OERenderUtil.poseRotateX(poseStack, -30 + Math.abs(-0.5f + (roted % 120 / 120f)) * 2f * 60);
            OERenderUtil.poseTrans16(poseStack, 0, -1.3, 0);
            mc.getItemRenderer().renderStatic(antenna, ItemTransforms.TransformType.GROUND, i, j, poseStack, multiBufferSource, 0);
            poseStack.popPose();
        }
    }

}