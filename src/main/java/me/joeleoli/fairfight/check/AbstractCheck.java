package me.joeleoli.fairfight.check;

import lombok.AllArgsConstructor;
import lombok.Getter;

import me.joeleoli.fairfight.FairFight;
import me.joeleoli.fairfight.event.player.AlertType;

import org.bukkit.entity.Player;

import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.PlayerAlertEvent;
import me.joeleoli.fairfight.event.player.PlayerBanEvent;

@AllArgsConstructor
@Getter
public abstract class AbstractCheck<T> implements ICheck<T> {

    protected final PlayerData playerData;
    private final Class<T> clazz;
    private final String name;

    @Override
    public Class<? extends T> getType() {
        return this.clazz;
    }

    protected FairFight getPlugin() {
        return FairFight.getInstance();
    }

    protected double getVl() {
        return this.playerData.getCheckVl(this);
    }

    protected void setVl(final double vl) {
        this.playerData.setCheckVl(vl, this);
    }

    protected boolean alert(AlertType alertType, Player player, String extra, boolean violation) {
        final PlayerAlertEvent event = new PlayerAlertEvent(alertType, player, this.name, extra);

        this.getPlugin().getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            if (violation) {
                this.playerData.addViolation(this);
            }

            return true;
        }

        return false;
    }

    protected boolean ban(final Player player) {
        this.playerData.setBanning(true);

        final PlayerBanEvent event = new PlayerBanEvent(player, this.name);

        this.getPlugin().getServer().getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    protected void randomBan(Player player, double rate) {
        this.playerData.setRandomBanRate(rate);
        this.playerData.setRandomBanReason(this.name);
        this.playerData.setRandomBan(true);

        this.getPlugin().getServer().getPluginManager().callEvent(new PlayerAlertEvent(AlertType.RELEASE, player, this.name, null));
    }

}
