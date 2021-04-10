package dev.flrp.econoblocks.utils;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Methods {

    private static final Econoblocks instance = Econoblocks.getInstance();

    public static ItemStack itemInHand(Player player) {
        if(instance.getServer().getVersion().contains("1.8")) {
            return player.getItemInHand();
        }
        return player.getInventory().getItemInMainHand();
    }

}
