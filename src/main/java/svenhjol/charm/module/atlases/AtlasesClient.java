package svenhjol.charm.module.atlases;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.event.RenderHeldItemCallback;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.ClientRegistry;

import java.util.List;

@ClientModule(module = Atlases.class)
public class AtlasesClient extends CharmModule {
    public static KeyMapping keyBinding;
    public int swappedSlot = -1;
    private AtlasRenderer renderer;

    @Override
    public void register() {
        ClientRegistry.menuScreen(Atlases.MENU, AtlasScreen::new);
    }

    @Override
    public void runWhenEnabled() {
        RenderHeldItemCallback.EVENT.register(this::handleRenderItem);
        ItemTooltipCallback.EVENT.register(this::handleItemTooltip);
        ClientPlayNetworking.registerGlobalReceiver(Atlases.MSG_CLIENT_SWAPPED_SLOT, this::handleSwappedSlot);
        ClientPlayNetworking.registerGlobalReceiver(Atlases.MSG_CLIENT_UPDATE_ATLAS_INVENTORY, this::handleUpdateAtlasInventory);

        if (Atlases.enableKeybind) {
            keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charm.openAtlas",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.inventory"
            ));

            ClientTickEvents.END_WORLD_TICK.register(level -> {
                if (keyBinding == null || level == null) return;
                while (keyBinding.consumeClick()) {
                    NetworkHelper.sendPacketToServer(Atlases.MSG_SERVER_SWAP_ATLAS, buf -> buf.writeInt(swappedSlot));
                }
            });
        }
    }

    private void handleSwappedSlot(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        int swappedSlot = buffer.readInt();
        client.execute(() -> this.swappedSlot = swappedSlot);
    }

    private void handleUpdateAtlasInventory(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        int atlasSlot = buffer.readInt();
        client.execute(() -> updateInventory(atlasSlot));
    }

    public InteractionResult handleRenderItem(float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack itemStack, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        if (itemStack.getItem() == Atlases.ATLAS_ITEM) {
            if (renderer == null) {
                renderer = new AtlasRenderer();
            }
            renderer.renderAtlas(matrices, vertexConsumers, light, hand, equipProgress, swingProgress, itemStack);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public void handleItemTooltip(ItemStack stack, TooltipFlag context, List<Component> lines) {
        if (stack == null || stack.isEmpty() || stack.getItem() != Atlases.ATLAS_ITEM) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        AtlasInventory inventory = Atlases.getInventory(player.level, stack);

        ItemStack map = inventory.getLastActiveMapItem();
        if (map == null) return;

        lines.add(new TextComponent("Scale " + inventory.getScale()).withStyle(ChatFormatting.GRAY));

        MutableComponent name = map.hasCustomHoverName() ? map.getHoverName().plainCopy()
            : map.getHoverName().plainCopy().append(new TextComponent(" #" + MapItem.getMapId(map)));
        lines.add(name.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public static void updateInventory(int atlasSlot) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack atlas = player.getInventory().getItem(atlasSlot);
        Atlases.getInventory(mc.level, atlas).reload(atlas);
    }

    public static boolean shouldDrawAtlasCopy(CartographyTableScreen screen) {
        return Charm.LOADER.isEnabled(Atlases.class) && screen.getMenu().getSlot(0).getItem().getItem() == Atlases.ATLAS_ITEM
            && screen.getMenu().getSlot(1).getItem().getItem() == Items.MAP;
    }
}

