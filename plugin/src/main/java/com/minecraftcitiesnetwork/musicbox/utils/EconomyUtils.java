package com.minecraftcitiesnetwork.musicbox.utils;

import lombok.experimental.UtilityClass;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.minecraftcitiesnetwork.musicbox.Lang;
import com.minecraftcitiesnetwork.musicbox.MusicBox;

@UtilityClass
public class EconomyUtils {
    private Economy provider() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return null;
        var registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    public boolean isEnable() {
        return MusicBox.getInstance().getConfigObject() != null
                && MusicBox.getInstance().getConfigObject().getEconomy().isEnable()
                && provider() != null;
    }

    public double getDiscPrice() {
        return MusicBox.getInstance().getConfigObject().getEconomy().getPrice();
    }

    public double getBalance(Player player) {
        Economy eco = provider();
        return isEnable() && eco != null ? eco.getBalance(player) : 0;
    }

    public boolean hasMoney(Player player, double money) {
        if (!isEnable()) return false;
        double currentMoney = getBalance(player);
        return (currentMoney - money) >= 0;
    }

    public boolean buyNoMessage(Player player, double price) {
        Economy eco = provider();
        return isEnable() && eco != null && eco.withdrawPlayer(player, price).transactionSuccess();
    }


    public boolean canBuy(Player player, double price) {
        if (!isEnable()) return false;
        double currentMoney = getBalance(player);
        double moneyLeft = currentMoney - price;
        if (moneyLeft >= 0) {
            return true;
        } else {
            moneyLeft *= -1;
            player.sendMessage(Lang.NO_HAS_MONEY.toString("{amount}", String.valueOf(moneyLeft)));
            return false;
        }
    }
}
