package me.joeleoli.fairfight.check.checks;

import me.joeleoli.fairfight.check.AbstractCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.util.update.PositionUpdate;

public abstract class PositionCheck extends AbstractCheck<PositionUpdate> {

    public PositionCheck(PlayerData playerData, String name) {
        super(playerData, PositionUpdate.class, name);
    }

}
