package zigqora.ricoshot.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zigqora.ricoshot.RicoshotConfig;

@Mixin(Projectile.class)
public class ProjectileEntityMixin {

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void onHitInject(HitResult hitResult, CallbackInfo ci) {
        if ((Object) this instanceof AbstractArrow arrow) {
            if (arrow.entityTags().contains("ultrakill_coin_boosted")) {
                Level world = arrow.level();
                if (!world.isClientSide()) {
                    // apply scaled damage
                    if (hitResult instanceof EntityHitResult entityHit) {
                        if (entityHit.getEntity() instanceof LivingEntity victim) {
                            boolean parried = false;
                            if (victim instanceof Player victimPlayer && victimPlayer.isBlocking()) {
                                Vec3 lookVec = victimPlayer.getViewVector(1.0F);
                                Vec3 arrowVel = arrow.getDeltaMovement().normalize();
                                if (lookVec.dot(arrowVel) < 0.0) {
                                    parried = true;

                                    // damage shield
                                    ItemStack activeItem = victimPlayer.getUseItem();
                                    if (!activeItem.isEmpty()) {
                                        EquipmentSlot activeHandSlot = victimPlayer.getUsedItemHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                                        activeItem.hurtAndBreak(10, victimPlayer, activeHandSlot);
                                    }

                                    // play parry sound
                                    world.playSound(
                                            null,
                                            victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                                            SoundEvents.SHIELD_BLOCK,
                                            SoundSource.PLAYERS,
                                            1.5F,
                                            1.0F
                                    );
                                    world.playSound(
                                            null,
                                            victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                                            SoundEvents.ANVIL_LAND,
                                            SoundSource.PLAYERS,
                                            1.0F,
                                            1.8F
                                    );

                                    // spawn parry particles
                                    if (world instanceof ServerLevel serverWorld) {
                                        serverWorld.sendParticles(
                                                ParticleTypes.CRIT,
                                                victimPlayer.getX(), victimPlayer.getEyeY() - 0.2, victimPlayer.getZ(),
                                                30, 0.3, 0.3, 0.3, 0.15
                                        );
                                        serverWorld.sendParticles(
                                                ParticleTypes.GLOW,
                                                victimPlayer.getX(), victimPlayer.getEyeY() - 0.2, victimPlayer.getZ(),
                                                20, 0.3, 0.3, 0.3, 0.05
                                        );
                                    }

                                    // feedback message
                                    if (RicoshotConfig.instance.enableActionBarText) {
                                        victimPlayer.sendOverlayMessage(Component.literal(RicoshotConfig.instance.shieldParryText));
                                        if (arrow.getOwner() instanceof Player attackerPlayer) {
                                            attackerPlayer.sendOverlayMessage(Component.literal(RicoshotConfig.instance.ricoshotBlockedText));
                                        }
                                    }
                                }
                            }

                            if (!parried) {
                                double customDamage = -1.0;
                                for (String tag : arrow.entityTags()) {
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
                                     victim.hurt(victim.damageSources().arrow(arrow, arrow.getOwner()), (float) customDamage);
                                 }

                                // explosive damage
                                world.explode(
                                        arrow,
                                        arrow.getX(),
                                        arrow.getY(),
                                        arrow.getZ(),
                                        4.0F, // 1x power
                                        false, // no fire
                                        Level.ExplosionInteraction.NONE // prevent block damage
                                );

                                // spawn impact trail
                                if (world instanceof ServerLevel serverWorld) {
                                    double x = arrow.getX();
                                    double y = arrow.getY();
                                    double z = arrow.getZ();

                                    // vertical beam
                                    for (int h = 0; h < 6; h++) {
                                        double height = h * 0.4;
                                        serverWorld.sendParticles(
                                                ParticleTypes.GLOW,
                                                x, y + height, z,
                                                12, 0.1, 0.1, 0.1, 0.02
                                        );
                                        serverWorld.sendParticles(
                                                ParticleTypes.END_ROD,
                                                x, y + height, z,
                                                6, 0.1, 0.1, 0.1, 0.02
                                        );
                                    }

                                    // shockwave
                                    for (int i = 0; i < 30; i++) {
                                        double angle = i * (Math.PI * 2 / 30);
                                        double dx = Math.cos(angle) * 1.5;
                                        double dz = Math.sin(angle) * 1.5;
                                        serverWorld.sendParticles(
                                                ParticleTypes.GLOW,
                                                x + dx, y + 0.2, z + dz,
                                                2, 0.05, 0.05, 0.05, 0.01
                                        );
                                        serverWorld.sendParticles(
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
