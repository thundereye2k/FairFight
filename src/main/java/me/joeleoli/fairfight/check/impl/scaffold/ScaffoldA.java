package me.joeleoli.fairfight.check.impl.scaffold;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.CustomLocation;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;

import org.bukkit.entity.Player;

public class ScaffoldA extends PacketCheck {

    private BlockPosition lastBlock;
    private float lastYaw;
    private float lastPitch;
    private float lastX;
    private float lastY;
    private float lastZ;

    public ScaffoldA(PlayerData playerData) {
        super(playerData, "Placement (Check 1)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockPlace) {
            final PacketPlayInBlockPlace blockPlace = (PacketPlayInBlockPlace) packet;
            final BlockPosition blockPosition = blockPlace.a();
            final float x = blockPlace.d();
            final float y = blockPlace.e();
            final float z = blockPlace.f();

            if (this.lastBlock != null && (blockPosition.getX() != this.lastBlock.getX() || blockPosition.getY() != this.lastBlock.getY() || blockPosition.getZ() != this.lastBlock.getZ())) {
                final CustomLocation location = this.playerData.getLastMovePacket();
                double vl = this.getVl();

                if (this.lastX == x && this.lastY == y && this.lastZ == z) {
                    final float deltaAngle = Math.abs(this.lastYaw - location.getYaw()) + Math.abs(this.lastPitch - location.getPitch());

                    if (deltaAngle > 4.0f && ++vl >= 4.0) {
                        this.alert(AlertType.EXPERIMENTAL, player, String.format("X %.1f, Y %.1f, Z %.1f, DA %.1f, VL %.1f", x, y, z, deltaAngle, vl), false);
                    }
                } else {
                    vl -= 0.5;
                }

                this.setVl(vl);

                this.lastX = x;
                this.lastY = y;
                this.lastZ = z;
                this.lastYaw = location.getYaw();
                this.lastPitch = location.getPitch();
            }

            this.lastBlock = blockPosition;
        }
    }

}
