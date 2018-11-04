package me.joeleoli.fairfight.check.impl.autoclicker;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.entity.Player;

public class AutoClickerK extends PacketCheck {

    private int stage;
    private boolean other;
    
    public AutoClickerK(PlayerData playerData) {
        super(playerData, "Auto-Clicker (Check 11)");
    }
    
    @Override
    public void handleCheck(Player player, Packet packet) {
        if (this.stage == 0) {
            if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig)packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                ++this.stage;
            }
        } else if (this.stage == 1) {
            if (packet instanceof PacketPlayInArmAnimation) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 2) {
            if (packet instanceof PacketPlayInFlying) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 3) {
            if (packet instanceof PacketPlayInArmAnimation) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 4) {
            if (packet instanceof PacketPlayInFlying) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 5) {
            if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig)packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                if (this.alert(AlertType.EXPERIMENTAL, player, "", false)) {
                    this.checkBan(player);
                }

                this.stage = 0;
            } else if (packet instanceof PacketPlayInArmAnimation) {
                ++this.stage;
            } else if (packet instanceof PacketPlayInFlying) {
                this.other = true;
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 6) {
            if (!this.other) {
                if (packet instanceof PacketPlayInFlying) {
                    ++this.stage;
                } else {
                    this.stage = 0;
                }
            } else {
                if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig)packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                    if (this.alert(AlertType.EXPERIMENTAL, player, "Type B.", false)) {
                        this.checkBan(player);
                    }

                    this.other = false;
                }

                this.stage = 0;
            }
        } else if (this.stage == 7) {
            if (packet instanceof PacketPlayInBlockDig && ((PacketPlayInBlockDig)packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                if (this.alert(AlertType.EXPERIMENTAL, player, "Type C.", false)) {
                    this.checkBan(player);
                }
            } else {
                this.stage = 0;
            }
        }
    }
    
    private void checkBan(final Player player) {
        final int violations = this.playerData.getViolations(this, 60000L);

        if (!this.playerData.isBanning() && violations > 2) {
            this.ban(player);
        }
    }

}
