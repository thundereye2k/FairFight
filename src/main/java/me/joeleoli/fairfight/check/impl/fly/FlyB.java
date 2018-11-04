package me.joeleoli.fairfight.check.impl.fly;

import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.update.PositionUpdate;
import org.bukkit.entity.Player;

import me.joeleoli.fairfight.check.checks.PositionCheck;

public class FlyB extends PositionCheck {

    public FlyB(PlayerData playerData) {
        super(playerData, "Flight (Check 2)");
    }
    
    @Override
    public void handleCheck(final Player player, final PositionUpdate update) {
        int vl = (int)this.getVl();

        if (!this.playerData.isInLiquid() && !this.playerData.isOnGround()) {
            final double offsetH = Math.hypot(update.getTo().getX() - update.getFrom().getX(), update.getTo().getZ() - update.getFrom().getZ());
            final double offsetY = update.getTo().getY() - update.getFrom().getY();

            if (offsetH > 0.0 && offsetY == 0.0) {
                if (++vl >= 10 && this.alert(AlertType.RELEASE, player, String.format("H %.2f. VL %s.", offsetH, vl), true)) {
                    final int violations = this.playerData.getViolations(this, 60000L);

                    if (!this.playerData.isBanning() && violations > 8) {
                        this.ban(player);
                    }
                }
            } else {
                vl = 0;
            }
        } else {
            vl = 0;
        }

        this.setVl(vl);
    }

}
