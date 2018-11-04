package me.joeleoli.fairfight.check.impl.step;

import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.update.PositionUpdate;
import org.bukkit.entity.Player;
import me.joeleoli.fairfight.check.checks.PositionCheck;

public class StepA extends PositionCheck {

	public StepA(PlayerData playerData) {
		super(playerData, "Step (Check 1)");
	}

	@Override
	public void handleCheck(final Player player, final PositionUpdate update) {
		double height = 0.9;
		double difference = update.getTo().getY() - update.getFrom().getY();

		if (difference > height) {
			this.alert(AlertType.EXPERIMENTAL, player, "", true);
		}
	}

}
