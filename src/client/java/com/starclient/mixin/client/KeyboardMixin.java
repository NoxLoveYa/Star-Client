package com.starclient.mixin.client;

import com.starclient.utils.KeybindsHandler;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void handleKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {

        if (window != MinecraftClient.getInstance().getWindow().getHandle() || MinecraftClient.getInstance().currentScreen instanceof ChatScreen)
            return;

        KeybindsHandler.checkForAction(key, action);
    }
}