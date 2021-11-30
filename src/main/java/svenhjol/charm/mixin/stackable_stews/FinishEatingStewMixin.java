package svenhjol.charm.mixin.stackable_stews;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import svenhjol.charm.module.stackable_stews.StackableStews;

@Mixin({BowlFoodItem.class, SuspiciousStewItem.class})
public class FinishEatingStewMixin {
    /**
     * Defer to tryEatStewStack when mushroom stew is eaten.
     * If the check passes, return the decremented stack.
     *
     * If this mixin is not present, vanilla will reduce an
     * entire stack of stew to a single bowl.
     */
    @Inject(
        method = "finishUsingItem",
        at = @At(value = "TAIL"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void hookFinishUsing(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir, ItemStack eatenStack) {
        boolean result = StackableStews.tryEatStewStack(entity, eatenStack);
        if (result) {
            cir.setReturnValue(eatenStack);
        }
    }
}
