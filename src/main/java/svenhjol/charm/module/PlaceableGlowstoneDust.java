package svenhjol.charm.module;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockRayTraceResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.block.PlacedGlowstoneDustBlock;

@Module(mod = Charm.MOD_ID, description = "Glowstone dust can be placed on the ground as a light source.")
public class PlaceableGlowstoneDust extends CharmModule {
    public static PlacedGlowstoneDustBlock PLACED_GLOWSTONE_DUST;

    @Override
    public void register() {
        PLACED_GLOWSTONE_DUST = new PlacedGlowstoneDustBlock(this);
    }

    @Override
    public void clientRegister() {
        RenderTypeLookup.setRenderLayer(PLACED_GLOWSTONE_DUST, RenderType.getCutout());
    }

    @Override
    public void init() {
        UseBlockCallback.EVENT.register(this::tryPlaceDust);
    }

    public static boolean tryPlaceDust(World world, RayTraceResult hitResult) {
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return false;

        BlockRayTraceResult BlockRayTraceResult = (BlockRayTraceResult)hitResult;
        BlockPos pos = BlockRayTraceResult.getBlockPos();
        Direction side = BlockRayTraceResult.getSide();
        BlockState state = world.getBlockState(pos);
        BlockPos offsetPos = pos.offset(side);

        if (state.isSolidSide(world, pos, side) && PosHelper.isLikeAir(world, offsetPos) && world.getBlockState(offsetPos).getBlock() != Blocks.LAVA) {
            BlockState placedState = PlaceableGlowstoneDust.PLACED_GLOWSTONE_DUST.getDefaultState()
                .with(PlacedGlowstoneDustBlock.FACING, side);

            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() == Blocks.WATER)
                placedState = placedState.with(Properties.WATERLOGGED, true);

            world.setBlockState(offsetPos, placedState, 2);
            world.playSound(null, offsetPos, SoundEvents.BLOCK_NYLIUM_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    private ActionResult tryPlaceDust(PlayerEntity player, World world, Hand hand, BlockRayTraceResult hitResult) {
        ItemStack stack = player.getHeldItem(hand);

        if (world != null && stack.getItem() == Items.GLOWSTONE_DUST) {
            player.swingHand(hand);

            if (!world.isRemote) {
                boolean result = tryPlaceDust(world, hitResult);

                if (result) {
                    if (!player.isCreative())
                        stack.decrement(1);

                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }
}
