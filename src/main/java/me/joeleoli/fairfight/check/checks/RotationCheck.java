package me.joeleoli.fairfight.check.checks;

import me.joeleoli.fairfight.check.AbstractCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.util.update.RotationUpdate;

public abstract class RotationCheck extends AbstractCheck<RotationUpdate> {

    public RotationCheck(PlayerData playerData, String name) {
        super(playerData, RotationUpdate.class, name);
    }

}
