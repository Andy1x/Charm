package svenhjol.meson.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.meson.helper.ItemHelper;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ItemEntity;remove()V",
            ordinal = 1
        ),
        cancellable = true
    )
    private void hookTick(CallbackInfo ci) {
        if (!ItemHelper.shouldItemDespawn((ItemEntity)(Object)this))
            ci.cancel();
    }
}
