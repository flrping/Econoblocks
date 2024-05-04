package dev.flrp.econoblocks.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.espresso.hook.economy.EconomyProvider;
import dev.flrp.espresso.hook.economy.PlayerPointsEconomyProvider;
import dev.flrp.espresso.hook.economy.TokenManagerEconomyProvider;
import dev.flrp.espresso.hook.economy.VaultEconomyProvider;
import org.bukkit.Bukkit;

public class EconomyModule extends AbstractModule {

    private final Econoblocks plugin;

    public EconomyModule(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Econoblocks.class).toInstance(plugin);
        Multibinder<EconomyProvider> economyProviderMultibinder = Multibinder.newSetBinder(binder(), EconomyProvider.class);
        if(Bukkit.getPluginManager().isPluginEnabled("TokenManager")) {
            Locale.log("Hooking into TokenManager.");
            economyProviderMultibinder.addBinding().to(TokenManagerEconomyProvider.class);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            Locale.log("Hooking into PlayerPoints.");
            economyProviderMultibinder.addBinding().to(PlayerPointsEconomyProvider.class);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Locale.log("Hooking into Vault.");
            economyProviderMultibinder.addBinding().to(VaultEconomyProvider.class);
        }
    }

}
