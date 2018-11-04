package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInHeldItemSlot;

import org.bukkit.entity.Player;

public class BadPacketsH extends PacketCheck {

    private int lastSlot;

    public BadPacketsH(PlayerData playerData) {
        super(playerData, "Packets (Check 8)");

        this.lastSlot = -1;
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInHeldItemSlot) {
            final int slot = ((PacketPlayInHeldItemSlot) packet).a();

            if (this.lastSlot == slot && this.alert(AlertType.RELEASE, player, "", true)) {
                final int violations = this.playerData.getViolations(this, 60000L);

                if (!this.playerData.isBanning() && violations > 2) {
                    this.ban(player);
                }
            }

            this.lastSlot = slot;
        }
    }

}
