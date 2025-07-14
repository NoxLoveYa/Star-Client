package com.starclient.gui.screen;

import com.starclient.gui.widgets.ButtonGroupWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import com.starclient.gui.widgets.ButtonWidget;
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

        {
            //Visuals group
            ButtonGroupWidget combatGroup = ButtonGroupWidget.builder(Text.of("Combat")).dimensions(40, 40, 90, 15).build();

            //Visuals Buttons
            ButtonWidget autoClickerButton = ButtonWidget.builder(Text.of("AutoClicker"), (btn) -> {
                com.starclient.utils.CheatOptions.GlowEnabled = !com.starclient.utils.CheatOptions.GlowEnabled;
            }).size(120, 15).build();

            combatGroup.appendButtonWidget(autoClickerButton);

            this.addDrawableChild(combatGroup);
            this.addDrawableChild(autoClickerButton);
        }

        {
            //Visuals group
            ButtonGroupWidget visualsGroup = ButtonGroupWidget.builder(Text.of("Visuals")).dimensions(165, 40, 90, 15).build();

            //Visuals Buttons
            ButtonWidget glowButton = ButtonWidget.builder(Text.of("Glow ESP"), (btn) -> {
                com.starclient.utils.CheatOptions.GlowEnabled = !com.starclient.utils.CheatOptions.GlowEnabled;
            }).size(120, 15).build();

            ButtonWidget nameEspButton = ButtonWidget.builder(Text.of("Name ESP"), (btn) -> {
                com.starclient.utils.CheatOptions.NameEnabled = !com.starclient.utils.CheatOptions.NameEnabled;
            }).size(120, 15).build();

            visualsGroup.appendButtonWidget(glowButton);
            visualsGroup.appendButtonWidget(nameEspButton);

            this.addDrawableChild(visualsGroup);
            this.addDrawableChild(glowButton);
            this.addDrawableChild(nameEspButton);
        }

        {
            //Misc group
            ButtonGroupWidget miscGroup = ButtonGroupWidget.builder(Text.of("Misc")).dimensions(290, 40, 90, 15).build();

            //Misc Buttons
            ButtonWidget watermarkButton = ButtonWidget.builder(Text.of("Watermark"), (btn) -> {
                com.starclient.utils.CheatOptions.WatermarkEnabled = !com.starclient.utils.CheatOptions.WatermarkEnabled;
            }).size(120, 15).build();

            miscGroup.appendButtonWidget(watermarkButton);

            this.addDrawableChild(miscGroup);
            this.addDrawableChild(watermarkButton);
        }
        //this.addDrawableChild(new PlayerSkinWidget(55, 85, PlayerUtils.getAllLoadedEntityModels(), PlayerUtils.getLocalPlayerSkinIdentifier())).setPosition(40, this.height / 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
