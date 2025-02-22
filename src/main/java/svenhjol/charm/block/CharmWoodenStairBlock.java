package svenhjol.charm.block;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.loader.CharmModule;

public abstract class CharmWoodenStairBlock extends StairBlock implements ICharmBlock {
    private final CharmModule module;

    public CharmWoodenStairBlock(CharmModule module, String name, BlockState state, Properties settings) {
        super(state, settings);

        this.register(module, name);
        this.module = module;
        this.setFireInfo(5, 20);
        this.setBurnTime(300);
    }

    public CharmWoodenStairBlock(CharmModule module, String name, Block block) {
        this(module, name, block.defaultBlockState(), Properties.copy(block));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (enabled()) {
            super.fillItemCategory(group, items);
        }
    }

    @Override
    public boolean enabled() {
        return module.isEnabled();
    }
}
