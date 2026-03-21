package com.starclient;

import com.mojang.blaze3d.platform.InputConstants;
import com.starclient.screen.StarClientMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class StarClientClient implements ClientModInitializer {
	private static final KeyMapping OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyMapping("key.starclient.open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
					KeyMapping.Category.MISC));

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_MENU_KEY.consumeClick()) {
				if (client.screen instanceof StarClientMenuScreen menuScreen) {
					menuScreen.onClose();
				} else {
					client.setScreen(new StarClientMenuScreen(client.screen));
				}
			}
		});
	}
}