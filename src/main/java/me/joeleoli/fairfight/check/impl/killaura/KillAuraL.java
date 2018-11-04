package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

import org.bukkit.entity.Player;

public class KillAuraL extends PacketCheck {

    private boolean sent;

    public KillAuraL(PlayerData playerData) {
        super(playerData, "Kill-Aura (Check 12)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInUseEntity && ((PacketPlayInUseEntity) packet).a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
            if (this.sent && this.alert(AlertType.RELEASE, player, "", true)) {
                final int violations = this.playerData.getViolations(this, 60000L);

                if (!this.playerData.isBanning() && violations > 2) {
                    this.ban(player);
                }
            }
        } else if (packet instanceof PacketPlayInEntityAction) {
            final PacketPlayInEntityAction.EnumPlayerAction action = ((PacketPlayInEntityAction) packet).b();

            if (action == PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING || action == PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING || action == PacketPlayInEntityAction.EnumPlayerAction.START_SNEAKING || action == PacketPlayInEntityAction.EnumPlayerAction.STOP_SNEAKING) {
                this.sent = true;
            }
        } else if (packet instanceof PacketPlayInFlying) {
            this.sent = false;
        }
    }

}
