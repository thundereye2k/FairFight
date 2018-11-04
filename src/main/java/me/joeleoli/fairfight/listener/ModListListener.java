package me.joeleoli.fairfight.listener;

import me.joeleoli.fairfight.FairFight;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.ModListRetrieveEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class ModListListener implements Listener {
    
    @EventHandler
    public void onModListRetrieve(ModListRetrieveEvent event) {
        final Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        final PlayerData playerData = FairFight.getInstance().getPlayerDataManager().getPlayerData(player);

        if (playerData == null) {
            return;
        }

        final Map<String, String> mods = event.getMods();

        FairFight.getInstance().getClientManager().onModList(playerData, player, mods);
    }

}
