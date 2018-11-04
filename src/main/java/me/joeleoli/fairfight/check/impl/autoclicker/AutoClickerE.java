package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class AutoClickerE extends PacketCheck {

    private boolean failed;
    private boolean sent;
    private int count;

    public AutoClickerE(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 5)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInArmAnimation
                && (System.currentTimeMillis() - this.playerData.getLastDelayedMovePacket()) > 220L
                && (System.currentTimeMillis() - this.playerData.getLastMovePacket().getTimestamp()) < 110L
                && !this.playerData.isDigging() && !this.playerData.isPlacing() && !this.playerData.isFakeDigging()) {
            if (this.sent) {
                ++this.count;

                if (!this.failed) {
                    int vl = (int) this.getVl();

                    if (++vl >= 5) {
                        this.alert(AlertType.EXPERIMENTAL, player, "CO " + this.count + ".", false);
                        vl = 0;
                    }

                    this.setVl(vl);
                    this.failed = true;
                }
            } else {
                this.sent = true;
                this.count = 0;
            }
        } else if (packet instanceof PacketPlayInFlying) {
            final boolean b = false;
            this.failed = b;
            this.sent = b;
            this.count = 0;
        }
    }

}
