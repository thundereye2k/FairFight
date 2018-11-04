package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class BadPacketsD extends PacketCheck {

    private boolean sent;

    public BadPacketsD(PlayerData playerData) {
        super(playerData, "Packets (Check 4)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInEntityAction) {
            final PacketPlayInEntityAction.EnumPlayerAction playerAction = ((PacketPlayInEntityAction) packet).b();
            if (playerAction == PacketPlayInEntityAction.EnumPlayerAction.START_SNEAKING || playerAction ==
                    PacketPlayInEntityAction.EnumPlayerAction.STOP_SNEAKING) {
                if (this.sent) {
                    if (this.alert(AlertType.RELEASE, player, "", true)) {
                        final int violations = this.playerData.getViolations(this, 60000L);
                        if (!this.playerData.isBanning() && violations > 2) {
                            this.ban(player);
                        }
                    }
                } else {
                    this.sent = true;
                }
            }
        } else if (packet instanceof PacketPlayInFlying) {
            this.sent = false;
        }
    }

}
