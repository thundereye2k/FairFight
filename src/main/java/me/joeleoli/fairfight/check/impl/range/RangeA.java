package me.joeleoli.fairfight.check.impl.range;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.fairfight.util.MathUtil;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.joeleoli.fairfight.util.CustomLocation;

public class RangeA extends PacketCheck {

	private boolean sameTick;

	public RangeA(PlayerData playerData) {
		super(playerData, "Range");
	}

	@Override
	public void handleCheck(Player player, Packet packet) {
		if (packet instanceof PacketPlayInUseEntity && !player.getGameMode().equals(GameMode.CREATIVE) &&
		    System.currentTimeMillis() - this.playerData.getLastDelayedMovePacket() > 220L &&
		    this.playerData.getLastMovePacket() != null &&
		    System.currentTimeMillis() - this.playerData.getLastMovePacket().getTimestamp() < 110L && !this.sameTick) {

			PacketPlayInUseEntity useEntity = (PacketPlayInUseEntity) packet;

			if (useEntity.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
				final Entity targetEntity = useEntity.a(((CraftPlayer) player).getHandle().getWorld());

				if (targetEntity instanceof EntityPlayer) {
					final Player target = (Player) targetEntity.getBukkitEntity();
					final CustomLocation targetLocation = this.playerData.getLastPlayerPacket(target.getUniqueId(), MathUtil.pingFormula(this.playerData.getPing()));

					if (targetLocation == null) {
						return;
					}

					long diff = System.currentTimeMillis() - targetLocation.getTimestamp();
					long estimate = MathUtil.pingFormula(this.playerData.getPing()) * 50L;
					long diffEstimate = diff - estimate;

					if (diffEstimate >= 500L) {
						return;
					}

					CustomLocation playerLocation = this.playerData.getLastMovePacket();
					PlayerData targetData = this.getPlugin().getPlayerDataManager().getPlayerData(target);

					if (targetData == null) {
						return;
					}

					double range = Math.hypot(playerLocation.getX() - targetLocation.getX(), playerLocation.getZ() - targetLocation.getZ());

					if (range > 6.5) {
						return;
					}

					double threshold = 3.3;

					if (!targetData.isSprinting() ||
					    MathUtil.getDistanceBetweenAngles(playerLocation.getYaw(), targetLocation.getYaw()) <= 90.0) {
						threshold = 4.0;
					}

					double vl = this.getVl();

					if (range > threshold) {
						if (++vl >= 12.5) {
							boolean ex = this.getPlugin().getRangeVl() == 0.0;

							if (this.alert(ex ? AlertType.EXPERIMENTAL : AlertType.RELEASE, player, String.format("P %.1f. R %.3f. T %.2f. D %s. VL %.2f.", range - threshold + 3.0, range, threshold, diffEstimate, vl), false)) {
								if (!this.playerData.isBanning() && vl >= this.getPlugin().getRangeVl() && !ex) {
									this.ban(player);
								}
							} else {
								vl = 0.0;
							}
						}
					} else if (range >= 2.0) {
						vl -= 0.25;
					}

					this.setVl(vl);
					this.sameTick = true;
				}
			}
		} else if (packet instanceof PacketPlayInFlying) {
			this.sameTick = false;
		}
	}

}
