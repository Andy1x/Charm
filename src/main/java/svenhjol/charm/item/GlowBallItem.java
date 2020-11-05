package svenhjol.charm.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.NonNullList;
import net.minecraft.world.World;
import svenhjol.charm.entity.GlowBallEntity;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.ICharmItem;

public class GlowBallItem extends EnderPearlItem implements ICharmItem {
    protected CharmModule module;

    public GlowBallItem(CharmModule module) {
        super(new Item.Settings().maxCount(16).group(ItemGroup.MISC));
        this.module = module;
        this.register(module, "glow_ball");
    }

    @Override
    public void appendStacks(ItemGroup group, NonNullList<ItemStack> stacks) {
        if (enabled())
            super.appendStacks(group, stacks);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getHeldItem(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (RANDOM.nextFloat() * 0.4F + 0.8F));
        user.getItemCooldownManager().set(this, 10);

        if (!world.isRemote) {
            GlowBallEntity entity = new GlowBallEntity(world, user);
            entity.setItem(itemStack);
            entity.setProperties(user, user.pitch, user.yaw, 0.0F, 1.5F, 1.0F);
            world.addEntity(entity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.abilities.creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isRemote());
    }

    @Override
    public boolean enabled() {
        return module.enabled;
    }
}
