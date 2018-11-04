package me.joeleoli.fairfight.check.impl.timer;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.LinkedList;

public class TimerA extends PacketCheck {

    private final Deque<Long> delays;
    private long lastPacketTime;

    public TimerA(PlayerData playerData) {
        super(playerData, "Timer");
        this.delays = new LinkedList<>();
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInFlying && !this.playerData.isAllowTeleport() && System.currentTimeMillis()
                - this.playerData.getLastDelayedMovePacket() > 220L) {
            this.delays.add(System.currentTimeMillis() - this.lastPacketTime);
            if (this.delays.size() == 40) {
                double average = 0.0;
                for (final long l : this.delays) {
                    average += l;
                }
                average /= this.delays.size();
                double vl = this.getVl();
                if (average <= 49.0) {
                    if ((vl += 1.25) >= 4.0 && this.alert(AlertType.RELEASE, player, String.format("AVG %.3f. R %.2f." +
                            " VL %.2f.", average, 50.0 / average, vl), false) && !this.playerData.isBanning() && vl
                            >= 20.0) {
                        this.ban(player);
                    }
                } else {
                    vl -= 0.5;
                }
                this.setVl(vl);
                this.delays.clear();
            }
            this.lastPacketTime = System.currentTimeMillis();
        }
    }

}
