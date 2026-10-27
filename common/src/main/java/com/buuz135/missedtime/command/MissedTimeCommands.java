package com.buuz135.missedtime.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.buuz135.missedtime.world.RecoveryManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class MissedTimeCommands {
    private MissedTimeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("missedtime")
                .requires(source -> source.getServer().isSingleplayer())
                .then(Commands.literal("accept").executes(context -> RecoveryManager.accept(context.getSource().getPlayerOrException())))
                .then(Commands.literal("decline").executes(context -> RecoveryManager.decline(context.getSource().getPlayerOrException())))
                .then(Commands.literal("pause").executes(context -> RecoveryManager.togglePause(context.getSource().getPlayerOrException())))
                .then(Commands.literal("faster").executes(context -> RecoveryManager.faster(context.getSource().getPlayerOrException())))
                .then(Commands.literal("slower").executes(context -> RecoveryManager.slower(context.getSource().getPlayerOrException())))
                .then(Commands.literal("stop").executes(context -> RecoveryManager.stop(context.getSource().getPlayerOrException())))
                .then(Commands.literal("simulate")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("minutes", IntegerArgumentType.integer(1))
                                .executes(context -> RecoveryManager.simulate(
                                        context.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(context, "minutes")))))
                .then(Commands.literal("status").executes(context -> RecoveryManager.status(context.getSource().getPlayerOrException()))));
    }
}
