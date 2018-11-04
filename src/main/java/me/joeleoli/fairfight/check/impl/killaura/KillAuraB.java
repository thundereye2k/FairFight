package me.joeleoli.fairfight.check.impl.killaura;

import me.joeleoli.fairfight.check.checks.PacketCheck;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.AlertType;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.entity.Player;

public class KillAuraB extends PacketCheck {
	private boolean sent;
	private boolean failed;
	private int movements;

	public KillAuraB(PlayerData playerData) {
		super(playerData, "Kill-Aura (Check 2)");
	}

	@Override
	public void handleCheck(Player player, Packet packet) {
		if (this.playerData.isDigging() && !this.playerData.isInstantBreakDigging() &&
		    System.currentTimeMillis() - this.playerData.getLastDelayedMovePacket() > 220L &&
		    this.playerData.getLastMovePacket() != null &&
		    System.currentTimeMillis() - this.playerData.getLastMovePacket().getTimestamp() < 110L) {
			int vl = (int) this.getVl();
			if (packet instanceof PacketPlayInBlockDig &&
			    ((PacketPlayInBlockDig) packet).c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
				this.movements = 0;
				vl = 0;
			} else if (packet instanceof PacketPlayInArmAnimation && this.movements >= 2) {
				if (this.sent) {
					if (!this.failed) {
						if (++vl >= 5) {
							this.alert(AlertType.EXPERIMENTAL, player, "VL " + vl + ".", false);
						}
						this.failed = true;
					}
				} else {
					this.sent = true;
				}
			} else if (packet instanceof PacketPlayInFlying) {
				final boolean b = false;
				this.failed = b;
				this.sent = b;
				++this.movements;
			}
			this.setVl(vl);
		}
	}
}
