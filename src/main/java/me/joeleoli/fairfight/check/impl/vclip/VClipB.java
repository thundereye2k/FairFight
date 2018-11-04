package me.joeleoli.fairfight.check.impl.vclip;

import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.update.PositionUpdate;
import me.joeleoli.nucleus.util.BlockUtil;
import org.bukkit.entity.Player;
import me.joeleoli.fairfight.check.checks.PositionCheck;

public class VClipB extends PositionCheck {

    public VClipB(PlayerData playerData) {
        super(playerData, "V-Clip (Check 2)");
    }
    
    @Override
    public void handleCheck(final Player player, final PositionUpdate update) {

        final double difference = update.getTo().getY() - update.getFrom().getY();

        if (difference >= 2.0 && BlockUtil.isSlab(player)) {
            player.teleport(update.getFrom());
            this.alert(AlertType.RELEASE, player, "", true);
        }
    }
}
