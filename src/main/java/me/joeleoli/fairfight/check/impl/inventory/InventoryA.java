package me.joeleoli.fairfight.check.impl.inventory;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;

import org.bukkit.entity.Player;

public class InventoryA extends PacketCheck {
    public InventoryA(PlayerData playerData) {
        super(playerData, "Inventory (Check 1)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInWindowClick && ((PacketPlayInWindowClick) packet).a() == 0 && !this
                .playerData.isInventoryOpen()) {
            if (this.alert(AlertType.RELEASE, player, "", true)) {
                final int violations = this.playerData.getViolations(this, 60000L);
                if (!this.playerData.isBanning() && violations > 5) {
                    this.ban(player);
                }
            }
            this.playerData.setInventoryOpen(true);
        }
    }

}
