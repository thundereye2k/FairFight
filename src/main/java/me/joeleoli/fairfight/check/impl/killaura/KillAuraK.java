package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

import org.bukkit.entity.Player;

public class KillAuraK extends PacketCheck {

    private int ticksSinceStage;
    private int streak;
    private int stage;

    public KillAuraK(PlayerData playerData) {
        super(playerData, "Kill-Aura (Check 11)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInArmAnimation) {
            if (this.stage == 0) {
                this.stage = 1;
            } else {
                final boolean b = false;
                this.stage = (b ? 1 : 0);
                this.streak = (b ? 1 : 0);
            }
        } else if (packet instanceof PacketPlayInUseEntity) {
            if (this.stage == 1) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (packet instanceof PacketPlayInFlying.PacketPlayInPositionLook) {
            if (this.stage == 2) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (packet instanceof PacketPlayInFlying.PacketPlayInPosition) {
            if (this.stage == 3) {
                if (++this.streak >= 15) {
                    this.alert(AlertType.EXPERIMENTAL, player, "STR " + this.streak + ".", false);
                }

                this.ticksSinceStage = 0;
            }

            this.stage = 0;
        }

        if (packet instanceof PacketPlayInFlying && ++this.ticksSinceStage > 40) {
            this.streak = 0;
        }
    }

}
