package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.event.player.AlertType;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

import me.joeleoli.fairfight.player.PlayerData;

import java.util.Deque;
import java.util.LinkedList;

public class AutoClickerF extends PacketCheck {

    private final Deque<Integer> recentCounts;
    private BlockPosition lastBlock;
    private int flyingCount;

    public AutoClickerF(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 6)");
        this.recentCounts = new LinkedList<>();
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockDig) {
            final PacketPlayInBlockDig blockDig = (PacketPlayInBlockDig) packet;
            if (blockDig.c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                if (this.lastBlock != null && this.lastBlock.equals(blockDig.a())) {
                    double vl = this.getVl();
                    this.recentCounts.addLast(this.flyingCount);
                    if (this.recentCounts.size() == 20) {
                        double average = 0.0;
                        for (final int i : this.recentCounts) {
                            average += i;
                        }
                        average /= this.recentCounts.size();
                        double stdDev = 0.0;
                        for (final int j : this.recentCounts) {
                            stdDev += Math.pow(j - average, 2.0);
                        }
                        stdDev /= this.recentCounts.size();
                        stdDev = Math.sqrt(stdDev);
                        if (stdDev < 0.45 && ++vl >= 3.0) {
                            if (this.alert(AlertType.RELEASE, player, String.format("STD %.2f. VL " +
                                    "%.1f.", stdDev, vl), false) && !this.playerData.isBanning() && !this.playerData
                                    .isRandomBan() && vl >= 6.0) {
                                this.randomBan(player, 200.0);
                            }
                        } else {
                            vl -= 0.5;
                        }
                        this.recentCounts.clear();
                    }
                    this.setVl(vl);
                }
                this.flyingCount = 0;
            } else if (blockDig.c() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                this.lastBlock = blockDig.a();
            }
        } else if (packet instanceof PacketPlayInFlying) {
            ++this.flyingCount;
        }
    }

}
