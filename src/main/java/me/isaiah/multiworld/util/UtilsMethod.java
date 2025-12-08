package me.isaiah.multiworld.util;

import net.minecraft.client.gui.components.EditBox;

public class UtilsMethod {
    // 解析并验证颜色字符串，返回 ARGB 整数（alpha=0xFF）或返回 null 表示无效
    public static Integer parseColorHexToArgb(String text) {
        if (text == null) return null;
        String s = text.trim();
        if (s.isEmpty()) return null;
        if (s.startsWith("#")) s = s.substring(1);
        if (s.length() != 6) return null;
        try {
            int rgb = Integer.parseInt(s, 16) & 0xFFFFFF;
            return rgb | 0xFF000000; // 确保 alpha 为 255
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void validateNumericField(EditBox field, String text) {
        try {
            Double.parseDouble(text);
            field.setTextColor(0xE0E0E0); // Default color
        } catch (NumberFormatException e) {
            field.setTextColor(0xFF5555); // Red color for error
        }
    }

    // Used for responder validation, sets text color
    public static void validateColorField(EditBox field, String text) {
        Integer color = parseColorHexToArgb(text);
        if (color != null) {
            field.setTextColor(0xE0E0E0);
        } else {
            field.setTextColor(0xFF5555);
        }
    }

    // Convenience method used in render
    public static boolean validateColorField(EditBox field) {
        return parseColorHexToArgb(field.getValue()) != null;
    }
}
