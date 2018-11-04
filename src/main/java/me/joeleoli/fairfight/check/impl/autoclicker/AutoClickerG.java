package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class AutoClickerG extends PacketCheck {

    private boolean failed;
    private boolean sent;

    public AutoClickerG(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 7)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockPlace && ((PacketPlayInBlockPlace) packet).getFace() == 255 && System
                .currentTimeMillis() - this.playerData.getLastDelayedMovePacket() > 220L && this.playerData
                .getLastMovePacket() != null && System.currentTimeMillis() - this.playerData.getLastMovePacket()
                .getTimestamp() < 110L && this.playerData.getLastAnimationPacket() + 1000L > System.currentTimeMillis
                ()) {
            if (this.sent) {
                if (!this.failed) {
                    this.alert(AlertType.EXPERIMENTAL, player, "", false);
                    this.failed = true;
                }
            } else {
                this.sent = true;
            }
        } else if (packet instanceof PacketPlayInFlying) {
            final boolean b = false;
            this.failed = b;
            this.sent = b;
        }
    }

}
