package svenhjol.charm.base.handler;

import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmLoader;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.StringHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.LoadWorldCallback;
import svenhjol.charm.event.StructureSetupCallback;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class ModuleHandler {
    public static Map<String, CharmModule> LOADED_MODULES = new TreeMap<>();
    public static final Map<String, List<Class<? extends CharmModule>>> AVAILABLE_MODULES = new HashMap<>();
    public static final List<Class<? extends CharmModule>> ENABLED_MODULES = new ArrayList<>(); // this is a cache of enabled classes
    private static Map<String, CharmLoader> INSTANCES = new HashMap<>();

    public static ModuleHandler INSTANCE = new ModuleHandler();

    private ModuleHandler() {
        BiomeHandler.init();

        // allow modules to modify structures via an event
        StructureSetupCallback.EVENT.invoker().interact();

        // listen for server world loading events
        LoadWorldCallback.EVENT.register(server -> {
            DecorationHandler.init(); // load late so that tags are populated at this point
        });
    }

    public void register(CharmModule module) {
        LOADED_MODULES.put(module.getName(), module);

        Charm.LOG.info("Registering module " + module.getName() + " (enabled: " + (module.enabled ? "yes" : "no") + ")");
        module.register();
    }

    public void depends(CharmModule module) {
        String name = module.getName();
        boolean isEnabled = module.enabled;
        boolean dependencyCheck = module.depends();

        String message;
        if (!isEnabled) {
            message = "Module " + name + " is not enabled.";
        } else if (!dependencyCheck) {
            message = "Module " + name + " did not pass dependency check, disabling.";
        } else {
            message = "Module " + name + " is enabled.";
        }

        Charm.LOG.debug("[ModuleHandler] " + message);
        module.enabled = isEnabled && dependencyCheck;
    }

    public void init(CharmModule module) {
        // this is a cache for quick lookup of enabled classes
        ModuleHandler.ENABLED_MODULES.add(module.getClass());

        Charm.LOG.info("Initialising module " + module.getName());
        module.init();
    }

    @Nullable
    public static CharmModule getModule(String moduleName) {
        return LOADED_MODULES.getOrDefault(StringHelper.snakeToUpperCamel(moduleName), null);
    }

    public static Map<String, CharmModule> getLoadedModules() {
        return LOADED_MODULES;
    }

    /**
     * Use this within static hook methods for quick check if a module is enabled.
     * @param clazz Module to check
     * @return True if the module is enabled
     */
    public static boolean enabled(Class<? extends CharmModule> clazz) {
        return ENABLED_MODULES.contains(clazz);
    }

    /**
     * Use this anywhere to check a module's enabled status for any Charm-based (or Quark) module.
     * @param moduleName Name (modid:module_name) of module to check
     * @return True if the module is enabled
     */
    public static boolean enabled(String moduleName) {
        String[] split = moduleName.split(":");
        String modName = split[0]; // TODO: check module is running
        String modModule = split[1];

        CharmModule module = getModule(modModule);
        return module != null && module.enabled;
    }
}
