package zigqora.ricoshot.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zigqora.ricoshot.RicoshotConfig;

@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {

    @Inject(method = "onCollision", at = @At("HEAD"), cancellable = true)
    private void onCollisionInject(HitResult hitResult, CallbackInfo ci) {
        if ((Object) this instanceof PersistentProjectileEntity arrow) {
            if (arrow.getCommandTags().contains("ultrakill_coin_boosted")) {
                World world = arrow.getWorld();
                if (!world.isClient()) {
                    // Check if we hit a LivingEntity to apply the exact scaled damage
                    if (hitResult instanceof net.minecraft.util.hit.EntityHitResult entityHit) {
                        if (entityHit.getEntity() instanceof net.minecraft.entity.LivingEntity victim) {
                            boolean parried = false;
                            if (victim instanceof PlayerEntity victimPlayer && victimPlayer.isBlocking()) {
                                Vec3d lookVec = victimPlayer.getRotationVec(1.0F);
                                Vec3d arrowVel = arrow.getVelocity().normalize();
                                if (lookVec.dotProduct(arrowVel) < 0.0) {
                                    parried = true;

                                    // 1. Damage shield durability by 10 points
                                    net.minecraft.item.ItemStack activeItem = victimPlayer.getActiveItem();
                                    if (!activeItem.isEmpty()) {
                                    EquipmentSlot activeHandSlot = victimPlayer.getActiveHand() == Hand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                                    // 1.21.2+: damage() requires ServerWorld + ServerPlayerEntity context
                                    if (world instanceof ServerWorld serverWorldDmg && victimPlayer instanceof ServerPlayerEntity serverPlayerVictim) {
                                        activeItem.damage(10, serverWorldDmg, serverPlayerVictim, (Item item) -> {});
                                    }
                                    }

                                    // 2. Plays custom audio: shield block + anvil chime
                                    world.playSound(
                                            null,
                                            victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                                            SoundEvents.ITEM_SHIELD_BLOCK,
                                            SoundCategory.PLAYERS,
                                            1.5F,
                                            1.0F
                                    );
                                    world.playSound(
                                            null,
                                            victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                                            SoundEvents.BLOCK_ANVIL_LAND,
                                            SoundCategory.PLAYERS,
                                            1.0F,
                                            1.8F
                                    );

                                    // 3. Spawns rich sparks (ParticleTypes.CRIT and ParticleTypes.GLOW)
                                    if (world instanceof ServerWorld serverWorld) {
                                        serverWorld.spawnParticles(
                                                ParticleTypes.CRIT,
                                                victimPlayer.getX(), victimPlayer.getEyeY() - 0.2, victimPlayer.getZ(),
                                                30, 0.3, 0.3, 0.3, 0.15
                                        );
                                        serverWorld.spawnParticles(
                                                ParticleTypes.GLOW,
                                                victimPlayer.getX(), victimPlayer.getEyeY() - 0.2, victimPlayer.getZ(),
                                                20, 0.3, 0.3, 0.3, 0.05
                                        );
                                    }

                                    // 4. Send action bar style feedback feeds
                                    if (RicoshotConfig.instance.enableActionBarText) {
                                        victimPlayer.sendMessage(Text.literal(RicoshotConfig.instance.shieldParryText), true);
                                        if (arrow.getOwner() instanceof PlayerEntity attackerPlayer) {
                                            attackerPlayer.sendMessage(Text.literal(RicoshotConfig.instance.ricoshotBlockedText), true);
                                        }
                                    }
                                }
                            }

                            if (!parried) {
                                double customDamage = -1.0;
                                for (String tag : arrow.getCommandTags()) {
                                    if (tag.startsWith("coin_boost_damage:")) {
                                        try {
                                            customDamage = Double.parseDouble(tag.substring("coin_boost_damage:".length()));
                                        } catch (NumberFormatException e) {
                                            // ignore
                                        }
                                        break;
                                    }
                                 }
                                 if (customDamage > 0.0) {
                                     victim.damage((ServerWorld) world, victim.getDamageSources().arrow(arrow, arrow.getOwner()), (float) customDamage);
                                 }

                                // Trigger explosive effect (1x TNT equivalent) for surrounding collateral damage ONLY ON TARGET HIT!
                                world.createExplosion(
                                        arrow,
                                        arrow.getX(),
                                        arrow.getY(),
                                        arrow.getZ(),
                                        4.0F, // 1x TNT equivalent!
                                        false, // no fire
                                        World.ExplosionSourceType.NONE // don't destroy blocks to prevent griefing
                                );

                                // Spawn a lingering yellow trail / beam after the explosion ONLY ON TARGET HIT!
                                if (world instanceof ServerWorld serverWorld) {
                                    double x = arrow.getX();
                                    double y = arrow.getY();
                                    double z = arrow.getZ();

                                    // 1. Column rising up (lingering vertical yellow beam)
                                    for (int h = 0; h < 6; h++) {
                                        double height = h * 0.4;
                                        serverWorld.spawnParticles(
                                                ParticleTypes.GLOW,
                                                x, y + height, z,
                                                12, 0.1, 0.1, 0.1, 0.02
                                        );
                                        serverWorld.spawnParticles(
                                                ParticleTypes.END_ROD,
                                                x, y + height, z,
                                                6, 0.1, 0.1, 0.1, 0.02
                                        );
                                    }

                                    // 2. Expanding radial shockwave of yellow sparkles
                                    for (int i = 0; i < 30; i++) {
                                        double angle = i * (Math.PI * 2 / 30);
                                        double dx = Math.cos(angle) * 1.5;
                                        double dz = Math.sin(angle) * 1.5;
                                        serverWorld.spawnParticles(
                                                ParticleTypes.GLOW,
                                                x + dx, y + 0.2, z + dz,
                                                2, 0.05, 0.05, 0.05, 0.01
                                        );
                                        serverWorld.spawnParticles(
                                                ParticleTypes.CRIT,
                                                x + dx, y + 0.2, z + dz,
                                                2, 0.05, 0.05, 0.05, 0.01
                                        );
                                    }
                                }
                            }
                        }
                    }

                    arrow.discard();
                    ci.cancel();
                }
            }
        }
    }
}
