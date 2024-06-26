package dev.flrp.econoblocks.util;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final Econoblocks plugin;
    private final int resourceID;

    public UpdateChecker(Econoblocks plugin, int resourceID) {
        this.plugin = plugin;
        this.resourceID = resourceID;
    }

    public void checkForUpdate(final Consumer<String> consumer) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceID).openStream();
                Scanner scanner = new Scanner(inputStream);
                if (scanner.hasNext()) consumer.accept(scanner.next());
            } catch (IOException e) {
                Locale.log("Unable to check for updates: " + e.getMessage());
            }
        });
    }


}
