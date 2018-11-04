package me.joeleoli.fairfight.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import me.joeleoli.fairfight.FairFight;
import me.joeleoli.fairfight.event.BungeeReceivedEvent;
import me.joeleoli.fairfight.event.ModListRetrieveEvent;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@RequiredArgsConstructor
public class BungeeListener implements PluginMessageListener {

	private final FairFight plugin;

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}

		final ByteArrayDataInput in = ByteStreams.newDataInput(message);
		final String subChannel = in.readUTF();

		if (subChannel.equals("ForgeMods")) {
			try {
				Map<String, String> mods = (Map<String, String>) new JSONParser().parse(in.readUTF());
				ModListRetrieveEvent event = new ModListRetrieveEvent(player, mods);

				this.plugin.getServer().getPluginManager().callEvent(event);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			return;
		}

		final short len = in.readShort();
		final byte[] messageBytes = new byte[len];

		in.readFully(messageBytes);

		ByteArrayDataInput dis = ByteStreams.newDataInput(messageBytes);
		String data = dis.readUTF();
		Long systemTime = Long.parseLong(data.split(":")[0]);

		final BungeeReceivedEvent event = new BungeeReceivedEvent(player, subChannel, data.replace(systemTime + ":", ""), message, systemTime > System.currentTimeMillis());

		this.plugin.getServer().getPluginManager().callEvent(event);
	}

}
