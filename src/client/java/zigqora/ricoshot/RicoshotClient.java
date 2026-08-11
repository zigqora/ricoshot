package zigqora.ricoshot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class RicoshotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Ricoshot.FLYING_NUGGET_ENTITY_TYPE, ThrownItemRenderer::new);
    }
}
