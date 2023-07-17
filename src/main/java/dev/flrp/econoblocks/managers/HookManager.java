package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.hooks.ItemsAdderHook;
import dev.flrp.econoblocks.hooks.VaultHook;

public class HookManager {

    private final Econoblocks plugin;

    public HookManager(Econoblocks plugin) {
        this.plugin = plugin;
        Locale.log("Starting to register hooks. Please wait.");
        load();
        Locale.log("Registering complete.");
    }

    private void load() {
        VaultHook.register();
        ItemsAdderHook.register();
    }

    public void reload() {
        ItemsAdderHook.reload();
    }

}
