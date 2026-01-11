package me.isaiah.multiworld.gui.gif;

import java.util.List;

public final class GifAnimation {
    public final int width;
    public final int height;
    public final List<GifFrame> frames;
    public final int loopCount; // 0 = infinite

    public GifAnimation(int width, int height, List<GifFrame> frames, int loopCount) {
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.loopCount = loopCount;
    }
}