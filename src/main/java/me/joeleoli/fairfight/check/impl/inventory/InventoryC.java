package me.joeleoli.fairfight.check.impl.inventory;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.CustomLocation;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;
import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.LinkedList;

public class InventoryC extends PacketCheck {

    private final Deque<Long> delays;

    public InventoryC(PlayerData playerData) {
        super(playerData, "Inventory (Check 3)");

        this.delays = new LinkedList<>();
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInWindowClick && System.currentTimeMillis() - this.playerData.getLastDelayedMovePacket() > 220L && !this.playerData.isAllowTeleport()) {
            final CustomLocation lastMovePacket = this.playerData.getLastMovePacket();

            if (lastMovePacket == null) {
                return;
            }

            final long delay = System.currentTimeMillis() - lastMovePacket.getTimestamp();

            this.delays.add(delay);

            if (this.delays.size() == 10) {
                double average = 0.0;

                for (final long loopDelay : this.delays) {
                    average += loopDelay;
                }

                average /= this.delays.size();

                this.delays.clear();

                double vl = this.getVl();

                if (average <= 35.0) {
                    if ((vl += 1.25) >= 4.0) {
                        if (this.alert(AlertType.RELEASE, player, String.format("AVG %.1f. VL %.2f.", average, vl), true)) {
                            if (!this.playerData.isBanning() && !this.playerData.isRandomBan() && vl >= 10.0) {
                                this.randomBan(player, 100.0);
                            }
                        } else {
                            vl = 0.0;
                        }
                    }
                } else {
                    vl -= 0.5;
                }
                this.setVl(vl);
            }
        }
    }

}
