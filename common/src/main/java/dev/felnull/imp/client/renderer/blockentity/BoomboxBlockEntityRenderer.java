package dev.felnull.imp.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.felnull.imp.IamMusicPlayer;
import dev.felnull.imp.block.BoomboxBlock;
import dev.felnull.imp.block.BoomboxData;
import dev.felnull.imp.block.MusicManagerBlock;
import dev.felnull.imp.blockentity.BoomboxBlockEntity;
import dev.felnull.imp.client.gui.screen.monitor.boombox.BoomboxMonitor;
import dev.felnull.imp.client.model.IMPModels;
import dev.felnull.imp.client.renderer.item.AntennaItemRenderer;
import dev.felnull.imp.item.IMPItems;
import dev.felnull.imp.util.IMPItemUtil;
import dev.felnull.otyacraftengine.client.renderer.blockentity.AbstractBlockEntityRenderer;
import dev.felnull.otyacraftengine.client.util.OERenderUtils;
import dev.felnull.otyacraftengine.util.OEVoxelShapeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.util.HashMap;
import java.util.Map;

public class BoomboxBlockEntityRenderer extends AbstractBlockEntityRenderer<BoomboxBlockEntity> {
    private static final Map<BoomboxData.MonitorType, BoomboxMonitor> monitors = new HashMap<>();
    private static final Minecraft mc = Minecraft.getInstance();

    protected BoomboxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BoomboxBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        BlockState state = blockEntity.getBlockState();
        var data = blockEntity.getBoomboxData();
        renderBoombox(poseStack, multiBufferSource, state, i, j, f, data, data.getHandleRaisedProgress(f) / (float) data.getHandleRaisedMax(), multiBufferSource.getBuffer(Sheets.cutoutBlockSheet()));
    }

    public static void renderBoombox(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockState blockState, int i, int j, float f, BoomboxData data, float handleRaised, VertexConsumer vertexConsumer) {
        float lidOpen = data.getLidOpenProgress(f) / (float) data.getLidOpenProgressMax();

        var cassetteTape = data.getCassetteTape();
        boolean changeCassetteTape = data.isChangeCassetteTape();
        var oldCassetteTape = data.getOldCassetteTape();


        var lidM = IMPModels.BOOMBOX_LID.get();

        //commenting these out because they arent used in this version. feel free to reimplement, but keep in mind that ive already
        //pretty much deleted the models in this build so you'll have to reimport them
        //and then go through the pain of moving them all around in the code so they fit the new models yada yada etc etc
        // - billnotic
        //--
        //var antenna = data.getAntenna();
        //float parabolicAntennaRoted = data.getParabolicAntennaProgress(f);
        //float antennaPar = data.getAntennaProgress(f) / 30f;
        //var handleM = IMPModels.BOOMBOX_HANDLE.get();
        //var buttonsM = IMPModels.BOOMBOX_BUTTONS.get();
        //var buttons = data.getButtons();

        poseStack.pushPose();
        //old rotation code, only supported horizontal rotation
        //OERenderUtils.poseRotateDirection(poseStack, direction, 1);

        if (blockState.getValue(BoomboxBlock.FACE) == AttachFace.WALL) {
            //OERenderUtils.poseRotateDirection(poseStack, blockState.getValue(BoomboxBlock.FACING), 1);

            if (blockState.getValue(BoomboxBlock.FACING) == Direction.SOUTH) {
                if (blockState.getValue(BoomboxBlock.VERTICAL_DIRECTION)==Direction.DOWN) {
                    OERenderUtils.poseRotateAll(poseStack, 270, 180, 180);
                    poseStack.translate(0f, 0f, -1f);
                } else {
                    OERenderUtils.poseRotateAll(poseStack, 270, 0, 180);
                    poseStack.translate(-1f, 0f, 0f);
                }
            } else if (blockState.getValue(BoomboxBlock.FACING) == Direction.EAST) {
                if (blockState.getValue(BoomboxBlock.VERTICAL_DIRECTION)==Direction.DOWN) {
                    OERenderUtils.poseRotateAll(poseStack, 90, 0, 270);
                    poseStack.translate(-1f, 0f, -1f);
                } else {
                    OERenderUtils.poseRotateAll(poseStack, 270, 0, 270);
                    poseStack.translate(0f, 0f, 0f);
                }
            } else if (blockState.getValue(BoomboxBlock.FACING) == Direction.WEST) {
                if (blockState.getValue(BoomboxBlock.VERTICAL_DIRECTION)==Direction.DOWN) {
                    OERenderUtils.poseRotateAll(poseStack, 270, 180, 270);
                    poseStack.translate(0f, -1f, -1f);
                } else {
                    OERenderUtils.poseRotateAll(poseStack, 90, 180, 270);
                    poseStack.translate(-1f, -1f, 0f);
                }
            } else if (blockState.getValue(BoomboxBlock.FACING) == Direction.NORTH) {
                if (blockState.getValue(BoomboxBlock.VERTICAL_DIRECTION)==Direction.DOWN) {
                    OERenderUtils.poseRotateAll(poseStack, 90, 0, 180);
                    poseStack.translate(-1f, -1f, -1f);
                } else {
                    OERenderUtils.poseRotateAll(poseStack, 90, 180, 180);
                    poseStack.translate(0f, -1f, 0f);
                }
            }
        } else if (blockState.getValue(BoomboxBlock.FACE) == AttachFace.CEILING) {
            //return(OEVoxelShapeUtils.rotateBoxZ180(tempVoxel));
            OERenderUtils.poseRotateDirection(poseStack, blockState.getValue(BoomboxBlock.FACING), 1);
            OERenderUtils.poseRotateAll(poseStack, 0, 0,180);
            poseStack.translate(-1f, -1f, 0f);
        } else {
            OERenderUtils.poseRotateDirection(poseStack, blockState.getValue(BoomboxBlock.FACING), 1);
        }

        if (lidOpen != 0) {
            poseStack.pushPose();
            OERenderUtils.poseTrans16(poseStack, 8f, 2.51f, 3.5f);
            OERenderUtils.poseScaleAll(poseStack, 0.5f);
            mc.getItemRenderer().renderStatic(changeCassetteTape ? oldCassetteTape : cassetteTape, ItemDisplayContext.FIXED, i, j, poseStack, multiBufferSource, mc.level, 0);
            poseStack.popPose();
        }

        //commenting this out as the display is not designed to be rendered by my new models, plus they clash aesthetically.
        //if anyone wants to adjust the position to fit the new models be my guest
        // - billnotic
        //--
        //if (!IamMusicPlayer.getConfig().hideDecorativeAntenna) {
        //    renderAntenna(poseStack, multiBufferSource, i, j, antenna, parabolicAntennaRoted, antennaPar);
        //}

        //no buttons in this version, commenting them out.
        // - billnotic
        //--
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 1, 8, 6);
        //OERenderUtils.poseTrans16(poseStack, 0.5, 0.5, 0.5);
        //OERenderUtils.poseRotateX(poseStack, (1f - handleRaised) * 90f);
        //OERenderUtils.poseTrans16(poseStack, -0.5, -0.5, -0.5);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, handleM, i, j);
        //poseStack.popPose();

        poseStack.pushPose();
        OERenderUtils.poseTrans16(poseStack, 4, 3, 3);
        //OERenderUtils.poseTrans16(poseStack, 0.125, 0.125, 0.125);
        OERenderUtils.poseRotateX(poseStack, lidOpen * -75f);
        //OERenderUtils.poseTrans16(poseStack, -0.125, -0.125, -0.125);
        OERenderUtils.renderModel(poseStack, vertexConsumer, lidM, i, j);
        poseStack.popPose();

        //no buttons in this version, commenting them out.
        // - billnotic
        //--
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 12.25, 9, 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 11.25, 9 - (buttons.radio() ? 0.5 : 0), 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 10.25, 9 - (buttons.start() ? 0.5 : 0), 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 9.25, 9 - (buttons.pause() ? 0.5 : 0), 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 8.25, 9, 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 7.25, 9 - (buttons.loop() ? 0.5 : 0), 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();

        //no buttons in this version, commenting them out.
        // - billnotic
        //--
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 5.55, 9, 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 4.55, 9, 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 3.55, 9 - (buttons.volMute() ? 0.5 : 0), 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();
        //poseStack.pushPose();
        //OERenderUtils.poseTrans16(poseStack, 2.55, 9 - (buttons.volMax() ? 0.5 : 0), 5.75);
        //OERenderUtils.renderModel(poseStack, vertexConsumer, buttonsM, i, j);
        //poseStack.popPose();

        //commenting this out as the display is not designed to be rendered by my new models, plus they clash aesthetically.
        //if anyone wants to adjust the position to fit the new models be my guest
        // - billnotic

        //if (!IamMusicPlayer.getConfig().hideDisplaySprite) {
            //poseStack.pushPose();
            //poseStack.translate(1, 0, 0);
            //OERenderUtils.poseRotateY(poseStack, 180);
            //OERenderUtils.poseTrans16(poseStack, 0.6, 5.6, -4.9);
            //var monitor = getMonitor(data.getMonitorType());
            //float px16 = 1f / 16f;
            //monitor.renderAppearance(poseStack, multiBufferSource, LightTexture.FULL_BRIGHT, j, f, px16 * 14.8f, px16 * 2.8f, data);
            //poseStack.popPose();
        //}

        poseStack.popPose();
    }

    //as the antenna no longer gets rendered, renderAntenna() is useless FYI
    // - billnotic

    private static void renderAntenna(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, ItemStack antenna, float roted, float antennaPar) {
        if (!IMPItemUtil.isAntenna(antenna)) return;
        if (antenna.is(IMPItems.RADIO_ANTENNA.get())) {
            poseStack.pushPose();
            float ws = 0.025f / 2f;
            OERenderUtils.poseTrans16(poseStack, 0.25, 9, 10.25);
            poseStack.translate(ws, ws, ws);
            OERenderUtils.poseRotateZ(poseStack, 90);
            poseStack.translate(-ws, -ws, -ws);
            OERenderUtils.poseScaleAll(poseStack, 0.75f);
            AntennaItemRenderer.renderAntenna(antenna, poseStack, multiBufferSource, i, j, (-0.5f + Math.max(antennaPar, 0.5f)) * 2f, -90 + 30 * Math.min(antennaPar, 0.5f) * 2f);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            OERenderUtils.poseTrans16(poseStack, 0.85, 8, 10.1);
            OERenderUtils.poseScaleAll(poseStack, 0.72f);
            OERenderUtils.poseRotateX(poseStack, 35);
            OERenderUtils.poseRotateZ(poseStack, 35);
            OERenderUtils.poseRotateY(poseStack, roted);
            OERenderUtils.poseTrans16(poseStack, 0, 1.3, 0);
            OERenderUtils.poseRotateX(poseStack, -30 + Math.abs(-0.5f + (roted % 120 / 120f)) * 2f * 60);
            OERenderUtils.poseTrans16(poseStack, 0, -1.3, 0);
            mc.getItemRenderer().renderStatic(antenna, ItemDisplayContext.GROUND, i, j, poseStack, multiBufferSource, mc.level, 0);
            poseStack.popPose();
        }
    }

    private static BoomboxMonitor getMonitor(BoomboxData.MonitorType type) {
        if (monitors.containsKey(type)) return monitors.get(type);

        var monitor = BoomboxMonitor.createdBoomBoxMonitor(type, null);
        monitors.put(type, monitor);
        return monitor;
    }
}
