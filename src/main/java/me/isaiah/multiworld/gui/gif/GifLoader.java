package me.isaiah.multiworld.gui.gif;

import com.madgag.gif.fmsware.GifDecoder;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class GifLoader {

    public static GifAnimation load(InputStream inputStream) {
        GifDecoder decoder = new GifDecoder();
        int status = decoder.read(inputStream);

        if (status != GifDecoder.STATUS_OK) {
            throw new IllegalStateException("Failed to decode GIF, status=" + status);
        }

        int frameCount = decoder.getFrameCount();
        List<GifFrame> frames = new ArrayList<>(frameCount);

        for (int i = 0; i < frameCount; i++) {
            BufferedImage img = decoder.getFrame(i);
            int delay = decoder.getDelay(i);
            frames.add(new GifFrame(img, delay));
        }

        return new GifAnimation(
                decoder.getFrameSize().width,
                decoder.getFrameSize().height,
                frames,
                decoder.getLoopCount()
        );
    }
}
