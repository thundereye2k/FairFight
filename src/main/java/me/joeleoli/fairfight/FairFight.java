package me.joeleoli.fairfight;

import lombok.Getter;

import me.joeleoli.fairfight.client.ClientManager;
import me.joeleoli.fairfight.handler.CustomMovementHandler;
import me.joeleoli.fairfight.handler.CustomPacketHandler;
import me.joeleoli.fairfight.listener.BungeeListener;
import me.joeleoli.fairfight.manager.PlayerDataManager;
import me.joeleoli.fairfight.mongo.FairFightMongo;
import me.joeleoli.fairfight.task.InsertLogsTask;

import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.config.FileConfig;
import me.joeleoli.nucleus.listener.ListenerHandler;

import me.joeleoli.ragespigot.RageSpigot;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class FairFight extends JavaPlugin {

    @Getter
    private static FairFight instance;

    private FileConfig mainFileConfig;
    private FairFightMongo mongo;
    private PlayerDataManager playerDataManager;
    private ClientManager clientManager;
    private Set<UUID> receivingAlerts;
    private Set<String> disabledChecks;
    private double rangeVl;
    
    public FairFight() {
        this.rangeVl = 30.0;
    }
    
    public void onEnable() {
        instance = this;

        this.receivingAlerts = new HashSet<>();
        this.disabledChecks = new HashSet<>();

        this.mainFileConfig = new FileConfig(this, "config.yml");
        this.mongo = new FairFightMongo();

        RageSpigot.INSTANCE.addPacketHandler(new CustomPacketHandler(this));
        RageSpigot.INSTANCE.addMovementHandler(new CustomMovementHandler(this));

        CommandHandler.loadCommandsFromPackage(this, "me.joeleoli.fairfight.command");
        ListenerHandler.loadListenersFromPackage(this, "me.joeleoli.fairfight.listener");

        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener(this));
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.getServer().getScheduler().runTaskTimer(this, new InsertLogsTask(), 20L * 60L * 5L, 20L * 60L * 5L);

        this.playerDataManager = new PlayerDataManager();
        this.clientManager = new ClientManager();
    }
    
    public boolean isAntiCheatEnabled() {
        return MinecraftServer.getServer().tps1.getAverage() > 19.0 && MinecraftServer.LAST_TICK_TIME + 100L > System.currentTimeMillis();
    }

    public boolean canAlert(Player player) {
        return this.receivingAlerts.contains(player.getUniqueId());
    }

    public boolean toggleAlerts(Player player) {
        boolean current = this.receivingAlerts.remove(player.getUniqueId());

        if (!current) {
            this.receivingAlerts.add(player.getUniqueId());
        }

        return !current;
    }

}
