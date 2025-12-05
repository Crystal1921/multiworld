package me.isaiah.multiworld.gui;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MapConfigScreen extends ConfigurationScreen.ConfigurationSectionScreen {
    public MapConfigScreen(Context context, Component title) {
        super(context, title);
    }

    public static MapConfigScreen create(ConfigurationScreen screen, net.neoforged.fml.config.ModConfig.Type type, net.neoforged.fml.config.ModConfig config, net.minecraft.network.chat.Component title) {
        return new MapConfigScreen(
                ConfigurationScreen.ConfigurationSectionScreen.Context.top(
                        config.getModId(),
                        screen,
                        config,
                        (context, string, element) -> element // 防止空指针的默认 Filter
                ),
                title
        );
    }

    @Override
    protected @Nullable Element createDoubleValue(@NotNull String key, ModConfigSpec.ValueSpec spec, @NotNull Supplier<Double> source, @NotNull Consumer<Double> target) {
        ModConfigSpec.Range<Double> range = spec.getRange();

        // 只有定义了范围（Range）才能创建滑块组合
        if (range != null) {
            DoubleControlWidget widget = new DoubleControlWidget(this,
                    0, 0, 150, 20, // 默认尺寸，会被列表布局自动调整
                    getTranslationComponent(key),
                    range,
                    source,
                    target,
                    key
            );

            return new Element(getTranslationComponent(key), getTooltipComponent(key, range), widget);
        }

        return super.createDoubleValue(key, spec, source, target);
    }

    /**
     * 一个同时包含 Slider 和 EditBox 的组合控件
     */
    static class DoubleControlWidget extends AbstractWidget {
        private final MapConfigScreen mapConfigScreen;
        private final AbstractWidget slider;
        private final EditBox editBox;
        private final OptionInstance<Double> option;
        private final ModConfigSpec.Range<Double> range;
        private double currentValue;
        private boolean isSyncing = false; // 防止循环更新

        public DoubleControlWidget(MapConfigScreen mapConfigScreen, int x, int y, int width, int height, Component message,
                                   ModConfigSpec.Range<Double> range, Supplier<Double> source, Consumer<Double> target, String key) {
            super(x, y, width, height, message);
            this.mapConfigScreen = mapConfigScreen;
            this.range = range;

            double min = range.getMin();
            double max = range.getMax();

            // 初始化本地基准值
            this.currentValue = source.get();

            this.editBox = new EditBox(Minecraft.getInstance().font, x, y, width, height, message);
            this.editBox.setMaxLength(18);
            this.editBox.setValue(String.format("%.2f", currentValue));

            OptionInstance.SliderableValueSet<Double> doubleRange = new OptionInstance.SliderableValueSet<>() {
                @Override
                public double toSliderValue(@NotNull Double value) {
                    return (value - min) / (max - min);
                }

                @Override
                public @NotNull Double fromSliderValue(double sliderValue) {
                    return min + sliderValue * (max - min);
                }

                @Override
                public @NotNull Optional<Double> validateValue(@NotNull Double value) {
                    return (value >= min && value <= max) ? Optional.of(value) : Optional.empty();
                }

                @Override
                public @NotNull Codec<Double> codec() {
                    return Codec.DOUBLE;
                }
            };

            option = new OptionInstance<>(
                    mapConfigScreen.getTranslationKey(key),
                    OptionInstance.noTooltip(),
                    (caption, val) -> Component.empty(),
                    doubleRange,
                    this.currentValue, // 使用本地基准值
                    newValue -> {
                        // OptionInstance 回调 (通常由拖动滑块触发)
                        // 关键逻辑: 只有在非 Sync 状态且值确实改变时才执行
                        if (!isSyncing && newValue != this.currentValue) {
                            this.currentValue = newValue; // 更新基准

                            // 同步到 Config
                            updateValue(newValue, target, key, source.get()); // 这里 source.get() 仅作 undo 参考

                            // 同步到 EditBox (防止死循环)
                            this.isSyncing = true;
                            this.editBox.setValue(String.format("%.2f", newValue));
                            this.isSyncing = false;
                        }
                    }
            );

            this.slider = option.createButton(Minecraft.getInstance().options, x, y, width, option::set);

            this.editBox.setResponder(text -> {
                if (!isSyncing) {
                    try {
                        double val = Double.parseDouble(text);
                        // 关键逻辑: 只有在值范围合法且与当前基准不同步时才执行
                        if (range.test(val) && val != this.currentValue) {
                            this.currentValue = val; // 更新基准

                            // 同步到 Config
                            updateValue(val, target, key, source.get());

                            // 同步到 Slider
                            this.isSyncing = true;
                            option.set(val); // 这会触发上面的 callback，但 isSyncing 会拦截 EditBox 的更新
                            this.isSyncing = false;
                            this.editBox.setTextColor(0xE0E0E0);
                        }
                    } catch (NumberFormatException e) {
                        this.editBox.setTextColor(0xFF0000);
                    }
                }
            });
        }

        // 统一的更新逻辑（包含撤销支持）
        private void updateValue(Double newValue, Consumer<Double> target, String key, Double oldValue) {
            mapConfigScreen.undoManager.add(
                    v -> setValue(target, key, v), newValue,
                    v -> setValue(target, key, v), oldValue
            );
        }

        private void setValue(Consumer<Double> target, String key, Double v) {
            target.accept(v);
            mapConfigScreen.onChanged(key);
            isSyncing = true;
            if (v == (long) v.doubleValue()) {
                editBox.setValue(String.valueOf((long) v.doubleValue())); // 输出 "6"
            } else {
                editBox.setValue(String.valueOf(v));        // 输出 "6.5"
            }
            if (slider instanceof OptionInstance.OptionInstanceSliderButton<?> sliderButton) {
                sliderButton.setValue(v / (range.getMax() - range.getMin()));
            }
            isSyncing = false;
        }

        // --- 3. 布局与渲染代理 ---

        @Override
        public void setX(int x) {
            super.setX(x);
            layout();
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            layout();
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);
            layout();
        }

        @Override
        public void setHeight(int height) {
            super.setHeight(height);
            layout();
        }

        private void layout() {
            if (slider == null || editBox == null) return;

            int gap = 4;
            int editBoxWidth = 50; // 输入框固定宽度
            int sliderWidth = this.width - editBoxWidth - gap;

            // 左边放滑块
            slider.setX(this.getX());
            slider.setY(this.getY());
            slider.setWidth(sliderWidth);
            slider.setHeight(this.height);

            // 右边放输入框
            editBox.setX(this.getX() + sliderWidth + gap);
            editBox.setY(this.getY());
            editBox.setWidth(editBoxWidth);
            editBox.setHeight(this.height);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.slider.render(guiGraphics, mouseX, mouseY, partialTick);
            this.editBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // --- 4. 事件转发 (关键！) ---

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // 1. 优先尝试点击文本框
            if (editBox.mouseClicked(mouseX, mouseY, button)) {
                // 关键：如果文本框被点中，必须强制让滑块失去焦点
                slider.setFocused(false);
                return true;
            }

            // 2. 尝试点击滑块
            if (slider.mouseClicked(mouseX, mouseY, button)) {
                // 关键：如果滑块被点中，必须强制让文本框失去焦点
                editBox.setFocused(false);
                return true;
            }

            // 3. 如果都没点中，但点击还在本控件范围内（比如点到了两个控件中间的缝隙）
            // 应该清除所有子控件的焦点，以免出现“幽灵”焦点
            if (this.isMouseOver(mouseX, mouseY)) {
                editBox.setFocused(false);
                slider.setFocused(false);
                // 我们消费这个点击，让自己获得（空的）焦点，防止点击穿透
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            // 拖拽结束后的处理可以放在这里
            return editBox.mouseReleased(mouseX, mouseY, button) || slider.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (editBox.isMouseOver(mouseX, mouseY)) {return false;}
            return slider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return editBox.keyPressed(keyCode, scanCode, modifiers) || slider.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return editBox.charTyped(codePoint, modifiers) || slider.charTyped(codePoint, modifiers);
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (focused) {
                if (editBox.isHovered()) editBox.setFocused(true);
            } else {
                editBox.setFocused(false);
                slider.setFocused(false);
            }
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        }
    }
}
