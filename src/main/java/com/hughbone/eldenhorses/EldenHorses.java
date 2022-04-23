package com.hughbone.eldenhorses;

import com.hughbone.eldenhorses.interfaces.ServerPlayerExt;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EldenHorses implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("elden_horses");
	public static final EldenHorseArmor NETHERITE_HORSE_ARMOR = new EldenHorseArmor(15,"netherite",new FabricItemSettings().maxCount(1).group(ItemGroup.MISC).fireproof());

	@Override
	public void onInitialize() {
		LOGGER.info("Elden Horses Initialized.");
		Registry.register(Registry.ITEM, new Identifier("elden_horses", "netherite_horse_armor"), NETHERITE_HORSE_ARMOR);

		Identifier identifier = new Identifier("elden_horses");
		ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buf, responseSender) -> {
			if (buf.readString().equals("summon")) {
				((ServerPlayerExt) player).summonHorse(true);
			}
		});
	}

}
