package me.joeleoli.fairfight.command;

import me.joeleoli.fairfight.FairFight;
import me.joeleoli.fairfight.player.PlayerData;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.player.PlayerInfo;
import me.joeleoli.nucleus.util.Style;

import org.bukkit.entity.Player;

public class Commands {

    @Command(names = {"fairfight logs", "ff logs"}, permissionNode = "fairfight.logs")
    public static void check(Player player, @Parameter(name = "target") PlayerInfo target) {

    }

    @Command(names = {"fairfight check", "ff check"}, permissionNode = "fairfight.check")
    public static void check(Player player, @Parameter(name = "filter") String filter) {
        boolean contains = FairFight.getInstance().getDisabledChecks().remove(filter.toUpperCase());

        if (contains) {
            player.sendMessage(Style.GREEN + "The filter `" + filter + "` has been removed.");
        } else {
            FairFight.getInstance().getDisabledChecks().add(filter.toUpperCase());
            player.sendMessage(Style.GREEN + "The filter `" + filter + "` has been added.");
        }
    }

    @Command(names = {"fairfight alerts", "ff alerts"}, permissionNode = "fairfight.alerts")
    public static void alerts(Player player) {
        boolean receiving = FairFight.getInstance().toggleAlerts(player);

        if (receiving) {
            player.sendMessage(Style.GREEN + "You enabled FairFight alerts.");
        } else {
            player.sendMessage(Style.RED + "You disabled FairFight alerts.");
        }
    }

    @Command(names = {"fairfight client", "ff client"}, permissionNode = "fairfight.client")
    public static void client(Player player, @Parameter(name = "target") Player target) {
        final PlayerData playerData = FairFight.getInstance().getPlayerDataManager().getPlayerData(target);

        if (playerData.getClient() != null) {
            player.sendMessage(Style.PINK + target.getName() + Style.YELLOW + " is on " + Style.PINK + playerData.getClient().getName());
        } else {
            player.sendMessage(Style.PINK + target.getName() + Style.YELLOW + " is on " + Style.PINK + "Unknown");
        }
    }

}
