package me.joeleoli.fairfight.check.impl.velocity;

import me.joeleoli.fairfight.check.checks.PositionCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.update.PositionUpdate;

import org.bukkit.entity.Player;

public class VelocityB extends PositionCheck {

    public VelocityB(PlayerData playerData) {
        super(playerData, "Velocity (Check 2)");
    }

    @Override
    public void handleCheck(final Player player, final PositionUpdate update) {
        final double offsetY = update.getTo().getY() - update.getFrom().getY();
        if (this.playerData.getVelocityY() > 0.0 && this.playerData.isWasOnGround() && !this.playerData.isUnderBlock
                () && !this.playerData.isWasUnderBlock() && !this.playerData.isInLiquid() && !this.playerData
                .isWasInLiquid() && !this.playerData.isInWeb() && !this.playerData.isWasInWeb() && !this.playerData
                .isOnStairs() && offsetY > 0.0 && offsetY < 0.41999998688697815 && update.getFrom().getY() % 1.0 ==
                0.0) {
            final double ratioY = offsetY / this.playerData.getVelocityY();
            int vl = (int) this.getVl();

            if (ratioY < 0.99) {
                final int percent = (int) Math.round(ratioY * 100.0);

                if (++vl >= 5 && this.alert(AlertType.RELEASE, player, "P " + percent + ". VL " + vl + ".", false) &&
                        !this.playerData.isBanning() && vl >= 15) {
                    this.ban(player);
                }
            } else {
                --vl;
            }

            this.setVl(vl);
        }
    }

}
