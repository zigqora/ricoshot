package zigqora.ricoshot;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ricoshot implements ModInitializer {
	public static final String MOD_ID = "ricoshot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Entity registration — 1.21.4+: vanilla EntityType.Builder (FabricEntityTypeBuilder removed in 1.21.5)
	private static final RegistryKey<EntityType<?>> FLYING_NUGGET_KEY =
			RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "flying_nugget"));

	public static final EntityType<FlyingNuggetEntity> FLYING_NUGGET_ENTITY_TYPE = Registry.register(
			Registries.ENTITY_TYPE,
			FLYING_NUGGET_KEY,
			EntityType.Builder.<FlyingNuggetEntity>create(FlyingNuggetEntity::new, SpawnGroup.MISC)
					.dimensions(0.25F, 0.25F)
					.maxTrackingRange(4)
					.trackingTickInterval(10)
					//? if >=1.21.4 {
					.build(FLYING_NUGGET_KEY));
					//?} else {
					/*.build());
					*///?}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Ricoshot Mod under the MIT License!");
		RicoshotConfig.load();

		// Ultrakill Coin Toss mechanic: Right click with a Golden Nugget in off-hand tosses it ONLY if main-hand is holding a Bow!
		net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
			net.minecraft.item.ItemStack offHandStack = player.getStackInHand(net.minecraft.util.Hand.OFF_HAND);
			net.minecraft.item.ItemStack mainHandStack = player.getStackInHand(net.minecraft.util.Hand.MAIN_HAND);
			if (offHandStack.isOf(net.minecraft.item.Items.GOLD_NUGGET) && mainHandStack.isOf(net.minecraft.item.Items.BOW)) {
				//? if >=1.21.4 {
				if (!player.getItemCooldownManager().isCoolingDown(offHandStack)) {
				//?} else {
				/*if (!player.getItemCooldownManager().isCoolingDown(offHandStack.getItem())) {
				*///?}
					if (!world.isClient()) {
						tossNugget(player, world);
					}
					// Return success if using the off-hand directly, otherwise pass to allow main-hand actions (like Bow use)
					//? if >=1.21.4 {
					return hand == net.minecraft.util.Hand.OFF_HAND ? 
							net.minecraft.util.ActionResult.SUCCESS : 
							net.minecraft.util.ActionResult.PASS;
					//?} else {
					/*return hand == net.minecraft.util.Hand.OFF_HAND ? 
							net.minecraft.util.TypedActionResult.success(player.getStackInHand(hand)) : 
							net.minecraft.util.TypedActionResult.pass(player.getStackInHand(hand));
					*///?}
				}
			}
			//? if >=1.21.4 {
			return net.minecraft.util.ActionResult.PASS;
			//?} else {
			/*return net.minecraft.util.TypedActionResult.pass(player.getStackInHand(hand));
			*///?}
		});
	}

	private static void tossNugget(net.minecraft.entity.player.PlayerEntity player, net.minecraft.world.World world) {
		FlyingNuggetEntity flyingNugget = new FlyingNuggetEntity(world, player);
		
		// Set position slightly above player's eyes
		flyingNugget.setPosition(player.getX(), player.getEyeY() - 0.1, player.getZ());
		
		// Calculate look vector
		net.minecraft.util.math.Vec3d look = player.getRotationVec(1.0F);
		
		// Throw forward and upward, combining player velocity for natural motion
		net.minecraft.util.math.Vec3d velocity = new net.minecraft.util.math.Vec3d(
				look.x * 0.45,
				0.55,
				look.z * 0.45
		).add(player.getVelocity().multiply(0.8));
		
		flyingNugget.setVelocity(velocity);
		world.spawnEntity(flyingNugget);

		// Play standard coin toss sound: high pitch chime/ding!
		if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
			serverWorld.playSound(
					null,
					player.getX(), player.getY(), player.getZ(),
					net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
					net.minecraft.sound.SoundCategory.PLAYERS,
					0.8F,
					1.6F
			);
		}

		// Cooldown of 20 ticks (1 second) to prevent spamming
		//? if >=1.21.4 {
		player.getItemCooldownManager().set(new net.minecraft.item.ItemStack(net.minecraft.item.Items.GOLD_NUGGET), 20);
		//?} else {
		/*player.getItemCooldownManager().set(net.minecraft.item.Items.GOLD_NUGGET, 20);
		*///?}

		// Consume one Golden Nugget from the off-hand stack in survival mode
		if (!player.getAbilities().creativeMode) {
			player.getStackInHand(net.minecraft.util.Hand.OFF_HAND).decrement(1);
		}
	}
}
