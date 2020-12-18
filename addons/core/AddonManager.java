package me.epicgodmc.prisoncore.addons.core;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import me.epicgodmc.prisoncore.PrisonCore;
import me.epicgodmc.prisoncore.addons.AddonCore;
import me.epicgodmc.prisoncore.addons.core.exceptions.InvalidAddonException;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
@ToString
public class AddonManager {

    private final static Pattern JAR_PATTERN = Pattern.compile("(.+?)(\\.jar)");

    private final File addonDir;
    private final List<Addon> addons = new ArrayList<>();
    private @NonNull final Map<@NonNull Addon, @NonNull List<Listener>> listeners = new HashMap<>();
    private boolean shutdown = false;


    public AddonManager(File addonDir) {
        Validate.notNull(addonDir, "Directory cannot be null");
        Validate.isTrue(addonDir.isDirectory(), "Directory must be a directory");
        this.addonDir = addonDir;
    }

    public Addon[] loadAddons() {
        return this.loadAddons(addonDir);
    }

    public void disableAddons()
    {
        this.shutdown = true;
        if (!this.addons.isEmpty())
        {
            AddonCore.getLogger().log(Level.INFO, "Disabling Addons...");
            this.addons.forEach(this::disableAddon);
            AddonCore.getLogger().log(Level.INFO, "Addons successfully disabled");
        }
        listeners.clear();
        this.addons.clear();
    }

    private void disableAddon(@NonNull Addon addon)
    {
        if (listeners.containsKey(addon))
        {
            listeners.get(addon).forEach(HandlerList::unregisterAll);
            listeners.remove(addon);
        }

        if (addon.isLoaded())
        {
            AddonCore.getLogger().log(Level.INFO, "Disabling Addon: "+addon.getName()+"...");
            try{
                addon.onDisable();
            }catch (Exception e)
            {
                AddonCore.getLogger().log(Level.SEVERE, "An Error occured while disabling addon");
                e.printStackTrace();
            }
        }
        addon.setLoaded(false);
        if (!shutdown)addons.remove(addon);
    }


    public Addon[] loadAddons(File directory) {
        Validate.notNull(directory, "Directory cannot be null");
        Validate.isTrue(directory.isDirectory(), "Directory must be a directory");

        List<Addon> result = new ArrayList<>();

        for (File file : directory.listFiles()) {
            if (!AddonManager.JAR_PATTERN.matcher(file.getName()).matches()) continue;

            try {
                result.add(this.loadAddon(file));
            } catch (InvalidAddonException e) {
                AddonCore.getLogger().log(Level.SEVERE,
                        "Cannot load '" + file.getName() + "' in folder '" + directory.getPath() + "': " + e.getMessage());
            }
        }
        return result.toArray(new Addon[result.size()]);
    }

    public synchronized Addon loadAddon(File file) throws InvalidAddonException {
        Addon result;

        if (!AddonManager.JAR_PATTERN.matcher(file.getName()).matches()) {
            throw new InvalidAddonException("File '" + file.getName() + "' is not a Jar File");
        }

        result = AddonLoader.loadAddon(file);
        if (result != null) {
            this.addons.add(result);
            Validate.notNull(result.getDescription());
            AddonCore.getLogger().log(Level.INFO, result.getName() + " [v." + result.getVersion() + " by " + result.getAuthor() + "] loaded");
        }

        return result;
    }

    public void registerListener(@NonNull Addon addon, @NonNull Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, PrisonCore.getInstance());
        listeners.computeIfAbsent(addon, k -> new ArrayList<>()).add(listener);
    }

    public Addon[] getAddons() {
        return this.addons.toArray(new Addon[0]);
    }
}
