package zigqora.ricoshot.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import zigqora.ricoshot.RicoshotConfig;

public class RicoshotConfigScreen extends Screen {
    private final Screen parent;

    public RicoshotConfigScreen(Screen parent) {
        super(Text.literal("Ricoshot Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        // Toggle Action Bar Text
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Popups: " + (RicoshotConfig.instance.enableActionBarText ? "§aON" : "§cOFF")),
            button -> {
                RicoshotConfig.instance.enableActionBarText = !RicoshotConfig.instance.enableActionBarText;
                button.setMessage(Text.literal("Popups: " + (RicoshotConfig.instance.enableActionBarText ? "§aON" : "§cOFF")));
                RicoshotConfig.save();
            }
        ).dimensions(centerX - 155, y, 150, 20).build());

        // Toggle Explosion Sound
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("TNT Sound: " + (RicoshotConfig.instance.playExplosionSound ? "§aON" : "§cOFF")),
            button -> {
                RicoshotConfig.instance.playExplosionSound = !RicoshotConfig.instance.playExplosionSound;
                button.setMessage(Text.literal("TNT Sound: " + (RicoshotConfig.instance.playExplosionSound ? "§aON" : "§cOFF")));
                RicoshotConfig.save();
            }
        ).dimensions(centerX + 5, y, 150, 20).build());

        y += 30;

        // Targeting Radius Slider (1 to 100 blocks)
        double radiusValue = (RicoshotConfig.instance.targetingRadius - 1.0) / 99.0;
        this.addDrawableChild(new SliderWidget(centerX - 155, y, 310, 20, Text.literal(String.format("Coin Targeting Radius: %.1f Blocks", RicoshotConfig.instance.targetingRadius)), radiusValue) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(String.format("Coin Targeting Radius: %.1f Blocks", RicoshotConfig.instance.targetingRadius)));
            }
            @Override
            protected void applyValue() {
                RicoshotConfig.instance.targetingRadius = 1.0 + this.value * 99.0;
                RicoshotConfig.save();
            }
        });

        y += 30;

        // Heart Damage Slider (1.0 to 100.0 damage)
        double damageValue = (RicoshotConfig.instance.baseDamage - 1.0f) / 99.0f;
        this.addDrawableChild(new SliderWidget(centerX - 155, y, 310, 20, Text.literal(String.format("Base Heart Damage: %.1f HP", RicoshotConfig.instance.baseDamage)), damageValue) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(String.format("Base Heart Damage: %.1f HP", RicoshotConfig.instance.baseDamage)));
            }
            @Override
            protected void applyValue() {
                RicoshotConfig.instance.baseDamage = (float) (1.0 + this.value * 99.0);
                RicoshotConfig.save();
            }
        });

        y += 40;

        // Done button
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.DONE,
            button -> this.client.setScreen(this.parent)
        ).dimensions(centerX - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        // this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
