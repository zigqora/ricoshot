package zigqora.ricoshot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

public class FlyingNuggetEntity extends ThrowableItemProjectile {

    public FlyingNuggetEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public FlyingNuggetEntity(Level world, LivingEntity owner) {
        super(Ricoshot.FLYING_NUGGET_ENTITY_TYPE, owner, world, new ItemStack(Items.GOLD_NUGGET));
    }

    public FlyingNuggetEntity(Level world, double x, double y, double z) {
        super(Ricoshot.FLYING_NUGGET_ENTITY_TYPE, x, y, z, world, new ItemStack(Items.GOLD_NUGGET));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GOLD_NUGGET;
    }

    @Override
    public void tick() {
        super.tick();

        // spawn particles
        if (this.level().isClientSide()) {
            this.level().addParticle(
                    ParticleTypes.GLOW,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    0, 0, 0
            );
            this.level().addParticle(
                    ParticleTypes.CRIT,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    0, 0, 0
            );
        } else {
            // Shot window
            if (this.tickCount == 14) {
                this.level().playSound(
                        null,
                        this.getX(), this.getY(), this.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS,
                        1.5F,
                        2.0F // chime sound
                );
                if (this.level() instanceof ServerLevel serverWorld) {
                    serverWorld.sendParticles(
                            ParticleTypes.EXPLOSION,
                            this.getX(), this.getY(), this.getZ(),
                            1, 0.0, 0.0, 0.0, 0.0
                    );
                    serverWorld.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER, //villager particle that used in emerald trading
                            this.getX(), this.getY() + 0.1, this.getZ(),
                            10, 0.1, 0.1, 0.1, 0.1
                    );
                }
            }

            // detect projectiles
            AABB searchBox = this.getBoundingBox().inflate(1.2); // 1.2 block hitbox
            List<AbstractArrow> arrows = this.level().getEntitiesOfClass(
                    AbstractArrow.class,
                    searchBox,
                    arrow -> arrow.isAlive() && (
                            arrow.entityTags().contains("ultrakill_coin_boosted") ||
                            (arrow.getWeaponItem() != null && arrow.getWeaponItem().is(Items.BOW))
                    )
            );

            if (!arrows.isEmpty()) {
                AbstractArrow arrow = arrows.get(0);
                triggerCoinHit(arrow);
            }

            // handle expiry
            if (this.tickCount > 100 || this.onGround()) {
                this.discard();
            }
        }
    }

    private void triggerCoinHit(AbstractArrow arrow) {
        Level world = this.level();

        // check chain status
        int chainCount = 0;
        boolean wasChainPerfect = false;

        for (String tag : arrow.entityTags()) {
            if (tag.startsWith("coin_chain_count:")) {
                try {
                    chainCount = Integer.parseInt(tag.substring("coin_chain_count:".length()));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            if (tag.equals("coin_perfect_timing")) {
                wasChainPerfect = true;
            }
        }

        // Increment the chain bounce count
        chainCount++;

        // check timing window
        boolean isPerfectTiming = (this.tickCount >= 13 && this.tickCount <= 25) || wasChainPerfect;

        // play hit sound
        float chimePitch = Math.min(2.0F, 1.4F + chainCount * 0.15F);
        world.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.BELL_BLOCK,
                SoundSource.PLAYERS,
                1.5F,
                chimePitch
        );
        world.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                1.5F,
                chimePitch - 0.2F
        );

        // spawn hit flash
        if (world instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(
                    ParticleTypes.EXPLOSION,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0
            );
            serverWorld.sendParticles(
                    ParticleTypes.CRIT,
                    this.getX(), this.getY(), this.getZ(),
                    20, 0.2, 0.2, 0.2, 0.15
            );
        }

        // redirect to targets
        double range = zigqora.ricoshot.RicoshotConfig.instance.targetingRadius;
        List<LivingEntity> targets = world.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(range),
                entity -> {
                    if (!entity.isAlive() || entity == arrow.getOwner()) {
                        return false;
                    }
                    // check los
                    if (!entity.hasLineOfSight(this)) {
                        return false;
                    }
                    if (arrow.getOwner() instanceof LivingEntity shooter) {
                        Vec3 lookVec = shooter.getViewVector(1.0F);
                        Vec3 targetVec = entity.getEyePosition().subtract(shooter.getEyePosition()).normalize();
                        double dot = lookVec.dot(targetVec);
                        return dot > 0.5; // fov check
                    }
                    return true;
                }
        );

        if (!targets.isEmpty()) {
            // cluster targets
            List<List<LivingEntity>> groups = clusterMobs(targets, 4.5);

            // notify shooter
            if (arrow.getOwner() instanceof Player player && RicoshotConfig.instance.enableActionBarText) {
                if (isPerfectTiming) {
                    player.sendOverlayMessage(Component.literal(RicoshotConfig.instance.ultraRicoshotPerfectText));
                } else if (chainCount > 1) {
                    player.sendOverlayMessage(Component.literal(RicoshotConfig.instance.ultraRicoshotText.replace("{chain}", String.valueOf(chainCount))));
                } else {
                    player.sendOverlayMessage(Component.literal(RicoshotConfig.instance.ricoshotText));
                }
            }

            // split shot per group
            for (List<LivingEntity> group : groups) {
                if (group.isEmpty()) continue;

                // get closest in group
                LivingEntity representative = null;
                double nearestDistSq = Double.MAX_VALUE;
                for (LivingEntity mob : group) {
                    double distSq = this.distanceToSqr(mob);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        representative = mob;
                    }
                }

                if (representative != null) {
                    // calc damage
                    double damageToDeal = zigqora.ricoshot.RicoshotConfig.instance.baseDamage;

                    // perfect timing bonus
                    if (isPerfectTiming) {
                        damageToDeal *= 1.5;
                    }

                    float damageAmount = (float) damageToDeal;

                    boolean parried = false;
                    if (representative instanceof Player victimPlayer && victimPlayer.isBlocking()) {
                        Vec3 lookVec = victimPlayer.getViewVector(1.0F);
                        Vec3 toCoinVec = this.position().subtract(victimPlayer.position()).normalize();
                        if (lookVec.dot(toCoinVec) > 0.0) {
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

                    Vec3 targetPos = representative.getEyePosition().subtract(0, 0.2, 0);

                    if (!parried) {
                        // apply hitscan damage
                        representative.hurt(representative.damageSources().arrow(null, arrow.getOwner()), (float) damageAmount);

                        // cosmetic explosion
                        if (world instanceof ServerLevel serverWorld) {
                            serverWorld.sendParticles(
                                    ParticleTypes.EXPLOSION_EMITTER,
                                    representative.getX(),
                                    representative.getY() + 1.0,
                                    representative.getZ(),
                                    1, 0.0, 0.0, 0.0, 0.0
                            );
                        }

                        // target explosion sound
                        if (RicoshotConfig.instance.playExplosionSound && world instanceof ServerLevel serverWorldExplode) {
                            serverWorldExplode.playSound(
                                    null,
                                    representative.getX(),
                                    representative.getY(),
                                    representative.getZ(),
                                    SoundEvents.GENERIC_EXPLODE,
                                    SoundSource.PLAYERS,
                                    1.2F,
                                    1.0F
                            );
                        }

                        // spawn beam
                        if (world instanceof ServerLevel serverWorld) {
                            spawnSplitBeam(serverWorld, this.position(), targetPos);

                            // Post-explosion particles at target position
                            double rx = representative.getX();
                            double ry = representative.getY();
                            double rz = representative.getZ();

                            // vertical beam
                            for (int h = 0; h < 6; h++) {
                                double height = h * 0.4;
                                serverWorld.sendParticles(
                                        ParticleTypes.GLOW,
                                        rx, ry + height, rz,
                                        12, 0.1, 0.1, 0.1, 0.02
                                );
                                serverWorld.sendParticles(
                                        ParticleTypes.END_ROD,
                                        rx, ry + height, rz,
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
                                        rx + dx, ry + 0.2, rz + dz,
                                        2, 0.05, 0.05, 0.05, 0.01
                                    );
                                serverWorld.sendParticles(
                                        ParticleTypes.CRIT,
                                        rx + dx, ry + 0.2, rz + dz,
                                        2, 0.05, 0.05, 0.05, 0.01
                                );
                            }
                        }
                    } else {
                        // spawn beam even when parried
                        if (world instanceof ServerLevel serverWorld) {
                            spawnSplitBeam(serverWorld, this.position(), targetPos);
                        }
                    }

                    // Play shoot/redirect sound for audio feedback
                    world.playSound(
                            null,
                            this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ARROW_SHOOT,
                            SoundSource.PLAYERS,
                            1.2F,
                            1.4F
                    );
                }
            }
            // Discard the original arrow since it successfully split
            arrow.discard();
        } else {
            // Send standard style message to shooter with a miss notice
            if (arrow.getOwner() instanceof Player player && RicoshotConfig.instance.enableActionBarText) {
                player.sendOverlayMessage(Component.literal(RicoshotConfig.instance.ricoshotNoTargetsText));
            }

            // Spawn simple smoke particles at the coin's final position to show a clean miss!
            if (world instanceof ServerLevel serverWorld) {
                serverWorld.sendParticles(
                        ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        15, 0.1, 0.1, 0.1, 0.05
                );
            }

            arrow.discard();
        }

        // Discard the nugget
        this.discard();
    }

    private List<List<LivingEntity>> clusterMobs(List<LivingEntity> mobs, double threshold) {
        List<List<LivingEntity>> clusters = new ArrayList<>();

        for (LivingEntity mob : mobs) {
            List<List<LivingEntity>> matchingClusters = new ArrayList<>();

            for (List<LivingEntity> cluster : clusters) {
                for (LivingEntity clusterMob : cluster) {
                    if (mob.distanceTo(clusterMob) <= threshold) {
                        matchingClusters.add(cluster);
                        break;
                    }
                }
            }

            if (matchingClusters.isEmpty()) {
                List<LivingEntity> newCluster = new ArrayList<>();
                newCluster.add(mob);
                clusters.add(newCluster);
            } else if (matchingClusters.size() == 1) {
                matchingClusters.get(0).add(mob);
            } else {
                List<LivingEntity> mergedCluster = matchingClusters.get(0);
                mergedCluster.add(mob);
                for (int i = 1; i < matchingClusters.size(); i++) {
                    List<LivingEntity> toMerge = matchingClusters.get(i);
                    mergedCluster.addAll(toMerge);
                    clusters.remove(toMerge);
                }
            }
        }

        return clusters;
    }

    private void spawnSplitBeam(ServerLevel world, Vec3 start, Vec3 end) {
        Vec3 diff = end.subtract(start);
        double distance = diff.length();
        int particleCount = (int) Math.max(10, distance * 5); // 5 particles per block, minimum 10
        for (int i = 0; i <= particleCount; i++) {
            double t = (double) i / particleCount;
            Vec3 point = start.add(diff.scale(t));
            // Spawn yellow glowing dust and end rod lines
            world.sendParticles(
                    ParticleTypes.GLOW,
                    point.x, point.y, point.z,
                    1, 0.0, 0.0, 0.0, 0.0
            );
            world.sendParticles(
                    ParticleTypes.END_ROD,
                    point.x, point.y, point.z,
                    1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }
}
