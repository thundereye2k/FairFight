package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.entity.Player;

public class KillAuraG extends PacketCheck {

    private int stage;
    
    public KillAuraG(PlayerData playerData) {
        super(playerData, "Kill-Aura (Check 7)");
        this.stage = 0;
    }
    
    @Override
    public void handleCheck(Player player, Packet packet) {
        final int calculusStage = this.stage % 6;
        if (calculusStage == 0) {
            if (packet instanceof PacketPlayInArmAnimation) {
                ++this.stage;
            }
            else {
                this.stage = 0;
            }
        }
        else if (calculusStage == 1) {
            if (packet instanceof PacketPlayInUseEntity) {
                ++this.stage;
            }
            else {
                this.stage = 0;
            }
        }
        else if (calculusStage == 2) {
            if (packet instanceof PacketPlayInEntityAction) {
                ++this.stage;
            }
            else {
                this.stage = 0;
            }
        }
        else if (calculusStage == 3) {
            if (packet instanceof PacketPlayInFlying) {
                ++this.stage;
            }
            else {
                this.stage = 0;
            }
        }
        else if (calculusStage == 4) {
            if (packet instanceof PacketPlayInEntityAction) {
                ++this.stage;
            }
            else {
                this.stage = 0;
            }
        }
        else if (calculusStage == 5) {
            if (packet instanceof PacketPlayInFlying) {
                if (++this.stage >= 30 && this.alert(AlertType.RELEASE, player, "S " + this.stage + ".", true)) {
                    final int violations = this.playerData.getViolations(this, 60000L);
                    if (!this.playerData.isBanning() && violations > 5) {
                        this.ban(player);
                    }
                }
            }
            else {
                this.stage = 0;
            }
        }
    }
}
