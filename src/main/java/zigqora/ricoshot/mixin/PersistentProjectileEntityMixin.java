package zigqora.ricoshot.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class PersistentProjectileEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInject(CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.entityTags().contains("ultrakill_coin_boosted")) {
            Level world = arrow.level();
            if (world.isClientSide()) {
                // coin trails
                for (int i = 0; i < 3; i++) {
                    world.addParticle(
                            ParticleTypes.CRIT,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0, 0, 0
                    );
                    world.addParticle(
                            ParticleTypes.GLOW,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0, 0, 0
                    );
                    world.addParticle(
                            ParticleTypes.END_ROD,
                            arrow.getX(), arrow.getY(), arrow.getZ(),
                            0, 0, 0
                    );
                }
            }
        }
    }
}
