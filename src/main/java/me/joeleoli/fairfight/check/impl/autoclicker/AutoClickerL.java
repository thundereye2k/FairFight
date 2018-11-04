package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class AutoClickerL extends PacketCheck {

    private int movements;
    private int failed;
    private int passed;
    private int stage;

    public AutoClickerL(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 12)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (System.currentTimeMillis() - this.playerData.getLastDelayedMovePacket() > 220L && this.playerData.getLastMovePacket() != null && System.currentTimeMillis() - this.playerData.getLastMovePacket().getTimestamp() < 110L) {
            if (packet instanceof PacketPlayInArmAnimation) {
                if (this.stage == 0 || this.stage == 1) {
                    ++this.stage;
                } else {
                    this.stage = 1;
                }
            } else if (packet instanceof PacketPlayInFlying) {
                if (this.stage == 2) {
                    ++this.stage;
                } else {
                    this.stage = 0;
                }
                ++this.movements;
            } else if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                if (this.stage == 3) {
                    ++this.failed;
                } else {
                    ++this.passed;
                }
                if (this.movements >= 200 && this.failed + this.passed > 60) {
                    final double rat = (this.passed == 0) ? -1.0 : (this.failed / this.passed);
                    double vl = this.getVl();

                    if (rat > 2.5) {
                        if ((vl += 1.0 + (rat - 2.0) * 0.75) >= 4.0) {
                            this.alert(AlertType.EXPERIMENTAL, player, String.format("RAT %.2f. VL %.2f.", rat, vl), false);
                        }
                    } else {
                        vl -= 2.0;
                    }

                    this.setVl(vl);

                    final boolean failed = false;
                    this.movements = (failed ? 1 : 0);
                    this.passed = (failed ? 1 : 0);
                    this.failed = (failed ? 1 : 0);
                }
            }
        } else {
            this.stage = 0;
        }
    }

}
