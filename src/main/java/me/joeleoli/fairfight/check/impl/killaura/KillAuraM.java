package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

import org.bukkit.entity.Player;

public class KillAuraM extends PacketCheck {

    private int swings;
    private int attacks;

    public KillAuraM(PlayerData playerData) {
        super(playerData, "Kill-Aura (Check 13)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (!this.playerData.isDigging() && !this.playerData.isPlacing()) {
            if (packet instanceof PacketPlayInFlying) {
                if (this.attacks > 0 && this.swings > this.attacks) {
                    this.alert(AlertType.EXPERIMENTAL, player, "S " + this.swings + ". A " + this.attacks + ".", false);
                }

                final KillAuraN auraN = this.playerData.getCheck(KillAuraN.class);

                if (auraN != null) {
                    auraN.handleCheck(player, new int[]{this.swings, this.attacks});
                }

                this.swings = 0;
                this.attacks = 0;
            } else if (packet instanceof PacketPlayInArmAnimation) {
                ++this.swings;
            } else if (packet instanceof PacketPlayInUseEntity && ((PacketPlayInUseEntity) packet).a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
                ++this.attacks;
            }
        }
    }

}
