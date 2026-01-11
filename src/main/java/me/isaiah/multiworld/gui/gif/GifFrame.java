package me.isaiah.multiworld.gui.gif;

import java.awt.image.BufferedImage;

public final class GifFrame {
    public final BufferedImage image;
    public final int delayMs;

    public GifFrame(BufferedImage image, int delayMs) {
        this.image = image;
        this.delayMs = delayMs;
    }
}