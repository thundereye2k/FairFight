package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class AutoClickerD extends PacketCheck {

    private int movements;
    private int stage;

    public AutoClickerD(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 4)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        int vl = (int) this.getVl();

        if (this.stage == 0) {
            if (packet instanceof PacketPlayInArmAnimation) {
                ++this.stage;
            }
        } else if (this.stage == 1) {
            if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 2) {
            if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                if (++vl >= 5) {
                    try {
                        if (this.movements > 10 && this.alert(AlertType.RELEASE, player, "M " + this.movements + ".", true)) {
                            final int violations = this.playerData.getViolations(this, 60000L);

                            if (!this.playerData.isBanning() && !this.playerData.isRandomBan() && violations > 4) {
                                this.randomBan(player, 250.0);
                            }
                        }
                    } finally {
                        final boolean movements = false;
                        this.movements = (movements ? 1 : 0);
                        vl = (movements ? 1 : 0);
                    }
                }

                this.stage = 0;
            } else if (packet instanceof PacketPlayInArmAnimation) {
                ++this.stage;
            } else {
                final boolean b = false;
                this.movements = (b ? 1 : 0);
                vl = (b ? 1 : 0);
                this.stage = (b ? 1 : 0);
            }
        } else if (this.stage == 3) {
            if (packet instanceof PacketPlayInFlying) {
                ++this.stage;
            } else {
                final boolean b2 = false;
                this.movements = (b2 ? 1 : 0);
                vl = (b2 ? 1 : 0);
                this.stage = (b2 ? 1 : 0);
            }
        } else if (this.stage == 4) {
            if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                ++this.movements;

                this.stage = 0;
            } else {
                final boolean b3 = false;
                this.movements = (b3 ? 1 : 0);
                vl = (b3 ? 1 : 0);
                this.stage = (b3 ? 1 : 0);
            }
        }

        this.setVl(vl);
    }

}
