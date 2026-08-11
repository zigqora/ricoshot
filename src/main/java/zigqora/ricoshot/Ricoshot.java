package zigqora.ricoshot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ricoshot implements ModInitializer {
	public static final String MOD_ID = "ricoshot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ResourceKey<EntityType<?>> FLYING_NUGGET_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "flying_nugget"));

	// Entity registration
	public static final EntityType<FlyingNuggetEntity> FLYING_NUGGET_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE, FLYING_NUGGET_KEY,
			EntityType.Builder.<FlyingNuggetEntity>of(FlyingNuggetEntity::new, MobCategory.MISC)
					.sized(0.25f, 0.25f)
					.clientTrackingRange(4).updateInterval(10).build(FLYING_NUGGET_KEY));

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Ricoshot Mod under the MIT License!");
		RicoshotConfig.load();

		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);
			ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
			if (offHandStack.is(Items.GOLD_NUGGET) && mainHandStack.is(Items.BOW)) {
				if (!player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) {
					if (!world.isClientSide()) {
						tossNugget(player, world);
					}
					return hand == InteractionHand.OFF_HAND ? 
							net.minecraft.world.InteractionResult.SUCCESS : 
							net.minecraft.world.InteractionResult.PASS;
				}
			}
			return net.minecraft.world.InteractionResult.PASS;
		});
	}

	private static void tossNugget(Player player, Level world) {
		FlyingNuggetEntity flyingNugget = new FlyingNuggetEntity(world, player);
		
		flyingNugget.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
		
		Vec3 look = player.getViewVector(1.0F);
		
		Vec3 velocity = new Vec3(
				look.x * 0.45,
				0.55,
				look.z * 0.45
		).add(player.getDeltaMovement().scale(0.8));
		
		flyingNugget.setDeltaMovement(velocity);
		world.addFreshEntity(flyingNugget);

		world.playSound(
				null,
				player.getX(), player.getY(), player.getZ(),
				SoundEvents.EXPERIENCE_ORB_PICKUP,
				SoundSource.PLAYERS,
				0.8F,
				1.6F
		);

		player.getCooldowns().addCooldown(player.getItemInHand(InteractionHand.OFF_HAND), 20);

		if (!player.getAbilities().instabuild) {
			player.getItemInHand(InteractionHand.OFF_HAND).shrink(1);
		}
	}
}