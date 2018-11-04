package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.*;

import org.bukkit.entity.Player;

public class BadPacketsL extends PacketCheck {

    private boolean sent;
    private boolean vehicle;

    public BadPacketsL(PlayerData playerData) {
        super(playerData, "Packets (Check 12)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInFlying) {
            if (this.sent) {
                this.alert(AlertType.EXPERIMENTAL, player, "", false);
            }

            final boolean b = false;
            this.vehicle = b;
            this.sent = b;
        } else if (packet instanceof PacketPlayInBlockPlace) {
            final PacketPlayInBlockPlace blockPlace = (PacketPlayInBlockPlace) packet;

            if (blockPlace.getFace() == 255) {
                final ItemStack itemStack = blockPlace.getItemStack();

                if (itemStack != null && itemStack.getName().toLowerCase().contains("sword") && this.playerData.isSprinting() && !this.vehicle) {
                    this.sent = true;
                }
            }
        } else if (packet instanceof PacketPlayInEntityAction && ((PacketPlayInEntityAction) packet).b() == PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING) {
            this.sent = false;
        } else if (packet instanceof PacketPlayInSteerVehicle) {
            this.vehicle = true;
        }
    }

}
