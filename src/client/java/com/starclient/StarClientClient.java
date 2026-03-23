package com.starclient;

import com.mojang.blaze3d.platform.InputConstants;
import com.starclient.render.LocalPlayerStarAuraRenderer;
import com.starclient.render.MobChamsRenderer;
import com.starclient.render.StarClientWatermarkRenderer;
import com.starclient.render.StarClientInfoBoxRenderer;
import com.starclient.screen.StarClientMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
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
	@SuppressWarnings("deprecation")
	public void onInitializeClient() {
		MobChamsRenderer.getInstance().initialize();
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			Minecraft client = Minecraft.getInstance();
			if (client.screen == null) {
				if (StarClientOptions.showWatermark) {
					StarClientWatermarkRenderer.render(context);
				}
				if (StarClientOptions.showInfoBox) {
					StarClientInfoBoxRenderer.render(context);
				}
			}
		});
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenEvents.afterRender(screen)
					.register((currentScreen, context, mouseX, mouseY, tickDelta) -> {
						if (StarClientOptions.showWatermark) {
							StarClientWatermarkRenderer.render(context);
						}
						if (StarClientOptions.showInfoBox) {
							StarClientInfoBoxRenderer.render(context);
						}
					});

			ScreenMouseEvents.allowMouseClick(screen).register((currentScreen, event) -> {
				if (event.button() == 0) {
					boolean handled = false;
					if (StarClientWatermarkRenderer.beginDragging(event.x(), event.y())) {
						handled = true;
					}
					if (StarClientInfoBoxRenderer.beginDragging(event.x(), event.y())) {
						handled = true;
					}
					if (handled)
						return false;
				}
				return true;
			});

			ScreenMouseEvents.allowMouseDrag(screen)
					.register((currentScreen, event, horizontalAmount, verticalAmount) -> {
						if (event.button() == 0) {
							boolean handled = false;
							if (StarClientWatermarkRenderer.isDragging()) {
								StarClientWatermarkRenderer.dragTo(event.x(), event.y());
								handled = true;
							}
							if (StarClientInfoBoxRenderer.isDragging()) {
								StarClientInfoBoxRenderer.dragTo(event.x(), event.y());
								handled = true;
							}
							if (handled)
								return false;
						}
						return true;
					});

			ScreenMouseEvents.allowMouseRelease(screen).register((currentScreen, event) -> {
				if (event.button() == 0) {
					boolean handled = false;
					if (StarClientWatermarkRenderer.isDragging()) {
						StarClientWatermarkRenderer.endDragging();
						handled = true;
					}
					if (StarClientInfoBoxRenderer.isDragging()) {
						StarClientInfoBoxRenderer.endDragging();
						handled = true;
					}
					if (handled)
						return false;
				}
				return true;
			});

			ScreenEvents.remove(screen).register(removedScreen -> StarClientWatermarkRenderer.endDragging());
			ScreenEvents.remove(screen).register(removedScreen -> StarClientInfoBoxRenderer.endDragging());
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			LocalPlayerStarAuraRenderer.tick(client);

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