package me.joeleoli.fairfight.check.impl.wtap;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.*;

import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.LinkedList;

public class WTapB extends PacketCheck {

    private Deque<Integer> recentCounts;
    private boolean block;
    private int flyingCount;

    public WTapB(PlayerData playerData) {
        super(playerData, "Tap (Check 2)");

        this.recentCounts = new LinkedList<>();
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInEntityAction) {
            final PacketPlayInEntityAction.EnumPlayerAction playerAction = ((PacketPlayInEntityAction) packet).b();

            if (playerAction == PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING && this.playerData
                    .getLastAttackPacket() + 1000L > System.currentTimeMillis() && this.flyingCount < 10 && !this
                    .block) {
                this.recentCounts.add(this.flyingCount);

                if (this.recentCounts.size() == 20) {
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

                    if (stdDev < 0.3) {
                        if ((vl += 1.2) >= 2.4) {
                            this.alert(AlertType.EXPERIMENTAL, player, String.format("STD %.2f, VL %.2f", stdDev, vl), false);
                        }
                    } else {
                        vl -= 2.0;
                    }
                    this.setVl(vl);
                    this.recentCounts.clear();
                }
            }
        } else if (packet instanceof PacketPlayInUseEntity && ((PacketPlayInUseEntity) packet).a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
            this.flyingCount = 0;
        } else if (packet instanceof PacketPlayInFlying) {
            ++this.flyingCount;

            this.block = false;
        } else if (packet instanceof PacketPlayInBlockPlace) {
            this.block = true;
        }
    }

}
