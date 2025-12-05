package me.isaiah.multiworld.gui;

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
    protected @Nullable Element createDoubleValue(String key, ModConfigSpec.ValueSpec spec, Supplier<Double> source, Consumer<Double> target) {
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
        private boolean isSyncing = false; // 防止循环更新

        public DoubleControlWidget(MapConfigScreen mapConfigScreen, int x, int y, int width, int height, Component message,
                                   ModConfigSpec.Range<Double> range, Supplier<Double> source, Consumer<Double> target, String key) {
            super(x, y, width, height, message);
            this.mapConfigScreen = mapConfigScreen;

            double min = range.getMin();
            double max = range.getMax();

            this.editBox = new EditBox(Minecraft.getInstance().font, x, y, width, height, message);
            this.editBox.setMaxLength(18);
            this.editBox.setValue(String.format("%.2f", source.get()));

            OptionInstance.SliderableValueSet<Double> doubleRange = new OptionInstance.SliderableValueSet<>() {
                @Override
                public double toSliderValue(Double value) {
                    return (value - min) / (max - min);
                }

                @Override
                public Double fromSliderValue(double sliderValue) {
                    return min + sliderValue * (max - min);
                }

                @Override
                public Optional<Double> validateValue(Double value) {
                    return (value >= min && value <= max) ? Optional.of(value) : Optional.empty();
                }

                @Override
                public com.mojang.serialization.Codec<Double> codec() {
                    return com.mojang.serialization.Codec.DOUBLE;
                }
            };

            OptionInstance<Double> option = new OptionInstance<>(
                    mapConfigScreen.getTranslationKey(key),
                    OptionInstance.noTooltip(), // Tooltip 交给外层 Element
                    (caption, val) -> Component.empty(), // 滑块上不显示文字，或者只显示简略信息
                    doubleRange,
                    source.get(),
                    newValue -> {
                        // Slider 改变 -> 更新 Config 和 EditBox
                        if (!isSyncing && !newValue.equals(source.get())) {
                            isSyncing = true;
                            updateValue(newValue, target, key, source.get());
                            editBox.setValue(String.format("%.2f", newValue)); // 同步文字
                            isSyncing = false;
                        }
                    }
            );
            this.slider = option.createButton(Minecraft.getInstance().options, x, y, width, option::set);

            this.editBox.setResponder(text -> {
                // EditBox 改变 -> 更新 Config 和 Slider
                if (!isSyncing) {
                    try {
                        double val = Double.parseDouble(text);
                        if (range.test(val) && !Double.valueOf(val).equals(source.get())) {
                            isSyncing = true;
                            updateValue(val, target, key, source.get());
                            option.set(val); // 同步滑块位置 (这会触发 Slider 的 callback，所以需要 isSyncing 锁)

                            // 重新获取 slider widget (因为 set 可能导致内部状态变化，不过通常 widget 引用不变)
                            // 这里主要是为了让 OptionInstance 内部刷新显示
                            isSyncing = false;
                            editBox.setTextColor(0xE0E0E0); // 正常颜色
                        } else {
                            // 值在范围外，但不报错，只是不应用
                        }
                    } catch (NumberFormatException e) {
                        editBox.setTextColor(0xFF0000); // 格式错误变红
                    }
                }
            });
        }

        // 统一的更新逻辑（包含撤销支持）
        private void updateValue(Double newValue, Consumer<Double> target, String key, Double oldValue) {
            mapConfigScreen.undoManager.add(v -> {
                target.accept(v);
                mapConfigScreen.onChanged(key);
                // 撤销时也要刷新 UI
                isSyncing = true;
                editBox.setValue(String.format("%.2f", v));
                // slider.set(v) 比较难直接调，因为 slider 是 widget，但数据源 source.get() 会由父类重绘时处理
                isSyncing = false;
            }, newValue, v -> {
                target.accept(v);
                mapConfigScreen.onChanged(key);
                isSyncing = true;
                editBox.setValue(String.format("%.2f", v));
                isSyncing = false;
            }, oldValue);
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
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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
