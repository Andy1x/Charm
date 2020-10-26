package svenhjol.charm.module;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.ResourceLocation;
import svenhjol.charm.Charm;
import svenhjol.charm.client.InventoryButtonClient;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;

@Module(mod = Charm.MOD_ID, alwaysEnabled = true, description = "Core configuration values.")
public class Core extends CharmModule {
    public static final ResourceLocation MSG_SERVER_OPEN_INVENTORY = new ResourceLocation(Charm.MOD_ID, "server_open_inventory");

    @Config(name = "Debug mode", description = "If true, routes additional debug messages into the standard game log.")
    public static boolean debug = false;

    @Config(name = "Inventory button return", description = "If inventory crafting or inventory ender chest modules are enabled, pressing escape or inventory key returns you to the inventory rather than closing the window.")
    public static boolean inventoryButtonReturn = false;

    @Override
    public void clientRegister() {
        new InventoryButtonClient();

        // listen for network requests to open the player's inventory
        ClientSidePacketRegistry.INSTANCE.register(MSG_SERVER_OPEN_INVENTORY, (context, data) -> {
            context.getTaskQueue().execute(PlayerHelper::openInventory);
        });
    }
}
