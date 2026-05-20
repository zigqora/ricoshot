package zigqora.ricoshot.mixin.client;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zigqora.ricoshot.RicoshotConfig;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addRicoshotToggleButton(CallbackInfo ci) {
        // 1. Locate the vanilla Options button by its translatable key "menu.options"
        ClickableWidget optionsButton = null;
        for (net.minecraft.client.gui.Element element : this.children()) {
            if (element instanceof ClickableWidget widget) {
                if (widget.getMessage() != null) {
                    String stringKey = "";
                    if (widget.getMessage().getContent() instanceof TranslatableTextContent translatable) {
                        stringKey = translatable.getKey();
                    }
                    if ("menu.options".equals(stringKey) || 
                        widget.getMessage().getString().toLowerCase().contains("options")) {
                        optionsButton = widget;
                        break;
                    }
                }
            }
        }

        // 2. Locate the accessibility button by geometry (20x20 button next to the Options button)
        ClickableWidget accessibilityButton = null;
        if (optionsButton != null) {
            for (net.minecraft.client.gui.Element element : this.children()) {
                if (element instanceof ClickableWidget widget) {
                    if (widget != optionsButton && 
                        widget.getWidth() == 20 && 
                        widget.getHeight() == 20 && 
                        Math.abs(widget.getY() - optionsButton.getY()) <= 2) {
                        accessibilityButton = widget;
                        break;
                    }
                }
            }
        }

        // 3. Determine positioning relative to options and accessibility button
        int x, y;
        if (optionsButton != null) {
            if (accessibilityButton != null) {
                // Place it to the left of the accessibility button (icon-row flanking options)
                x = accessibilityButton.getX() - 24;
                y = accessibilityButton.getY();
            } else {
                // Place it where the accessibility button would normally be (left of options)
                x = optionsButton.getX() - 24;
                y = optionsButton.getY();
            }
        } else {
            // Screen center fallback if options button isn't found
            x = this.width / 2 - 124;
            y = this.height / 4 + 104;
        }

        // 4. Create and add our Ricoshot toggle button
        int width = 20;
        int height = 20;

        ButtonWidget toggleButton = ButtonWidget.builder(
            Text.literal(RicoshotConfig.instance.enableActionBarText ? "§6🪙" : "§7🪙"),
            (button) -> {
                // Toggle config setting
                RicoshotConfig.instance.enableActionBarText = !RicoshotConfig.instance.enableActionBarText;
                RicoshotConfig.save();

                // Update button message and tooltip dynamically for instant, responsive user feedback!
                button.setMessage(Text.literal(RicoshotConfig.instance.enableActionBarText ? "§6🪙" : "§7🪙"));
                button.setTooltip(Tooltip.of(Text.literal(RicoshotConfig.instance.enableActionBarText ? "§6§lRicoshot Popups: §a§lENABLED" : "§7§lRicoshot Popups: §c§lDISABLED")));
            }
        )
        .dimensions(x, y, width, height)
        .tooltip(Tooltip.of(Text.literal(RicoshotConfig.instance.enableActionBarText ? "§6§lRicoshot Popups: §a§lENABLED" : "§7§lRicoshot Popups: §c§lDISABLED")))
        .build();

        this.addDrawableChild(toggleButton);
    }
}
