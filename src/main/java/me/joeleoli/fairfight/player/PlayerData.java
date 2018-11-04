package me.joeleoli.fairfight.player;

import lombok.Getter;
import lombok.Setter;

import me.joeleoli.fairfight.FairFight;
import me.joeleoli.fairfight.check.impl.aimassist.*;
import me.joeleoli.fairfight.check.impl.autoclicker.*;
import me.joeleoli.fairfight.check.impl.badpackets.*;
import me.joeleoli.fairfight.check.impl.inventory.*;
import me.joeleoli.fairfight.check.impl.killaura.*;
import me.joeleoli.fairfight.check.impl.velocity.VelocityA;
import me.joeleoli.fairfight.check.ICheck;
import me.joeleoli.fairfight.check.impl.fly.FlyA;
import me.joeleoli.fairfight.check.impl.fly.FlyB;
import me.joeleoli.fairfight.check.impl.fly.FlyC;
import me.joeleoli.fairfight.check.impl.range.RangeA;
import me.joeleoli.fairfight.check.impl.scaffold.ScaffoldA;
import me.joeleoli.fairfight.check.impl.scaffold.ScaffoldB;
import me.joeleoli.fairfight.check.impl.scaffold.ScaffoldC;
import me.joeleoli.fairfight.check.impl.step.StepA;
import me.joeleoli.fairfight.check.impl.timer.TimerA;
import me.joeleoli.fairfight.check.impl.velocity.VelocityB;
import me.joeleoli.fairfight.check.impl.velocity.VelocityC;
import me.joeleoli.fairfight.check.impl.wtap.WTapA;
import me.joeleoli.fairfight.check.impl.wtap.WTapB;
import me.joeleoli.fairfight.client.ClientType;
import me.joeleoli.fairfight.client.EnumClientType;
import me.joeleoli.fairfight.util.CustomLocation;

import net.minecraft.server.v1_8_R3.BlockPosition;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Setter @Getter public final class PlayerData {

	private static final Map<Class<? extends ICheck>, Constructor<? extends ICheck>> CONSTRUCTORS;
	public static final Class<? extends ICheck>[] CHECKS;

	private final Map<UUID, List<CustomLocation>> recentPlayerPackets;
	private final Map<ICheck, Set<Long>> checkViolationTimes;
	private final Map<Class<? extends ICheck>, ICheck> checkMap;
	private final Map<Integer, Long> keepAliveTimes;
	private final Map<ICheck, Double> checkVlMap;
	private final Set<BlockPosition> fakeBlocks = new HashSet<>();
	private final Set<CustomLocation> teleportLocations;
	private Map<String, String> forgeMods;
	private StringBuilder sniffedPacketBuilder;
	private CustomLocation lastMovePacket;
	private ClientType client;
	private UUID lastTarget;
	private String randomBanReason;
	private double randomBanRate;
	private boolean randomBan;
	private boolean allowTeleport;
	private boolean inventoryOpen;
	private boolean setInventoryOpen;
	private boolean sendingVape;
	private boolean attackedSinceVelocity;
	private boolean underBlock;
	private boolean sprinting;
	private boolean inLiquid;
	private boolean instantBreakDigging;
	private boolean fakeDigging;
	private boolean onGround;
	private boolean sniffing;
	private boolean onStairs;
	private boolean onCarpet;
	private boolean placing;
	private boolean banning;
	private boolean digging;
	private boolean inWeb;
	private boolean onIce;
	private boolean wasUnderBlock;
	private boolean wasOnGround;
	private boolean wasInLiquid;
	private boolean wasInWeb;
	private double lastGroundY;
	private double velocityX;
	private double velocityY;
	private double velocityZ;
	private long lastDelayedMovePacket;
	private long lastAnimationPacket;
	private long lastAttackPacket;
	private long lastVelocity;
	private long ping;
	private int velocityH;
	private int velocityV;
	private int lastCps;
	private int movementsSinceIce;
	private int movementsSinceUnderBlock;

	public PlayerData() {
		this.recentPlayerPackets = new HashMap<>();
		this.checkViolationTimes = new HashMap<>();
		this.checkMap = new HashMap<>();
		this.keepAliveTimes = new HashMap<>();
		this.checkVlMap = new HashMap<>();
		this.teleportLocations = Collections.newSetFromMap(new ConcurrentHashMap<CustomLocation, Boolean>());
		this.sniffedPacketBuilder = new StringBuilder();
		this.client = EnumClientType.VANILLA;

		FairFight.getInstance().getServer().getScheduler().runTaskAsynchronously(FairFight.getInstance(), () -> {
			PlayerData.CONSTRUCTORS.keySet().stream().map(o -> (Class<? extends ICheck>) o).forEach(check -> {
				Constructor<? extends ICheck> constructor = PlayerData.CONSTRUCTORS.get(check);
				try {
					this.checkMap.put(check, constructor.newInstance(this));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		});
	}

	public <T extends ICheck> T getCheck(final Class<T> clazz) {
		return (T) this.checkMap.get(clazz);
	}

	public CustomLocation getLastPlayerPacket(final UUID playerUUID, final int index) {
		final List<CustomLocation> customLocations = this.recentPlayerPackets.get(playerUUID);
		if (customLocations != null && customLocations.size() > index) {
			return customLocations.get(customLocations.size() - index);
		}
		return null;
	}

	public void addPlayerPacket(final UUID playerUUID, final CustomLocation customLocation) {
		List<CustomLocation> customLocations = this.recentPlayerPackets.get(playerUUID);
		if (customLocations == null) {
			customLocations = new ArrayList<>();
		}
		if (customLocations.size() == 20) {
			customLocations.remove(0);
		}
		customLocations.add(customLocation);
		this.recentPlayerPackets.put(playerUUID, customLocations);
	}

	public void addTeleportLocation(final CustomLocation teleportLocation) {
		this.teleportLocations.add(teleportLocation);
	}

	public boolean allowTeleport(final CustomLocation teleportLocation) {
		for (final CustomLocation customLocation : this.teleportLocations) {
			final double delta = Math.pow(teleportLocation.getX() - customLocation.getX(), 2.0) +
			                     Math.pow(teleportLocation.getZ() - customLocation.getZ(), 2.0);
			if (delta <= 0.005) {
				this.teleportLocations.remove(customLocation);
				return true;
			}
		}
		return false;
	}

	public double getCheckVl(final ICheck check) {
		if (!this.checkVlMap.containsKey(check)) {
			this.checkVlMap.put(check, 0.0);
		}

		return this.checkVlMap.get(check);
	}

	public void setCheckVl(double vl, final ICheck check) {
		if (vl < 0.0) {
			vl = 0.0;
		}

		this.checkVlMap.put(check, vl);
	}

	public boolean keepAliveExists(final int id) {
		return this.keepAliveTimes.containsKey(id);
	}

	public long getKeepAliveTime(final int id) {
		return this.keepAliveTimes.get(id);
	}

	public void removeKeepAliveTime(final int id) {
		this.keepAliveTimes.remove(id);
	}

	public void addKeepAliveTime(final int id) {
		this.keepAliveTimes.put(id, System.currentTimeMillis());
	}

	public int getViolations(final ICheck check, final Long time) {
		final Set<Long> timestamps = this.checkViolationTimes.get(check);

		if (timestamps != null) {
			int violations = 0;

			for (final long timestamp : timestamps) {
				if (System.currentTimeMillis() - timestamp <= time) {
					++violations;
				}
			}

			return violations;
		}

		return 0;
	}

	public void addViolation(final ICheck check) {
		Set<Long> timestamps = this.checkViolationTimes.get(check);

		if (timestamps == null) {
			timestamps = new HashSet<>();
		}

		timestamps.add(System.currentTimeMillis());

		this.checkViolationTimes.put(check, timestamps);
	}

	static {
		CHECKS = new Class[]{
				AimAssistA.class, AimAssistB.class, AimAssistC.class, AimAssistD.class,
				AimAssistE.class,

				AutoClickerA.class, AutoClickerB.class, AutoClickerC.class, AutoClickerD.class,
				AutoClickerE.class, AutoClickerF.class, AutoClickerG.class, AutoClickerH.class,
				AutoClickerI.class, AutoClickerJ.class, AutoClickerK.class, AutoClickerK.class,
				AutoClickerL.class,

				BadPacketsA.class, BadPacketsB.class, BadPacketsC.class, BadPacketsD.class,
				BadPacketsE.class, BadPacketsF.class, BadPacketsG.class, BadPacketsH.class,
				BadPacketsI.class, BadPacketsJ.class, BadPacketsK.class, BadPacketsL.class,

				FlyA.class, FlyB.class, FlyC.class,

				InventoryA.class, InventoryB.class, InventoryC.class, InventoryD.class,
				InventoryE.class, InventoryF.class, InventoryG.class,

				KillAuraA.class, KillAuraB.class, KillAuraC.class, KillAuraD.class,
				KillAuraE.class, KillAuraF.class, KillAuraG.class, KillAuraH.class,
				KillAuraI.class, KillAuraJ.class, KillAuraK.class, KillAuraL.class,
				KillAuraM.class, KillAuraN.class, KillAuraO.class, KillAuraP.class,
				KillAuraQ.class, KillAuraR.class, KillAuraS.class,

				RangeA.class,

				TimerA.class,

				VelocityA.class, VelocityB.class, VelocityC.class,

				WTapA.class, WTapB.class,

				ScaffoldA.class, ScaffoldB.class, ScaffoldC.class,

				StepA.class,
		};

		CONSTRUCTORS = new ConcurrentHashMap<>();

		for (final Class<? extends ICheck> check : PlayerData.CHECKS) {
			try {
				PlayerData.CONSTRUCTORS.put(check, check.getConstructor(PlayerData.class));
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

}
