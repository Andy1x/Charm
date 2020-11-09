package svenhjol.charm.base.handler;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Config;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigHandler {

    public static void createConfig(String mod, Map<String, CharmModule> modules) {
        Map<String, Map<String, Object>> finalConfig = new LinkedHashMap<>();

        String configName = mod.equals(Charm.MOD_ID) ? Charm.MOD_ID : Charm.MOD_ID + "-" + mod;
        String configPath = "./config/" + configName + ".toml";

        modules.forEach((moduleName, module) -> {
            finalConfig.put(moduleName, new LinkedHashMap<>());
            finalConfig.get(moduleName).put("Description", module.description);

            if (!module.alwaysEnabled)
                finalConfig.get(moduleName).put("Enabled", module.enabled);
        });

        // read config from disk and add to the config map
        try {
            Map<?, ?> loadedConfig = readConfig(Paths.get(configPath));

            for (Map.Entry<?, ?> entry : loadedConfig.entrySet()) {
                Object key = entry.getKey();
                if (!(key instanceof String))
                    continue;

                String moduleName = (String)key;
                if (!finalConfig.containsKey(moduleName))
                    continue;

                finalConfig.get(moduleName).putAll((HashMap)entry.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read config for " + configName, e);
        }

        // parse config and apply values to modules
        for (Map.Entry<String, CharmModule> entry : modules.entrySet()) {
            String moduleName = entry.getKey();
            CharmModule module = entry.getValue();

            // set module enabled/disabled
            module.enabled = (boolean)finalConfig.get(moduleName).getOrDefault("Enabled", module.enabledByDefault);

            // get and set module config options
            ArrayList<Field> fields = new ArrayList<>(Arrays.asList(module.getClass().getDeclaredFields()));
            fields.forEach(field -> {
                try {
                    Config annotation = field.getDeclaredAnnotation(Config.class);
                    if (annotation == null)
                        return;

                    field.setAccessible(true);
                    String name = annotation.name();

                    if (name.isEmpty())
                        name = field.getName();

                    Object val = field.get(null);

                    if (finalConfig.get(moduleName).containsKey(name)) {
                        Object configVal = finalConfig.get(moduleName).get(name);

                        if (val instanceof Integer && configVal instanceof Double)
                            configVal = (int)(double)configVal;  // this is stupidland

                        field.set(null, configVal);
                        finalConfig.get(moduleName).put(name, configVal);
                    } else {
                        finalConfig.get(moduleName).put(name, val);
                    }
                } catch (Exception e) {
                    Charm.LOG.error("Failed to set config for " + moduleName + ": " + e.getMessage());
                }
            });
        }

        // write out the config
        try {
            writeConfig(Paths.get(configPath), finalConfig);
        } catch (Exception e) {
            Charm.LOG.error("Failed to write config: " + e.getMessage());
        }
    }

    private static Map<?, ?> readConfig(Path path) throws IOException {
        touch(path);

        Toml toml = new Toml();
        Reader reader = Files.newBufferedReader(path);
        Map<?, ?> map = toml.read(reader).toMap();
        reader.close();
        return map;
    }

    private static void writeConfig(Path path, Map<?, ?> map) throws IOException {
        touch(path);

        TomlWriter toml = new TomlWriter();
        Writer writer = Files.newBufferedWriter(path);
        toml.write(map, writer);
        writer.close();
    }

    private static void touch(Path path) throws IOException {
        File file = path.toFile();

        if (file.exists())
            return;

        File dir = file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create config parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new IOException("Parent file is not a directory");
        }

        try (Writer writer = new FileWriter(file)) {
            writer.write("\n");
        }
    }
}
