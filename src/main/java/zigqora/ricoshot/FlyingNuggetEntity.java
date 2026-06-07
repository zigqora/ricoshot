package zigqora.ricoshot;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FlyingNuggetEntity extends ThrownItemEntity {

    public FlyingNuggetEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public FlyingNuggetEntity(World world, LivingEntity owner) {
        //? if >=1.21.4 {
        super(Ricoshot.FLYING_NUGGET_ENTITY_TYPE, owner, world, new net.minecraft.item.ItemStack(net.minecraft.item.Items.GOLD_NUGGET));
        //?} else {
        /*super(Ricoshot.FLYING_NUGGET_ENTITY_TYPE, owner, world);
        *///?}
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GOLD_NUGGET;
    }

    @Override
    public void tick() {
        super.tick();

        // Cache world to avoid repeated getWorld() calls (return type changed in 1.21.5+)
        //? if >=1.21.11 {
        /*World world = (World) this.getEntityWorld();
        *///?} else {
        World world = (World) this.getWorld();
        //?}

        // Spawn nice sparkling gold particles on client side!
        if (world.isClient()) {
            //? if >=1.21.11 {
            /*world.addImportantParticleClient(
                    ParticleTypes.GLOW,
                    this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0
            );
            world.addImportantParticleClient(
                    ParticleTypes.CRIT,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    0.0, 0.0, 0.0
            );
            *///?} else {
            world.addParticle(
                    ParticleTypes.GLOW,
                    this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0
            );
            world.addParticle(
                    ParticleTypes.CRIT,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    0.0, 0.0, 0.0
            );
            //?}
        } else {
            // Apex Twinkle Indicator: at tick 14, right before reaching its peak, play a chime and flash!
            if (this.age == 14) {
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.playSound(
                            null,
                            this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                            SoundCategory.PLAYERS,
                            1.5F,
                            2.0F
                    );
                    //? if >=1.21.11 {
                    /*serverWorld.spawnParticles(
                            ParticleTypes.EXPLOSION,
                            this.getX(), this.getY(), this.getZ(),
                            1, 0.0, 0.0, 0.0, 0.0
                    );
                    *///?} else {
                    serverWorld.spawnParticles(
                            ParticleTypes.FLASH,
                            this.getX(), this.getY(), this.getZ(),
                            1, 0.0, 0.0, 0.0, 0.0
                    );
                    //?}
                    serverWorld.spawnParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getY() + 0.1, this.getZ(),
                            10, 0.1, 0.1, 0.1, 0.1
                    );
                }
            }

            // Find any projectile entity (arrow, spectral arrow, etc.) close to us
            Box searchBox = this.getBoundingBox().expand(1.2);
            List<PersistentProjectileEntity> arrows = world.getEntitiesByClass(
                    PersistentProjectileEntity.class,
                    searchBox,
                    arrow -> arrow.isAlive() && (
                            arrow.getCommandTags().contains("ultrakill_coin_boosted") ||
                            (arrow.getWeaponStack() != null && arrow.getWeaponStack().isOf(Items.BOW))
                    )
            );

            if (!arrows.isEmpty()) {
                PersistentProjectileEntity arrow = arrows.get(0);
                triggerCoinHit(arrow);
            }

            // Also, if the nugget hits the ground or stays in air too long, discard it
            if (this.age > 100 || this.isOnGround()) {
                this.discard();
            }
        }
    }

    private void triggerCoinHit(PersistentProjectileEntity arrow) {
        //? if >=1.21.11 {
        /*World world = (World) this.getEntityWorld();
        *///?} else {
        World world = (World) this.getWorld();
        //?}

        // 1. Check current chain count and perfect timing status from the incoming arrow
        int chainCount = 0;
        boolean wasChainPerfect = false;

        for (String tag : arrow.getCommandTags()) {
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

        // Perfect timing window is when the coin is between ticks 13 and 25, or if the chain is already perfect
        boolean isPerfectTiming = (this.age >= 13 && this.age <= 25) || wasChainPerfect;

        // Play the ultrakill coin hit sound: a high pitch ding!
        // Pitch increases with the chain count to sound more and more chaotic and epic!
        float chimePitch = Math.min(2.0F, 1.4F + chainCount * 0.15F);
        if (world instanceof ServerWorld serverWorldSound) {
            serverWorldSound.playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_BELL_USE,
                    SoundCategory.PLAYERS,
                    1.5F,
                    chimePitch
            );
            serverWorldSound.playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS,
                    1.5F,
                    chimePitch - 0.2F
            );
        }

        // Spawn an epic flash particle at the coin position!
        if (world instanceof ServerWorld serverWorld) {
            //? if >=1.21.11 {
            /*serverWorld.spawnParticles(
                    ParticleTypes.EXPLOSION,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0
            );
            *///?} else {
            serverWorld.spawnParticles(
                    ParticleTypes.FLASH,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0
            );
            //?}
            serverWorld.spawnParticles(
                    ParticleTypes.CRIT,
                    this.getX(), this.getY(), this.getZ(),
                    20, 0.2, 0.2, 0.2, 0.15
            );
        }

        // 3. No more other nuggets: redirect the arrow to nearby mob / player targets in shooter's FOV!
        double range = zigqora.ricoshot.RicoshotConfig.instance.targetingRadius;
        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                this.getBoundingBox().expand(range),
                entity -> {
                    if (!entity.isAlive() || entity == arrow.getOwner()) {
                        return false;
                    }
                    // Ensure the target has direct line-of-sight to the coin (no shooting through walls or blocks!)
                    if (!entity.canSee(this)) {
                        return false;
                    }
                    if (arrow.getOwner() instanceof LivingEntity shooter) {
                        Vec3d lookVec = shooter.getRotationVec(1.0F);
                        Vec3d targetVec = entity.getEyePos().subtract(shooter.getEyePos()).normalize();
                        double dot = lookVec.dotProduct(targetVec);
                        return dot > 0.5; // Must be within the shooter's ~120 degree FOV cone
                    }
                    return true;
                }
        );

        if (!targets.isEmpty()) {
            // Cluster the mobs into groups based on spatial proximity (distance <= 4.5 blocks)
            List<List<LivingEntity>> groups = clusterMobs(targets, 4.5);

            // Send the ultimate final hit style feed to the shooter player
            if (arrow.getOwner() instanceof PlayerEntity player && RicoshotConfig.instance.enableActionBarText) {
                if (isPerfectTiming) {
                    player.sendMessage(Text.literal(RicoshotConfig.instance.ultraRicoshotPerfectText), true);
                } else if (chainCount > 1) {
                    player.sendMessage(Text.literal(RicoshotConfig.instance.ultraRicoshotText.replace("{chain}", String.valueOf(chainCount))), true);
                } else {
                    player.sendMessage(Text.literal(RicoshotConfig.instance.ricoshotText), true);
                }
            }

            // For each group, split one hitscan redirect directed at the representative target of that group
            for (List<LivingEntity> group : groups) {
                if (group.isEmpty()) continue;

                // Pick the representative (closest target to the coin in the group)
                LivingEntity representative = null;
                double nearestDistSq = Double.MAX_VALUE;
                for (LivingEntity mob : group) {
                    double distSq = this.squaredDistanceTo(mob);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        representative = mob;
                    }
                }

                if (representative != null) {
                    // Calculate damage based on user's configuration
                    // The user requested that we use the slider's configured heart damage directly.
                    double damageToDeal = zigqora.ricoshot.RicoshotConfig.instance.baseDamage;

                    // Perfect timing grants a 50% damage boost instead of a guaranteed instakill
                    if (isPerfectTiming) {
                        damageToDeal *= 1.5;
                    }

                    float damageAmount = (float) damageToDeal;


                    boolean parried = false;
                    if (representative instanceof PlayerEntity victimPlayer && victimPlayer.isBlocking()) {
                        Vec3d lookVec = victimPlayer.getRotationVec(1.0F);
                        //? if >=1.21.11 {
                        /*Vec3d toCoinVec = new Vec3d(this.getX(), this.getY(), this.getZ()).subtract(new Vec3d(victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ())).normalize();
                        *///?} else {
                        Vec3d toCoinVec = this.getPos().subtract(victimPlayer.getPos()).normalize();
                        //?}
                        if (lookVec.dotProduct(toCoinVec) > 0.0) {
                            parried = true;

                            // 1. Damage shield durability by 10 points
                            net.minecraft.item.ItemStack activeItem = victimPlayer.getActiveItem();
                            if (!activeItem.isEmpty()) {
                                net.minecraft.entity.EquipmentSlot activeHandSlot = victimPlayer.getActiveHand() == net.minecraft.util.Hand.OFF_HAND ? net.minecraft.entity.EquipmentSlot.OFFHAND : net.minecraft.entity.EquipmentSlot.MAINHAND;
                                // 1.21.2+: damage() requires ServerWorld + ServerPlayerEntity context
                                if (world instanceof ServerWorld serverWorld2 && victimPlayer instanceof ServerPlayerEntity serverPlayerVictim) {
                                    //? if >=1.21.4 {
                                    activeItem.damage(10, serverWorld2, serverPlayerVictim, (Item item) -> {});
                                    //?} else {
                                    /*activeItem.damage(10, serverPlayerVictim, activeHandSlot);
                                    *///?}
                                }
                            }

                            // 2. Plays custom audio: shield block + anvil chime
                            if (world instanceof ServerWorld serverWorldParry) {
                                serverWorldParry.playSound(
                                        null,
                                        victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                                        SoundEvents.ITEM_SHIELD_BLOCK,
                                        SoundCategory.PLAYERS,
                                        1.5F,
                                        1.0F
                                );
                                serverWorldParry.playSound(
                                        null,
                                        victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                                        SoundEvents.BLOCK_ANVIL_LAND,
                                        SoundCategory.PLAYERS,
                                        1.0F,
                                        1.8F
                                );
                            }

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

                    Vec3d targetPos = representative.getEyePos().subtract(0, 0.2, 0);

                    if (!parried) {
                        // Apply instant hitscan damage
                        //? if >=1.21.4 {
                        representative.damage((ServerWorld) world, representative.getDamageSources().arrow(null, arrow.getOwner()), (float) damageAmount);
                        //?} else {
                        /*representative.damage(representative.getDamageSources().arrow(null, arrow.getOwner()), (float) damageAmount);
                        *///?}

                        // Spawn a purely cosmetic explosion particle!
                        // (We used to call world.createExplosion here, which dealt massive TNT damage and ruined the math!)
                        if (world instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(
                                    ParticleTypes.EXPLOSION_EMITTER,
                                    representative.getX(),
                                    representative.getY() + 1.0,
                                    representative.getZ(),
                                    1, 0.0, 0.0, 0.0, 0.0
                            );
                        }

                        // Play explosion sound at target location if enabled in config
                        if (RicoshotConfig.instance.playExplosionSound && world instanceof ServerWorld serverWorldExplode) {
                            serverWorldExplode.playSound(
                                    null,
                                    representative.getX(),
                                    representative.getY(),
                                    representative.getZ(),
                                    SoundEvents.ENTITY_GENERIC_EXPLODE,
                                    SoundCategory.PLAYERS,
                                    1.2F,
                                    1.0F
                            );
                        }

                        // Spawn visual splitting beam from coin to target
                        if (world instanceof ServerWorld serverWorld) {
                            //? if >=1.21.11 {
                            /*spawnSplitBeam(serverWorld, new Vec3d(this.getX(), this.getY(), this.getZ()), targetPos);
                            *///?} else {
                            spawnSplitBeam(serverWorld, this.getPos(), targetPos);
                            //?}

                            // Post-explosion particles at target position
                            double rx = representative.getX();
                            double ry = representative.getY();
                            double rz = representative.getZ();

                            // 1. Column rising up (lingering vertical yellow beam)
                            for (int h = 0; h < 6; h++) {
                                double height = h * 0.4;
                                serverWorld.spawnParticles(
                                        ParticleTypes.GLOW,
                                        rx, ry + height, rz,
                                        12, 0.1, 0.1, 0.1, 0.02
                                );
                                serverWorld.spawnParticles(
                                        ParticleTypes.END_ROD,
                                        rx, ry + height, rz,
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
                                        rx + dx, ry + 0.2, rz + dz,
                                        2, 0.05, 0.05, 0.05, 0.01
                                    );
                                serverWorld.spawnParticles(
                                        ParticleTypes.CRIT,
                                        rx + dx, ry + 0.2, rz + dz,
                                        2, 0.05, 0.05, 0.05, 0.01
                                );
                            }
                        }
                    } else {
                        // Spawn visual splitting beam from coin to target even when parried
                        if (world instanceof ServerWorld serverWorld) {
                            //? if >=1.21.11 {
                            /*spawnSplitBeam(serverWorld, new Vec3d(this.getX(), this.getY(), this.getZ()), targetPos);
                            *///?} else {
                            spawnSplitBeam(serverWorld, this.getPos(), targetPos);
                            //?}
                        }
                    }

                    // Play shoot/redirect sound for audio feedback
                    if (world instanceof ServerWorld serverWorldShoot) {
                        serverWorldShoot.playSound(
                                null,
                                this.getX(), this.getY(), this.getZ(),
                                SoundEvents.ENTITY_ARROW_SHOOT,
                                SoundCategory.PLAYERS,
                                1.2F,
                                1.4F
                        );
                    }
                }
            }
            // Discard the original arrow since it successfully split
            arrow.discard();
        } else {
            // Send standard style message to shooter with a miss notice
            if (arrow.getOwner() instanceof PlayerEntity player && RicoshotConfig.instance.enableActionBarText) {
                player.sendMessage(Text.literal(RicoshotConfig.instance.ricoshotNoTargetsText), true);
            }

            // Spawn simple smoke particles at the coin's final position to show a clean miss!
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
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

    /**
     * Single-Linkage Clustering to group mobs by proximity.
     * Mobs within the threshold distance are merged into the same group.
     */
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
                // Merge multiple matching clusters
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

    private void spawnSplitBeam(ServerWorld world, Vec3d start, Vec3d end) {
        Vec3d diff = end.subtract(start);
        double distance = diff.length();
        int particleCount = (int) Math.max(10, distance * 5); // 5 particles per block, minimum 10
        for (int i = 0; i <= particleCount; i++) {
            double t = (double) i / particleCount;
            Vec3d point = start.add(diff.multiply(t));
            // Spawn intense yellow glowing dust and end rod lines
            world.spawnParticles(
                    ParticleTypes.GLOW,
                    point.x, point.y, point.z,
                    1, 0.0, 0.0, 0.0, 0.0
            );
            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    point.x, point.y, point.z,
                    1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }
}
