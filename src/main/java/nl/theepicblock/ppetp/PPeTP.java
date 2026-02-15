package nl.theepicblock.ppetp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PPeTP implements ModInitializer {
	public static final String MOD_ID = "proper-pet-tp";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final GameRule<Boolean> SHOULD_TP_CROSS_DIMENSIONAL = GameRuleBuilder
            .forBoolean(false)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(Identifier.of(MOD_ID, "pet_teleport_cross_dimension"));

	/**
	 * Checks if teleportation is enabled for the given entity type
	 * using the persistent per-world config (managed via /ppetp command).
	 */
	public static boolean isTeleportEnabled(MinecraftServer server, EntityType<?> type) {
		var config = TeleportConfig.get(server.getOverworld());
		return config.isEnabled(type);
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PPeTPCommand.register(dispatcher, registryAccess);
		});
	}
}