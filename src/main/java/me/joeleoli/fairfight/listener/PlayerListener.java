package me.joeleoli.fairfight.listener;

import io.netty.buffer.Unpooled;
import java.text.MessageFormat;
import me.joeleoli.fairfight.FairFight;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.event.player.PlayerAlertEvent;
import me.joeleoli.fairfight.event.player.PlayerBanEvent;
import me.joeleoli.fairfight.mongo.FairFightLog;
import me.joeleoli.fairfight.mongo.FairFightMongo;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		FairFight.getInstance().getPlayerDataManager().addPlayerData(event.getPlayer());

		if (event.getPlayer().hasPermission("fairfight.alerts")) {
			FairFight.getInstance().getReceivingAlerts().add(event.getPlayer().getUniqueId());
		}

		FairFight.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(FairFight.getInstance(), () -> {
			final PlayerConnection playerConnection = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection;
			final PacketPlayOutCustomPayload packetPlayOutCustomPayload = new PacketPlayOutCustomPayload("REGISTER",
					new PacketDataSerializer(Unpooled.wrappedBuffer("CB-Client".getBytes()))
			);
			final PacketPlayOutCustomPayload packetPlayOutCustomPayload2 = new PacketPlayOutCustomPayload("REGISTER",
					new PacketDataSerializer(Unpooled.wrappedBuffer("CC".getBytes()))
			);

			playerConnection.sendPacket(packetPlayOutCustomPayload);
			playerConnection.sendPacket(packetPlayOutCustomPayload2);
		}, 10L);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		FairFight.getInstance().getReceivingAlerts().remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		final PlayerData playerData = FairFight.getInstance().getPlayerDataManager().getPlayerData(player);

		if (playerData != null) {
			playerData.setSendingVape(true);
		}
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		final PlayerData playerData = FairFight.getInstance().getPlayerDataManager().getPlayerData(player);

		if (playerData != null) {
			playerData.setInventoryOpen(false);
		}
	}

	@EventHandler
	public void onPlayerAlert(PlayerAlertEvent event) {
		if (!FairFight.getInstance().isAntiCheatEnabled()) {
			event.setCancelled(true);
			return;
		}

		final Player player = event.getPlayer();

		if (player == null) {
			return;
		}

		final PlayerData playerData = FairFight.getInstance().getPlayerDataManager().getPlayerData(player);

		if (playerData == null) {
			return;
		}

		final String extra = event.getExtra() == null ? "" : " (" + event.getExtra() + ")";
		final String message = Style.translate(new MessageFormat("&6&l[FF] {0} &ehas flagged &b{1} &7(Ping: {2}){3}")
				.format(new Object[]{
						NucleusAPI.getColoredName(player), event.getCheckName(), player.getPing(), extra
				}));

		if (event.getAlertType() == AlertType.RELEASE) {
			for (Player onlinePlayer : FairFight.getInstance().getServer().getOnlinePlayers()) {
				if (FairFight.getInstance().canAlert(onlinePlayer)) {
					onlinePlayer.sendMessage(message);
				}
			}
		}

		FairFightLog.getQueue().add(new FairFightLog(
				player.getUniqueId(),
				event.getCheckName(),
				playerData.getClient().getName(),
				player.getPing(),
				MinecraftServer.getServer().tps1.getAverage(),
				System.currentTimeMillis()
		));
	}

	@EventHandler
	public void onPlayerBan(PlayerBanEvent event) {
		if (!FairFight.getInstance().isAntiCheatEnabled()) {
			event.setCancelled(true);
			return;
		}

		final Player player = event.getPlayer();

		if (player == null) {
			return;
		}

		final String[] messages = new String[]{
				Style.getBorderLine(),
				Style.GOLD + Style.BOLD + " " + Style.UNICODE_CAUTION + " " + Style.PINK + player.getName() +
				Style.YELLOW + " has been banned by " + Style.PINK + Style.BOLD + "FairFight",
				Style.getBorderLine()
		};

		Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(messages));

		TaskUtil.run(() -> {
			FairFight.getInstance().getServer().dispatchCommand(
					FairFight.getInstance().getServer().getConsoleSender(),
					"ban " + player.getName() + " FairFight Ban -s"
			);
		});
	}

}
