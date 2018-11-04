package me.joeleoli.fairfight.check.impl.scaffold;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.CustomLocation;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.entity.Player;

public class ScaffoldB extends PacketCheck {

    private long lastPlace;
    private boolean place;

    public ScaffoldB(PlayerData playerData) {
        super(playerData, "Placement (Check 2)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        double vl = this.getVl();
        if (packet instanceof PacketPlayInBlockPlace && System.currentTimeMillis() - this.playerData
                .getLastDelayedMovePacket() > 220L && !this.playerData.isAllowTeleport()) {
            final CustomLocation lastMovePacket = this.playerData.getLastMovePacket();
            if (lastMovePacket == null) {
                return;
            }
            final long delay = System.currentTimeMillis() - lastMovePacket.getTimestamp();
            if (delay <= 25.0) {
                this.lastPlace = System.currentTimeMillis();
                this.place = true;
            } else {
                vl -= 0.25;
            }
        } else if (packet instanceof PacketPlayInFlying && this.place) {
            final long time = System.currentTimeMillis() - this.lastPlace;
            if (time >= 25L) {
                if (++vl >= 10.0) {
                    this.alert(AlertType.EXPERIMENTAL, player, String.format("T %s. VL %.2f.", time, vl), false);
                }
            } else {
                vl -= 0.25;
            }
            this.place = false;
        }
        this.setVl(vl);
    }

}
