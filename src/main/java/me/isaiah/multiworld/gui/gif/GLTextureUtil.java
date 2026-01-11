package me.isaiah.multiworld.gui.gif;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public final class GLTextureUtil {

    public static int createTexture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();

        int texId = GL11.glGenTextures();

        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA8,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                buffer
        );

        // 设置纹理参数
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // 解绑纹理
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        MemoryUtil.memFree(buffer);
        return texId;
    }

    /**
     * 删除纹理，释放 GPU 资源
     */
    public static void deleteTexture(int textureId) {
        if (textureId != 0) {
            RenderSystem.assertOnRenderThreadOrInit();
            GL11.glDeleteTextures(textureId);
        }
    }
}
