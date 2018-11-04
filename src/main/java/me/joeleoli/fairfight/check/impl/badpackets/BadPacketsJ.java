package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;

import org.bukkit.entity.Player;

public class BadPacketsJ extends PacketCheck {

    private boolean placing;

    public BadPacketsJ(PlayerData playerData) {
        super(playerData, "Packets (Check 10)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockDig) {
            if (((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.RELEASE_USE_ITEM) {
                if (!this.placing && this.alert(AlertType.RELEASE, player, "", true)) {
                    final int violations = this.playerData.getViolations(this, 60000L);
                    if (!this.playerData.isBanning() && violations > 2) {
                        this.ban(player);
                    }
                }
                this.placing = false;
            }
        } else if (packet instanceof PacketPlayInBlockPlace) {
            this.placing = true;
        }
    }

}
