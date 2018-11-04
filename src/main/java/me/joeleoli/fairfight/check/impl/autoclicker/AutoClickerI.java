package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.LinkedList;

public class AutoClickerI extends PacketCheck {

    private final Deque<Integer> recentCounts;
    private int flyingCount;

    public AutoClickerI(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 9)");
        this.recentCounts = new LinkedList<>();
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.RELEASE_USE_ITEM) {
            if (this.flyingCount < 10 && this.playerData.getLastAnimationPacket() + 2000L > System.currentTimeMillis()) {
                this.recentCounts.add(this.flyingCount);
                if (this.recentCounts.size() == 100) {
                    double average = 0.0;
                    for (final double flyingCount : this.recentCounts) {
                        average += flyingCount;
                    }
                    average /= this.recentCounts.size();
                    double stdDev = 0.0;
                    for (final long l : this.recentCounts) {
                        stdDev += Math.pow(l - average, 2.0);
                    }
                    stdDev /= this.recentCounts.size();
                    stdDev = Math.sqrt(stdDev);
                    double vl = this.getVl();
                    if (stdDev < 0.2) {
                        if ((vl += 1.4) >= 4.0) {
                            this.alert(AlertType.EXPERIMENTAL, player, String.format("STD %.2f. VL %.2f.", stdDev,
                                    vl), false);
                        }
                    } else {
                        vl -= 0.8;
                    }
                    this.setVl(vl);
                    this.recentCounts.clear();
                }
            }
        } else if (packet instanceof PacketPlayInBlockPlace && ((PacketPlayInBlockPlace) packet).getItemStack() !=
                null && ((PacketPlayInBlockPlace) packet).getItemStack().getName().toLowerCase().contains("sword")) {
            this.flyingCount = 0;
        } else if (packet instanceof PacketPlayInFlying) {
            ++this.flyingCount;
        }
    }

}
