package me.isaiah.multiworld.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.registry.ItemRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = MultiworldMod.MOD_ID)
public class RenderPortalEvent {
    @SubscribeEvent
    public static void renderBEOutLines(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        var levelRenderer = event.getLevelRenderer();
        if (levelRenderer.entityEffect == null) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (player.getMainHandItem().is(ItemRegistry.PortalFinder)) {
            ResourceLocation registry = player.level().dimension().location();
            Camera camera = event.getCamera();
            PoseStack poseStack = event.getPoseStack();
            PortalCommand.KNOWN_PORTALS.values()
                    .stream()
                    .filter(p -> p.getOriginWorldId().equals(registry))
                    .forEach(p -> {
                        BlockPos min = p.getMinPos();
                        BlockPos max = p.getMaxPos();

                        var vec3 = camera.getPosition();
                        double d0 = vec3.x();
                        double d1 = vec3.y();
                        double d2 = vec3.z();
                        poseStack.pushPose();
                        poseStack.translate((double) min.getX() - d0, (double) min.getY() - d1, (double) min.getZ() - d2);

                        int dX = max.getX() - min.getX() + 1;
                        int dY = max.getY() - min.getY() + 1;
                        int dZ = max.getZ() - min.getZ() + 1;
                        VertexConsumer vertexconsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
                        LevelRenderer.renderLineBox(poseStack, vertexconsumer, 0, 0, 0, dX, dY, dZ, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
                        poseStack.popPose();
                    });
        }
    }
}
