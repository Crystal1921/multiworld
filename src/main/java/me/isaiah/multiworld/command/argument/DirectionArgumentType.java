package me.isaiah.multiworld.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class DirectionArgumentType implements ArgumentType<Direction> {
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((component) -> Component.translatableEscape("argument.color.invalid", component));

    public static DirectionArgumentType direction() {
        return new DirectionArgumentType();
    }

    public static Direction getColor(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Direction.class);
    }

    @Override
    public Direction parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString();
        Direction name = Direction.byName(s);
        if (name != null) {
            return name;
        } else {
            throw ERROR_INVALID_VALUE.createWithContext(reader, s);
        }
    }
}
