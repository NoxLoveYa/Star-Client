package com.starclient.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.starclient.gui.screen.CheatOptions; // Import your custom screen
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    // Mixins require a constructor matching a super constructor, or one that matches the target.
    // We'll use the simplest available one, usually a Screen constructor if it's the superclass.
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    // Shadow the private initWidgets method to inject into it
    @Shadow
    protected abstract void initWidgets();

    // Inject into the end of the initWidgets method
    // We need to capture the 'adder' local variable to add our button to the grid.
    @Inject(
            method = "initWidgets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/GridWidget;forEachChild(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.BEFORE // Inject before children are added to the screen
            )
            // Capture local variables needed for injection
    )
    private void addCustomButtonToGameMenu(CallbackInfo ci, @Local GridWidget.Adder adder) {
        // Create your new button
        ButtonWidget customButton = ButtonWidget.builder(
                        Text.of("Star Client Options"), // Use your existing translation key
                        button -> {
                            // This is what happens when the button is pressed
                            // It opens your custom screen, passing the current GameMenuScreen as the parent
                            this.client.setScreen(new CheatOptions(this));
                        }
                )
                .dimensions(this.width / 2 - 100, this.height / 2 - 52, 200, 20) // Match the width of other normal buttons in GameMenuScreen
                .build();

        // Add the button to the grid using the 'adder'
        adder.add(customButton);
    }
}