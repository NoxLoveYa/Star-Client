package com.starclient;

import com.mojang.blaze3d.platform.InputConstants;
import com.starclient.render.MobChamsRenderer;
import com.starclient.screen.StarClientMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class StarClientClient implements ClientModInitializer {
	private static final KeyMapping OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyMapping("key.starclient.open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
					KeyMapping.Category.MISC));
	private boolean wasOpenMenuKeyDown = false;
	private long lastRainbowTickNanos = 0L;

	@Override
	public void onInitializeClient() {
		MobChamsRenderer.getInstance().initialize();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			long now = System.nanoTime();
			if (lastRainbowTickNanos != 0L) {
				double elapsedSeconds = (now - lastRainbowTickNanos) / 1_000_000_000.0;
				StarClientOptions.tickRainbowHues(elapsedSeconds);
			}
			lastRainbowTickNanos = now;

			boolean isDown = OPEN_MENU_KEY.isDown()
					|| org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().handle(),
							GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

			if (isDown && !wasOpenMenuKeyDown) {
				toggleMenu(client);
			}

			wasOpenMenuKeyDown = isDown;
		});
	}

	private static void toggleMenu(Minecraft client) {
		if (client.screen instanceof StarClientMenuScreen menuScreen) {
			menuScreen.onClose();
		} else {
			client.setScreen(new StarClientMenuScreen(client.screen));
		}
	}
}