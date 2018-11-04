package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

import org.bukkit.entity.Player;

public class KillAuraR extends PacketCheck {

    private boolean sentUseEntity;

    public KillAuraR(PlayerData playerData) {
        super(playerData, "Kill-Aura (Check 18)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockPlace) {
            if (((PacketPlayInBlockPlace) packet).getFace() != 255 && this.sentUseEntity && this.alert(AlertType.RELEASE, player, "", true)) {
                final int violations = this.playerData.getViolations(this, 60000L);

                if (!this.playerData.isBanning() && violations > 2) {
                    this.ban(player);
                }
            }
        } else if (packet instanceof PacketPlayInUseEntity) {
            this.sentUseEntity = true;
        } else if (packet instanceof PacketPlayInFlying) {
            this.sentUseEntity = false;
        }
    }

}
