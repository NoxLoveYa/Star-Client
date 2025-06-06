package com.starclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class StarClientClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		HudCheat.initialize();
	}
}