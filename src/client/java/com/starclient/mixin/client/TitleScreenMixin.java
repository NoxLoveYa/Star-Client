package com.starclient.mixin.client;

import com.starclient.gui.screen.CheatOptions; // Import your custom screen
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    // Constructor required by Mixin for type safety (you can call super() if needed)
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    // This injects our code *after* the target method returns
    @Inject(at = @At("RETURN"), method = "init()V")
    private void addCustomButton(CallbackInfo ci) {
        // Calculate the position for your new button
        // You can adjust these values to place it where you want
        int buttonX = this.width / 2 - 100; // Center the button horizontally
        int buttonY = this.height / 4 + 48 + 72 + 24 + 24; // Below the "Options" and "Quit" buttons, adjust as needed

        // Create your new button
        ButtonWidget customButton = ButtonWidget.builder(
                        Text.literal("Star Client"), // Translatable text for your button
                        button -> {
                            // This is what happens when the button is pressed
                            // It opens your custom screen, passing the current TitleScreen as the parent
                            this.client.setScreen(new CheatOptions(this));
                        }
                )
                .dimensions(buttonX, buttonY, 200, 20) // Set position and size
                .build();

        // Add the button to the screen's drawable children
        this.addDrawableChild(customButton);
    }
}