package svenhjol.charm.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.util.ActionResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.event.UpdateAnvilCallback;
import svenhjol.charm.module.AnvilImprovements;
import svenhjol.charm.module.StackableEnchantedBooks;

import java.util.HashMap;
import java.util.Map;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;

    @Shadow private String newItemName;

    @Shadow private int repairItemUsage;

    public AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Redirect(
        method = "updateResult",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z",
            ordinal = 1
        )
    )
    private boolean hookUpdateResultTooExpensive(PlayerAbilities abilities) {
        return AnvilImprovements.allowTooExpensive() || abilities.creativeMode;
    }

    @Inject(
        method = "updateResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isDamageable()Z"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void hookUpdateResultUpdateAnvil(CallbackInfo ci, ItemStack left, int i, int baseCost, int k, ItemStack itemStack2, ItemStack right) {
        ActionResult result = UpdateAnvilCallback.EVENT.invoker().interact((AnvilScreenHandler)(Object)this, left, right, this.output, this.newItemName, baseCost, this::applyUpdateAnvil);
        if (result == ActionResult.SUCCESS)
            ci.cancel();
    }

    private void applyUpdateAnvil(ItemStack out, int xpCost, int materialCost) {
        output.setStack(0, out);
        levelCost.set(xpCost);
        repairItemUsage = materialCost;
    }

    @Redirect(
        method = "updateResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;set(Ljava/util/Map;Lnet/minecraft/item/ItemStack;)V"
        )
    )
    private void hookUpdateResultAllowHigherLevel2(Map<Enchantment, Integer> enchantments, ItemStack stack) {
        ItemStack inputStack = this.input.getStack(1);

        Map<Enchantment, Integer> reset = new HashMap<>();

        // TODO: check it's an enchanted book

        Map<Enchantment, Integer> bookEnchants = EnchantmentHelper.get(inputStack);

        bookEnchants.forEach((e, l) -> {
            if (l > e.getMaxLevel()) {
                reset.put(e, l);
            }
        });

        reset.forEach((e, l) -> {
            if (enchantments.containsKey(e))
                enchantments.put(e, l);
        });

        EnchantmentHelper.set(reset, stack);
    }

    @Inject(
        method = "canTakeOutput",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookCanTakeOutput(PlayerEntity player, boolean unused, CallbackInfoReturnable<Boolean> cir) {
        if (AnvilImprovements.allowTakeWithoutXp(player, levelCost))
            cir.setReturnValue(true);
    }

    @Redirect(
        method = "onTakeOutput",
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/item/ItemStack;decrement(I)V"
            )
        ),
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V",
            opcode = Opcodes.INVOKEINTERFACE,
            ordinal = 2
        )
    )
    private void anvilUpdateHook(Inventory inv, int index, ItemStack stack) {
        if (ModuleHandler.enabled("charm:stackable_enchanted_books"))
            stack = StackableEnchantedBooks.getReducedStack(inv.getStack(index));

        inv.setStack(index, stack);
    }


}
