package zigqora.ricoshot.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import zigqora.ricoshot.Ricoshot;

public class RicoshotClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register Ricoshot gold nugget flying entity renderer on the client
		EntityRendererRegistry.register(
				Ricoshot.FLYING_NUGGET_ENTITY_TYPE,
				FlyingItemEntityRenderer::new
		);
	}
}