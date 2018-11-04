package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;

import org.bukkit.entity.Player;

public class AutoClickerC extends PacketCheck {

    private boolean sent;

    public AutoClickerC(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 3)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInBlockDig) {
            final PacketPlayInBlockDig.EnumPlayerDigType digType = ((PacketPlayInBlockDig) packet).c();

            if (digType == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                this.sent = true;
            } else if (digType == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                int vl = (int) this.getVl();

                if (this.sent) {
                    if (++vl > 10 && this.alert(AlertType.RELEASE, player, "VL " + vl + ".", false) && !this.playerData.isBanning() && !this.playerData.isRandomBan() && vl >= 20) {
                        this.randomBan(player, 250.0);
                    }
                } else {
                    vl = 0;
                }

                this.setVl(vl);
            }
        } else if (packet instanceof PacketPlayInArmAnimation) {
            this.sent = false;
        }
    }

}
