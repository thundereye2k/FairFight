package me.joeleoli.fairfight.check.impl.inventory;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class InventoryD extends PacketCheck {

    private int stage;
    
    public InventoryD(PlayerData playerData) {
        super(playerData, "Inventory (Check 4)");
        this.stage = 0;
    }
    
    @Override
    public void handleCheck(Player player, Packet packet) {
        if (this.stage == 0) {
            if (packet instanceof PacketPlayInClientCommand && ((PacketPlayInClientCommand)packet).a() == PacketPlayInClientCommand.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                ++this.stage;
            }
        } else if (this.stage == 1) {
            if (packet instanceof PacketPlayInFlying.PacketPlayInLook) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 2) {
            if (packet instanceof PacketPlayInFlying.PacketPlayInLook) {
                this.alert(AlertType.EXPERIMENTAL, player, "", false);
            }

            this.stage = 0;
        }
    }

}
