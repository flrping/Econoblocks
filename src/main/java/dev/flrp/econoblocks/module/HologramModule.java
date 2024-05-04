package dev.flrp.econoblocks.module;

import com.google.inject.AbstractModule;
import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.espresso.hook.hologram.DecentHologramsHologramProvider;
import dev.flrp.espresso.hook.hologram.HologramProvider;
import dev.flrp.espresso.hook.hologram.HologramType;

public class HologramModule extends AbstractModule {

    private final Econoblocks plugin;

    public HologramModule(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Econoblocks.class).toInstance(plugin);

        bind(HologramProvider.class).toProvider(() -> {
            HologramType hologramType = plugin.getConfig().contains("hologram") ? HologramType.valueOf(plugin.getConfig().getString("hologram")) : HologramType.NONE;
            switch(hologramType) {
                case DECENT_HOLOGRAMS:
                    Locale.log("Hooking into HolographicDisplays.");
                    return new DecentHologramsHologramProvider();
                default:
                    Locale.log("No hologram plugin found.");
                    return null;
            }
        });
    }

}
