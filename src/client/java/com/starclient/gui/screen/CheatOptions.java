package com.starclient.gui.screen;

import com.starclient.utils.PlayerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT) // Important: This screen only exists on the client
public class CheatOptions extends Screen {

    public static Object Visual;
    // The parent screen (TitleScreen in this case), to return to
    private final Screen parent;

    public CheatOptions(Screen parent) {
        super(Text.literal("Star Client Options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> this.client.setScreen(this.parent))
                        .dimensions(this.width / 2 - 100, this.height - 20 - 4, 200, 20) // Position at the bottom center
                        .build()
        );

        this.addDrawableChild(ButtonWidget.builder(Text.of("Toggle Glow"), (btn) -> {
            com.starclient.CheatOptions.GlowEnabled = !com.starclient.CheatOptions.GlowEnabled;
            // When the button is clicked, we can display a toast to the screen.
            assert this.client != null;
            if (com.starclient.CheatOptions.GlowEnabled) {
                this.client.getToastManager().add(
                        SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Glow Cheat"), Text.of("Enabled"))
                );
            } else {
                this.client.getToastManager().add(
                        SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Glow Cheat"), Text.of("Disabled"))
                );
            }
        }).dimensions(40, 40, 120, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Toggle Watermark"), (btn) -> {
            com.starclient.CheatOptions.WatermarkEnabled = !com.starclient.CheatOptions.WatermarkEnabled;
        }).dimensions(this.width - 160, 40, 120, 20).build());

        this.addDrawableChild(new PlayerSkinWidget(55, 85, PlayerUtils.getAllLoadedEntityModels(), PlayerUtils.getLocalPlayerSkinIdentifier())).setPosition(40, this.height / 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Draw your screen title in the center top
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        // You can draw more custom elements here
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Welcome to my custom mod screen!"), this.width / 2, this.height / 2 - 20, 0xFFAAAA);
    }
}
