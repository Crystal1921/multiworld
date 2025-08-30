package me.isaiah.multiworld.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.registry.ItemRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

@EventBusSubscriber(value = Dist.CLIENT, modid = MultiworldMod.MOD_ID)
public class RenderPortalEvent {
    @SubscribeEvent
    public static void renderBEOutLines(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        var levelRenderer = event.getLevelRenderer();
        if (levelRenderer.entityEffect == null) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (player.getMainHandItem().is(ItemRegistry.PortalFinder)) {
            ResourceLocation registry = player.level().dimension().location();
            Camera camera = event.getCamera();
            BlockState state = Blocks.STONE.defaultBlockState();
            RenderType outline = RenderType.outline(InventoryMenu.BLOCK_ATLAS);
            OutlineBufferSource outlineSource = mc.renderBuffers().outlineBufferSource();
            PoseStack poseStack = event.getPoseStack();
            PortalCommand.KNOWN_PORTALS.values()
                    .stream()
                    .filter(p -> p.getOriginWorldId().equals(registry))
                    .forEach(p -> {
                        var vec3 = camera.getPosition();
                        BlockPos pos = p.getMaxPos();
                        double d0 = vec3.x();
                        double d1 = vec3.y();
                        double d2 = vec3.z();
                        poseStack.pushPose();
                        poseStack.translate((double) pos.getX() - d0, (double) pos.getY() - d1, (double) pos.getZ() - d2);
                        mc.getBlockRenderer().getModelRenderer().renderModel(
                                poseStack.last(), outlineSource.getBuffer(outline), state, p.getModel(),
                                0.0F, 0.0F, 0.0F, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, outline);
                        poseStack.popPose();
                    });
            mc.renderBuffers().outlineBufferSource().endOutlineBatch();
            levelRenderer.entityEffect.process(event.getPartialTick().getGameTimeDeltaTicks());
            mc.getMainRenderTarget().bindWrite(false);
        }
    }

}
