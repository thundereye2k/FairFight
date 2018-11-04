package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.CustomLocation;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

import org.bukkit.entity.Player;

public class KillAuraE extends PacketCheck {

    private long lastAttack;
    private boolean attack;
    
    public KillAuraE(PlayerData playerData) {
        super(playerData, "Kill-Aura (Check 5)");
    }
    
    @Override
    public void handleCheck(Player player, Packet packet) {
        double vl = this.getVl();

        if (packet instanceof PacketPlayInUseEntity && ((PacketPlayInUseEntity)packet).a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK && System.currentTimeMillis() - this.playerData.getLastDelayedMovePacket() > 220L && !this.playerData.isAllowTeleport()) {
            final CustomLocation lastMovePacket = this.playerData.getLastMovePacket();

            if (lastMovePacket == null) {
                return;
            }

            final long delay = System.currentTimeMillis() - lastMovePacket.getTimestamp();

            if (delay <= 25.0) {
                this.lastAttack = System.currentTimeMillis();
                this.attack = true;
            } else {
                vl -= 0.25;
            }
        } else if (packet instanceof PacketPlayInFlying && this.attack) {
            final long time = System.currentTimeMillis() - this.lastAttack;

            if (time >= 25L) {
                if (++vl >= 10.0 && this.alert(AlertType.RELEASE, player, String.format("T %s. VL %.2f.", time, vl), false) && !this.playerData.isBanning() && vl >= 20.0) {
                    this.ban(player);
                }
            } else {
                vl -= 0.25;
            }

            this.attack = false;
        }

        this.setVl(vl);
    }

}
