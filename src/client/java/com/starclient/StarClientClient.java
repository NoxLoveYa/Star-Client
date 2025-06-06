package com.starclient;

import com.starclient.render.HudCheat;
import net.fabricmc.api.ClientModInitializer;

public class StarClientClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		HudCheat.initialize();
	}
}