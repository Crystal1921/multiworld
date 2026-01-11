package me.isaiah.multiworld.gui.gif;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.GameRenderer;

import java.util.ArrayList;
import java.util.List;

public final class GifPlayer {

    private final List<Integer> textures = new ArrayList<>();
    private final List<Integer> delays = new ArrayList<>();

    private int frameIndex = 0;
    private long nextFrameTime;
    private boolean closed = false;

    public GifPlayer(GifAnimation animation) {
        for (GifFrame frame : animation.frames) {
            textures.add(GLTextureUtil.createTexture(frame.image));
            delays.add(Math.max(frame.delayMs, 10));
        }
        nextFrameTime = System.currentTimeMillis();
    }

    public void render(float x, float y, float w, float h) {
        if (closed) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= nextFrameTime) {
            frameIndex = (frameIndex + 1) % textures.size();
            nextFrameTime = now + delays.get(frameIndex);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, textures.get(frameIndex));

        var bufferBuilder = Tesselator.getInstance().begin(
                com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX
        );

        bufferBuilder.addVertex(x, y + h, 0.0f).setUv(0.0f, 1.0f);
        bufferBuilder.addVertex(x + w, y + h, 0.0f).setUv(1.0f, 1.0f);
        bufferBuilder.addVertex(x + w, y, 0.0f).setUv(1.0f, 0.0f);
        bufferBuilder.addVertex(x, y, 0.0f).setUv(0.0f, 0.0f);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    /**
     * 释放所有纹理资源，防止内存泄漏
     */
    public void close() {
        if (!closed) {
            for (int texture : textures) {
                GLTextureUtil.deleteTexture(texture);
            }
            textures.clear();
            delays.clear();
            closed = true;
        }
    }

    /**
     * 重置动画到第一帧
     */
    public void reset() {
        frameIndex = 0;
        nextFrameTime = System.currentTimeMillis();
    }
}
