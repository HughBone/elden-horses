package com.hughbone.eldenhorses;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EldenHorsesClient implements ClientModInitializer {

    private static boolean summonCooldown = false;

    @Override
    public void onInitializeClient() {
        createKeybinds();
    }

    private void createKeybinds() {
        // Summon Horse Keybind
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eldenhorses.summonhorse",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                "eldenhorses.category" // Translation key of category.
        )) ;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!summonCooldown && keyBinding.isPressed()) {
                if (client.player.hasVehicle()) {
                    client.player.sendMessage(Text.of("Dismount to summon!"), true);
                    return;
                }
                // Summon steed
                Identifier identifier = new Identifier("elden_horses");
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString("summon");
                try {
                    ClientPlayNetworking.send(identifier, buf);
                } catch (IllegalStateException e) {
                    client.player.sendMessage(Text.of("Failed: Mod Not On Server?"), true);
                    e.printStackTrace();
                }

                // One second cooldown
                (new Thread(() -> {
                    summonCooldown = true;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                    summonCooldown = false;
                })).start();

            }
        });
    }



}
