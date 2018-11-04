package me.joeleoli.fairfight.check.impl.badpackets;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;

import net.minecraft.server.v1_8_R3.*;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BadPacketsK extends PacketCheck {

    public BadPacketsK(PlayerData playerData) {
        super(playerData, "Packets (Check 11)");
    }

    @Override
    public void handleCheck(Player player, Packet packet) {
        if (packet instanceof PacketPlayInUseEntity) {
            final PacketPlayInUseEntity useEntity = (PacketPlayInUseEntity) packet;
            if (useEntity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
                final Entity targetEntity = useEntity.a(((CraftPlayer) player).getHandle().getWorld());
                if (targetEntity instanceof EntityPlayer) {
                    final Vec3D vec3D = useEntity.b();
                    if ((Math.abs(vec3D.a) > 0.41 || Math.abs(vec3D.b) > 1.91 || Math.abs(vec3D.c) > 0.41) && this
                            .alert(AlertType.RELEASE, player, "", true)) {
                        final int violations = this.playerData.getViolations(this, 60000L);
                        if (!this.playerData.isBanning() && !this.playerData.isRandomBan() && violations > 2) {
                            this.randomBan(player, 100.0);
                        }
                    }
                }
            }
        }
    }

}
