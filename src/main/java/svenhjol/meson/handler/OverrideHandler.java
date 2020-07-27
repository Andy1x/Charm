package svenhjol.meson.handler;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import svenhjol.meson.MesonMod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Inspired by Quark.
 * This needs the accesstransformer.cfg set up to enable public translationKey in Item and Block.
 * @link {ItemOverrideHandler.java}
 */
@SuppressWarnings({"unused", "deprecation"})
public class OverrideHandler {
    private static final Map<Item, String> defaultItemKeys = new HashMap<>();
    private static final Map<Block, String> defaultBlockKeys = new HashMap<>();

    public static void changeItemTranslationKey(Item item, @Nullable String newKey) {
        if (!defaultItemKeys.containsKey(item)) {
            // record the default before trying to set it
            defaultItemKeys.put(item, item.getTranslationKey());
        }

        if (newKey == null) {
            newKey = defaultItemKeys.get(item);
        }

        item.translationKey = newKey;
    }

    public static void changeBlockTranslationKey(Block block, @Nullable String newKey) {
        if (!defaultBlockKeys.containsKey(block)) {
            // record the default before trying to set it
            defaultBlockKeys.put(block, block.getTranslationKey());
        }

        if (newKey == null) {
            newKey = defaultBlockKeys.get(block);
        }

        block.translationKey = newKey;
    }

    public static void changeVanillaBlock(MesonMod mod, Block block, ResourceLocation newRes) {
        Registry.register(Registry.BLOCK, newRes, block);
        mod.register(block, newRes);
    }

    public static void changeVanillaItem(MesonMod mod, Item item, ResourceLocation newRes) {
        Registry.register(Registry.ITEM, newRes, item);
        mod.register(item, newRes);
    }
}