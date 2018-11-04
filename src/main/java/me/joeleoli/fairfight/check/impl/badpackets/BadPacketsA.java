package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInSteerVehicle;

import org.bukkit.entity.Player;

public class BadPacketsA extends PacketCheck {

    private int streak;

    public BadPacketsA(PlayerData playerData) {
        super(playerData, "Packets (Check 1)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInFlying) {
            if (((PacketPlayInFlying) packet).g()) {
                this.streak = 0;
            } else if (++this.streak > 20 && this.alert(AlertType.RELEASE, player, "", false) && !this.playerData
                    .isBanning()) {
                this.ban(player);
            }
        } else if (packet instanceof PacketPlayInSteerVehicle) {
            this.streak = 0;
        }
    }

}
