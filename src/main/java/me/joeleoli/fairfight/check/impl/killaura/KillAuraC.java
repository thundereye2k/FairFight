package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.CustomLocation;
import me.joeleoli.fairfight.util.MathUtil;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class KillAuraC extends PacketCheck {

    private float lastYaw;

    public KillAuraC(PlayerData playerData) {
        super(playerData, "Kill-Aura (Check 3)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (this.playerData.getLastTarget() == null) {
            return;
        }

        if (packet instanceof PacketPlayInFlying) {
            final PacketPlayInFlying flying = (PacketPlayInFlying) packet;

            if (flying.h() && !this.playerData.isAllowTeleport()) {
                final CustomLocation targetLocation = this.playerData.getLastPlayerPacket(this.playerData.getLastTarget(), MathUtil.pingFormula(this.playerData.getPing()));

                if (targetLocation == null) {
                    return;
                }

                final CustomLocation playerLocation = this.playerData.getLastMovePacket();

                if (playerLocation.getX() == targetLocation.getX()) {
                    return;
                }

                if (targetLocation.getZ() == playerLocation.getZ()) {
                    return;
                }

                final float yaw = flying.d();

                if (yaw != this.lastYaw) {
                    final float bodyYaw = MathUtil.getDistanceBetweenAngles(yaw, MathUtil.getRotationFromPosition
                            (playerLocation, targetLocation)[0]);

                    if (bodyYaw == 0.0f && this.alert(AlertType.RELEASE, player, null, true)) {
                        final int violations = this.playerData.getViolations(this, 60000L);

                        if (!this.playerData.isBanning() && violations > 5) {
                            this.ban(player);
                        }
                    }
                }

                this.lastYaw = yaw;
            }
        }
    }

}
