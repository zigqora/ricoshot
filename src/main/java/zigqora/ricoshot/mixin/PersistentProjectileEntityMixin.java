package zigqora.ricoshot.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInject(CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;
        if (arrow.getCommandTags().contains("ultrakill_coin_boosted")) {
            //? if v12111plus {
            World world = arrow.getEntityWorld();
            //?} else {
            /*World world = arrow.getWorld();
            *///?}
            if (world.isClient()) {
                // Beautiful dense golden, crit, and light trail particles representing the coin boost!
                for (int i = 0; i < 3; i++) {
                    //? if v12111plus {
                    world.addImportantParticleClient(
                            net.minecraft.particle.ParticleTypes.CRIT,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0.0, 0.0, 0.0
                    );
                    world.addImportantParticleClient(
                            net.minecraft.particle.ParticleTypes.GLOW,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0.0, 0.0, 0.0
                    );
                    world.addImportantParticleClient(
                            net.minecraft.particle.ParticleTypes.END_ROD,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0.0, 0.0, 0.0
                    );
                    //?} else {
                    /*world.addParticle(
                            net.minecraft.particle.ParticleTypes.CRIT,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0.0, 0.0, 0.0
                    );
                    world.addParticle(
                            net.minecraft.particle.ParticleTypes.GLOW,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0.0, 0.0, 0.0
                    );
                    world.addParticle(
                            net.minecraft.particle.ParticleTypes.END_ROD,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0.0, 0.0, 0.0
                    );
                    *///?}
                }
            }
        }
    }
}
