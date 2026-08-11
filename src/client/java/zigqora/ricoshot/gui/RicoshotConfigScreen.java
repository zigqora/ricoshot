package zigqora.ricoshot.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import zigqora.ricoshot.RicoshotConfig;

public class RicoshotConfigScreen extends Screen {
    private final Screen parent;

    public RicoshotConfigScreen(Screen parent) {
        super(Component.literal("Ricoshot Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        // action bar toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Popups: " + (RicoshotConfig.instance.enableActionBarText ? "§aON" : "§cOFF")),
            button -> {
                RicoshotConfig.instance.enableActionBarText = !RicoshotConfig.instance.enableActionBarText;
                button.setMessage(Component.literal("Popups: " + (RicoshotConfig.instance.enableActionBarText ? "§aON" : "§cOFF")));
                RicoshotConfig.save();
            }
        ).bounds(centerX - 155, y, 150, 20).build());

        // explosion toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("TNT Sound: " + (RicoshotConfig.instance.playExplosionSound ? "§aON" : "§cOFF")),
            button -> {
                RicoshotConfig.instance.playExplosionSound = !RicoshotConfig.instance.playExplosionSound;
                button.setMessage(Component.literal("TNT Sound: " + (RicoshotConfig.instance.playExplosionSound ? "§aON" : "§cOFF")));
                RicoshotConfig.save();
            }
        ).bounds(centerX + 5, y, 150, 20).build());

        y += 30;

        // radius slider
        double radiusValue = (RicoshotConfig.instance.targetingRadius - 1.0) / 99.0;
        this.addRenderableWidget(new AbstractSliderButton(centerX - 155, y, 310, 20, Component.literal(String.format("Coin Targeting Radius: %.1f Blocks", RicoshotConfig.instance.targetingRadius)), radiusValue) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.format("Coin Targeting Radius: %.1f Blocks", RicoshotConfig.instance.targetingRadius)));
            }
            @Override
            protected void applyValue() {
                RicoshotConfig.instance.targetingRadius = 1.0 + this.value * 99.0;
                RicoshotConfig.save();
            }
        });

        y += 30;

        // damage slider
        double damageValue = (RicoshotConfig.instance.baseDamage - 1.0f) / 99.0f;
        this.addRenderableWidget(new AbstractSliderButton(centerX - 155, y, 310, 20, Component.literal(String.format("Base Heart Damage: %.1f HP", RicoshotConfig.instance.baseDamage)), damageValue) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.format("Base Heart Damage: %.1f HP", RicoshotConfig.instance.baseDamage)));
            }
            @Override
            protected void applyValue() {
                RicoshotConfig.instance.baseDamage = (float) (1.0 + this.value * 99.0);
                RicoshotConfig.save();
            }
        });

        y += 40;

        // Done button
        this.addRenderableWidget(Button.builder(
            CommonComponents.GUI_DONE,
            button -> this.minecraft.setScreen(this.parent)
        ).bounds(centerX - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float delta) {
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, delta);
    }
}
