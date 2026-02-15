package nl.theepicblock.ppetp.mixin;

import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.server.world.ServerWorld;
import nl.theepicblock.ppetp.AllayTeleporter;
import nl.theepicblock.ppetp.PPeTP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayEntity.class)
public class AllayEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        try {
            var self = (AllayEntity)(Object)this;
            if (self.getEntityWorld() instanceof ServerWorld) {
                AllayTeleporter.checkAllay(self);
            }
        } catch (Exception e) {
            PPeTP.LOGGER.error("Failed to process allay teleport check", e);
        }
    }
}
