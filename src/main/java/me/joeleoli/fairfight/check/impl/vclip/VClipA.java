package me.joeleoli.fairfight.check.impl.vclip;

import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.update.PositionUpdate;
import me.joeleoli.fairfight.check.checks.PositionCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.nucleus.util.BlockUtil;

import org.bukkit.entity.Player;

public class VClipA extends PositionCheck {

    public VClipA(PlayerData playerData) {
        super(playerData, "V-Clip (Check 1)");
    }
    
    @Override
    public void handleCheck(final Player player, final PositionUpdate update) {
        double difference = update.getTo().getY() - update.getFrom().getY();
        if (difference >= 2.0 && !BlockUtil.isBlockFaceAir(player)) {
            player.teleport(update.getFrom());
            this.alert(AlertType.RELEASE, player, "", true);
        }
    }

}
