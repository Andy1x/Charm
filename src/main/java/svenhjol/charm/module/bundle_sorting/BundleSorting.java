package svenhjol.charm.module.bundle_sorting;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.bundle_sorting.event.SortBundleItemsCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@CommonModule(mod = Charm.MOD_ID, description = "Scroll the mouse while hovering over a bundle to change the order of its contents.")
public class BundleSorting extends CharmModule {
    public static final String TAG_ITEMS = "Items"; // must match tag of BundleItem
    public static final ResourceLocation MSG_SERVER_SCROLLED_ON_HOVER = new ResourceLocation(Charm.MOD_ID, "server_scrolled_on_hover");

    // add items that should allow bundle sorting to this list
    public static final List<ItemLike> SORTABLE = new ArrayList<>();

    @Override
    public void register() {
        SORTABLE.add(Items.BUNDLE);
    }

    @Override
    public void runWhenEnabled() {
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_SCROLLED_ON_HOVER, this::handleScrollOnHover);
        SortBundleItemsCallback.EVENT.register(this::handleSortBundleItems);
    }

    private void handleScrollOnHover(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        int slotIndex = buffer.readInt();
        boolean direction = buffer.readBoolean();

        server.execute(() -> {
            if (player == null) return;
            ItemStack itemInSlot = player.containerMenu.getSlot(slotIndex).getItem();
            SortBundleItemsCallback.EVENT.invoker().interact(player, itemInSlot, direction);
        });
    }

    private void handleSortBundleItems(ServerPlayer player, ItemStack stack, boolean direction) {
        if (stack.getItem() instanceof BundleItem) {
            List<ItemStack> contents = BundleItem.getContents(stack).collect(Collectors.toCollection(LinkedList::new));
            if (contents.size() < 1) return;

            SortBundleItemsCallback.sortByScrollDirection(contents, direction);
            stack.removeTagKey(TAG_ITEMS);

            CompoundTag nbt = stack.getOrCreateTag();
            nbt.put(TAG_ITEMS, new ListTag());
            ListTag list = nbt.getList(TAG_ITEMS, 10);

            Collections.reverse(contents);
            contents.forEach(s -> {
                CompoundTag stackNbt = new CompoundTag();
                s.save(stackNbt);
                list.add(0, stackNbt);
            });
        }
    }
}
