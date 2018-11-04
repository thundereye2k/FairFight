package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;

import org.bukkit.entity.Player;

public class BadPacketsE extends PacketCheck {

    public BadPacketsE(PlayerData playerData) {
        super(playerData, "Packets (Check 5)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.RELEASE_USE_ITEM && this.playerData.isPlacing() && this.alert(AlertType.RELEASE, player, "", true)) {
            final int violations = this.playerData.getViolations(this, 60000L);

            if (!this.playerData.isBanning() && violations > 2) {
                this.ban(player);
            }
        }
    }

}
