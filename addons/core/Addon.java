package me.epicgodmc.prisoncore.addons.core;

import lombok.*;
import me.epicgodmc.prisoncore.PrisonCore;
import me.epicgodmc.prisoncore.addons.AddonCore;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Level;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public abstract class Addon {

    private static final String ADDON_CONFIG_FILENAME = "config.yml";
    private final String sep = File.separator;
    private final String name, description, author, version;
    private File dataFolder;
    private File cfgFolder;
    private FileConfiguration config;
    private ClassLoader loader;
    private boolean loaded;


    public Addon(AddonInfo info) {
        this.name = info.name();
        this.description = info.description();
        this.author = info.author();
        this.version = info.version();
    }

    final void init(AddonClassLoader classLoader, File dataFolder) {
        this.dataFolder = dataFolder;
        this.cfgFolder = new File(String.format("plugins%sPrisonCore%sAddons%s%s", sep , sep, sep, this.name));
        this.cfgFolder = new File(PrisonCore.getInstance().getDataFolder().getPath() + File.separator + "Addons" + File.separator + this.name);
        this.loader = classLoader;
        this.createDataFolder();
        this.onEnable();
        this.loaded = true;
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public PrisonCore getCore() {
        return PrisonCore.getInstance();
    }

    public FileConfiguration getConfig() {
        if (config == null) config = loadYamlFile();
        return config;
    }

    public Server getServer() {
        return Bukkit.getServer();
    }

    public void registerListener(Listener listener) {
        PrisonCore.getInstance().getAddonManager().registerListener(this, listener);
    }

    private void createDataFolder() {
        if (!cfgFolder.exists()) {
            cfgFolder.mkdirs();
        }
    }

    private FileConfiguration loadYamlFile() {
        File yamlFile = new File(cfgFolder, ADDON_CONFIG_FILENAME);
        YamlConfiguration yamlConfiguration = null;

        if (!yamlFile.exists())
        {
            try{
                yamlFile.createNewFile();
            }catch (IOException e)
            {
                AddonCore.getLogger().log(Level.SEVERE, "Could not load " + ADDON_CONFIG_FILENAME + ": " + e.getMessage());
            }

        }

        try {
            yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.load(yamlFile);
        } catch (InvalidConfigurationException | IOException e) {
            AddonCore.getLogger().log(Level.SEVERE, "Could not load " + ADDON_CONFIG_FILENAME + ": " + e.getMessage());
        }
        return yamlConfiguration;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface AddonInfo {
        String name();

        String description();

        String author();

        String version() default "1.0";
    }
}
