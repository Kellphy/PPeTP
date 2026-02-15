package nl.theepicblock.ppetp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PPeTPCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("ppetp")
                .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                .then(CommandManager.literal("enable")
                    .then(CommandManager.argument("type", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE))
                        .executes(PPeTPCommand::enableType)))
                .then(CommandManager.literal("disable")
                    .then(CommandManager.argument("type", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE))
                        .executes(PPeTPCommand::disableType)))
                .then(CommandManager.literal("list")
                    .executes(PPeTPCommand::listTypes))
        );
    }

    private static int enableType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var entry = RegistryEntryReferenceArgumentType.getEntityType(context, "type");
        var entityType = entry.value();
        var id = Registries.ENTITY_TYPE.getId(entityType);
        var config = TeleportConfig.get(context.getSource().getServer().getOverworld());
        if (config.enable(id)) {
            context.getSource().sendFeedback(() -> Text.literal("Enabled PPeTP for " + id), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("PPeTP was already enabled for " + id), false);
        }
        return 1;
    }

    private static int disableType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var entry = RegistryEntryReferenceArgumentType.getEntityType(context, "type");
        var entityType = entry.value();
        var id = Registries.ENTITY_TYPE.getId(entityType);
        var config = TeleportConfig.get(context.getSource().getServer().getOverworld());
        if (config.disable(id)) {
            context.getSource().sendFeedback(() -> Text.literal("Disabled PPeTP for " + id), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("PPeTP was already disabled for " + id), false);
        }
        return 1;
    }

    private static int listTypes(CommandContext<ServerCommandSource> context) {
        var config = TeleportConfig.get(context.getSource().getServer().getOverworld());
        var enabled = config.getEnabledTypes();
        if (enabled.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("PPeTP: No entity types enabled"), false);
        } else {
            var list = String.join(", ", enabled.stream().map(Object::toString).sorted().toList());
            context.getSource().sendFeedback(() -> Text.literal("PPeTP enabled for: " + list), false);
        }
        return 1;
    }
}
