package me.isaiah.multiworld.gui.webp;

import org.lwjgl.BufferUtils;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class WebPTexture {

    private final int textureId;
    private final int width;
    private final int height;

    private final List<WebPFrame> frames = new ArrayList<>();

    private int currentFrame = 0;
    private long timerNs = 0;

    /* ================= 构造 ================= */

    public WebPTexture(File file) throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("webp").next();
        ImageInputStream stream = ImageIO.createImageInputStream(file);
        reader.setInput(stream);

        int frameCount = reader.getNumImages(true);

        // 用第一帧确定尺寸
        BufferedImage base = reader.read(0);
        width = base.getWidth();
        height = base.getHeight();

        // 画布：用于帧合成
        BufferedImage canvas =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = canvas.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);

        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = reader.read(i);
            IIOMetadata meta = reader.getImageMetadata(i);

            int duration = extractDuration(meta);

            // 👉 核心：叠加到画布（处理增量帧）
            g.drawImage(frame, 0, 0, null);

            // 复制当前画布作为「完整帧」
            BufferedImage full =
                    new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = full.createGraphics();
            fg.drawImage(canvas, 0, 0, null);
            fg.dispose();

            WebPFrame f = new WebPFrame();
            f.pixels = toRGBA(full);
            f.durationMs = duration;

            frames.add(f);
        }

        g.dispose();
        reader.dispose();
        stream.close();

        // OpenGL 纹理
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                width,
                height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                frames.getFirst().pixels
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    /* ================= 更新 ================= */

    public void update(long deltaNs) {
        if (frames.size() <= 1) return;

        timerNs += deltaNs;

        WebPFrame frame = frames.get(currentFrame);
        long frameNs = frame.durationMs * 1_000_000L;

        if (timerNs >= frameNs) {
            timerNs -= frameNs;
            currentFrame = (currentFrame + 1) % frames.size();

            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    width,
                    height,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    frames.get(currentFrame).pixels
            );
        }
    }

    /* ================= 使用 ================= */

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public int id() {
        return textureId;
    }

    public void dispose() {
        glDeleteTextures(textureId);
    }

    /* ================= 工具 ================= */

    private static ByteBuffer toRGBA(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                buf.put((byte) ((argb >> 16) & 0xFF));
                buf.put((byte) ((argb >> 8) & 0xFF));
                buf.put((byte) (argb & 0xFF));
                buf.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        buf.flip();
        return buf;
    }

    private static int extractDuration(IIOMetadata meta) {
        try {
            var tree = meta.getAsTree("javax_imageio_1.0");
            var node = tree.getFirstChild();
            var attr = node.getAttributes().getNamedItem("delayTime");
            return Integer.parseInt(attr.getNodeValue());
        } catch (Exception e) {
            return 100;
        }
    }
}
