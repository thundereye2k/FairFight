package me.joeleoli.fairfight.check.impl.fly;

import me.joeleoli.fairfight.check.checks.PositionCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.update.PositionUpdate;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FlyA extends PositionCheck {

    public FlyA(PlayerData playerData) {
        super(playerData, "Flight (Check 1)");
    }

    @Override
    public void handleCheck(final Player player, final PositionUpdate update) {
        int vl = (int) this.getVl();

        if (!this.playerData.isInLiquid() && !this.playerData.isOnGround() && this.playerData.getVelocityV() == 0) {
            if (update.getFrom().getY() >= update.getTo().getY()) {
                return;
            }

            final double distance = update.getTo().getY() - this.playerData.getLastGroundY();
            double limit = 2.0;

            if (player.hasPotionEffect(PotionEffectType.JUMP)) {
                for (final PotionEffect effect : player.getActivePotionEffects()) {
                    if (effect.getType().equals(PotionEffectType.JUMP)) {
                        final int level = effect.getAmplifier() + 1;
                        limit += Math.pow(level + 4.2, 2.0) / 16.0;
                        break;
                    }
                }
            }

            if (distance > limit) {
                if (++vl >= 10 && this.alert(AlertType.RELEASE, player, "VL " + vl + ".", true)) {
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
