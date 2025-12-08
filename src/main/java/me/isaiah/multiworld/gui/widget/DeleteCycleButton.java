package me.isaiah.multiworld.gui.widget; // 你的包名

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class DeleteCycleButton<T> extends AbstractButton {

    public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
    private final Component name;
    private final ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<DeleteCycleButton<T>, MutableComponent> narrationProvider;
    private final OnValueChange<T> onValueChange;
    private final boolean displayOnlyValue;
    private final OptionInstance.TooltipSupplier<T> tooltipSupplier;
    private int index;
    @Getter
    private T value;

    // 2. 将构造函数改为 public
    public DeleteCycleButton(
            int x, int y, int width, int height,
            Component message, Component name, int index, T value,
            ValueListSupplier<T> values,
            Function<T, Component> valueStringifier,
            Function<DeleteCycleButton<T>, MutableComponent> narrationProvider,
            OnValueChange<T> onValueChange,
            OptionInstance.TooltipSupplier<T> tooltipSupplier,
            boolean displayOnlyValue
    ) {
        super(x, y, width, height, message);
        this.name = name;
        this.index = index;
        this.value = value;
        this.values = values;
        this.valueStringifier = valueStringifier;
        this.narrationProvider = narrationProvider;
        this.onValueChange = onValueChange;
        this.displayOnlyValue = displayOnlyValue;
        this.tooltipSupplier = tooltipSupplier;
        this.updateTooltip();
    }

    public static <T> Builder<T> builder(Function<T, Component> valueStringifier) {
        return new Builder<>(valueStringifier);
    }

    // --- 3. 核心：自定义渲染逻辑 ---
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 在这里写你的自定义渲染逻辑
        // 比如绘制自定义纹理、边框、背景等

        // 示例：绘制一个简单的矩形背景（实际开发请使用 texture）
        int color = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000;
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, color);

        // 绘制文字
        int textColor = this.active ? 16777215 : 10526880;
        guiGraphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,
                this.getMessage(),
                this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2,
                textColor);
    }

    // --- 复制的业务逻辑 (必须保留以维持功能) ---
    private void updateTooltip() {
        this.setTooltip(this.tooltipSupplier.apply(this.value));
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }
    }

    private void cycleValue(int delta) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + delta, list.size());
        T t = list.get(this.index);
        this.updateValue(t);
        this.onValueChange.onValueChange(this, t);
    }

    private T getCycledValue(int delta) {
        List<T> list = this.values.getSelectedList();
        return list.get(Mth.positiveModulo(this.index + delta, list.size()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0) {
            this.cycleValue(-1);
        } else if (scrollY < 0.0) {
            this.cycleValue(1);
        }
        return true;
    }

    public void setValue(T value) {
        List<T> list = this.values.getSelectedList();
        int i = list.indexOf(value);
        if (i != -1) {
            this.index = i;
        }
        this.updateValue(value);
    }

    private void updateValue(T value) {
        Component component = this.createLabelForValue(value);
        this.setMessage(component);
        this.value = value;
        this.updateTooltip();
    }

    private Component createLabelForValue(T value) {
        return this.displayOnlyValue ? this.valueStringifier.apply(value) : this.createFullName(value);
    }

    private MutableComponent createFullName(T value) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(value));
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            T t = this.getCycledValue(1);
            Component component = this.createLabelForValue(t);
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", component));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", component));
            }
        }
    }

    public MutableComponent createDefaultNarrationMessage() {
        return Component.translatable("gui.narrate.button", this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage());
    }

    public interface OnValueChange<T> {
        void onValueChange(DeleteCycleButton<T> cycleButton, T value);
    }

    public interface ValueListSupplier<T> {
        static <T> ValueListSupplier<T> create(Collection<T> values) {
            final List<T> list = ImmutableList.copyOf(values);
            return new ValueListSupplier<>() {
                @Override
                public List<T> getSelectedList() {
                    return list;
                }

                @Override
                public List<T> getDefaultList() {
                    return list;
                }
            };
        }

        List<T> getSelectedList();

        List<T> getDefaultList();
    }

    public static class Builder<T> {
        private final Function<T, Component> valueStringifier;
        // ... 复制 Builder 内部的字段 ...
        private int initialIndex;
        @Nullable
        private T initialValue;
        private OptionInstance.TooltipSupplier<T> tooltipSupplier = tooltip -> null;
        private Function<DeleteCycleButton<T>, MutableComponent> narrationProvider = (c) -> c.createDefaultNarrationMessage(); // 注意泛型变化
        private ValueListSupplier<T> values = ValueListSupplier.create(ImmutableList.of());
        private boolean displayOnlyValue;

        public Builder(Function<T, Component> valueStringifier) {
            this.valueStringifier = valueStringifier;
        }

        public static DeleteCycleButton.Builder<Boolean> booleanBuilder(Component componentOn, Component componentOff) {
            return new DeleteCycleButton.Builder<Boolean>(aBoolean -> aBoolean ? componentOn : componentOff).withValues(BOOLEAN_OPTIONS);
        }

        public Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public Builder<T> withValues(Collection<T> values) {
            return this.withValues(ValueListSupplier.create(values));
        }

        @SafeVarargs
        public final Builder<T> withValues(T... values) {
            return this.withValues(ImmutableList.copyOf(values));
        }

        public Builder<T> withValues(ValueListSupplier<T> values) {
            this.values = values;
            return this;
        }

        public Builder<T> withInitialValue(T initialValue) {
            this.initialValue = initialValue;
            int i = this.values.getDefaultList().indexOf(initialValue);
            if (i != -1) {
                this.initialIndex = i;
            }
            return this;
        }

        public DeleteCycleButton<T> create(int x, int y, int width, int height, Component name, OnValueChange<T> onValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T t = this.initialValue != null ? this.initialValue : list.get(this.initialIndex);
                Component component = this.valueStringifier.apply(t);
                Component component1 = this.displayOnlyValue ? component : CommonComponents.optionNameValue(name, component);

                // 返回你的自定义类
                return new DeleteCycleButton<>(
                        x, y, width, height, component1, name, this.initialIndex, t,
                        this.values, this.valueStringifier, this.narrationProvider,
                        onValueChange, this.tooltipSupplier, this.displayOnlyValue
                );
            }
        }
    }
}