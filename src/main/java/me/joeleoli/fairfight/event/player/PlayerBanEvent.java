package me.joeleoli.fairfight.event.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.*;
import org.bukkit.entity.*;

@Getter
public class PlayerBanEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST;

    static {
        HANDLER_LIST = new HandlerList();
    }

    private Player player;
    private String reason;
    @Setter
    private boolean cancelled;

    public PlayerBanEvent(final Player player, final String reason) {
        this.player = player;
        this.reason = reason;
    }
    
    public static HandlerList getHandlerList() {
        return PlayerBanEvent.HANDLER_LIST;
    }
    
    public HandlerList getHandlers() {
        return PlayerBanEvent.HANDLER_LIST;
    }

}
