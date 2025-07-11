package com.starclient.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT) // Important: This screen only exists on the client
public class CheatOptions extends Screen {

    public static Object Visual;
    protected Screen parent;
    // The parent screen (TitleScreen in this case), to return to

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
            com.starclient.utils.CheatOptions.GlowEnabled = !com.starclient.utils.CheatOptions.GlowEnabled;
        }).dimensions(40, 40, 120, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Toggle Watermark"), (btn) -> {
            com.starclient.utils.CheatOptions.WatermarkEnabled = !com.starclient.utils.CheatOptions.WatermarkEnabled;
        }).dimensions(this.width - 160, 40, 120, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Toggle Name ESP"), (btn) -> {
            com.starclient.utils.CheatOptions.NameEnabled = !com.starclient.utils.CheatOptions.NameEnabled;
        }).dimensions(40, 64, 120, 20).build());

        //this.addDrawableChild(new PlayerSkinWidget(55, 85, PlayerUtils.getAllLoadedEntityModels(), PlayerUtils.getLocalPlayerSkinIdentifier())).setPosition(40, this.height / 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Welcome to my custom mod screen!"), this.width / 2, this.height / 2 - 20, 0xFFAAAA);
    }
}
