package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;

import org.bukkit.entity.Player;

public class BadPacketsG extends PacketCheck {

    private PacketPlayInEntityAction.EnumPlayerAction lastAction;

    public BadPacketsG(PlayerData playerData) {
        super(playerData, "Packets (Check 7)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInEntityAction) {
            final PacketPlayInEntityAction.EnumPlayerAction playerAction = ((PacketPlayInEntityAction) packet).b();

            if (playerAction == PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING || playerAction == PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING) {
                if (this.lastAction == playerAction && this.playerData.getLastAttackPacket() + 10000L > System.currentTimeMillis() && this.alert(AlertType.RELEASE, player, "", true)) {
                    final int violations = this.playerData.getViolations(this, 60000L);

                    if (!this.playerData.isBanning() && violations > 2) {
                        this.ban(player);
                    }
                }

                this.lastAction = playerAction;
            }
        }
    }

}
