package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInSteerVehicle;

import org.bukkit.entity.Player;

public class BadPacketsI extends PacketCheck {

    private float lastYaw;
    private float lastPitch;
    private boolean ignore;

    public BadPacketsI(PlayerData playerData) {
        super(playerData, "Packets (Check 9)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInFlying) {
            final PacketPlayInFlying flying = (PacketPlayInFlying) packet;

            if (!flying.g() && flying.h()) {
                if (this.lastYaw == flying.d() && this.lastPitch == flying.e()) {
                    if (!this.ignore) {
                        this.alert(AlertType.EXPERIMENTAL, player, "", false);
                    }
                    this.ignore = false;
                }
                this.lastYaw = flying.d();
                this.lastPitch = flying.e();
            } else {
                this.ignore = true;
            }
        } else if (packet instanceof PacketPlayInSteerVehicle) {
            this.ignore = true;
        }
    }

}
