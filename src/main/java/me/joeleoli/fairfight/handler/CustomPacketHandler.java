package me.joeleoli.fairfight.handler;

import lombok.AllArgsConstructor;

import me.joeleoli.fairfight.event.player.AlertType;
import me.joeleoli.ragespigot.handler.PacketHandler;
import me.joeleoli.fairfight.FairFight;
import me.joeleoli.fairfight.check.ICheck;
import me.joeleoli.fairfight.client.EnumClientType;
import me.joeleoli.fairfight.player.PlayerData;
import me.joeleoli.fairfight.event.player.PlayerAlertEvent;
import me.joeleoli.fairfight.event.player.PlayerBanEvent;
import me.joeleoli.fairfight.util.CustomLocation;

import net.minecraft.server.v1_8_R3.*;

import org.apache.commons.io.Charsets;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class CustomPacketHandler implements PacketHandler {

    private static Field positionField;

    private static final List<String> INSTANT_BREAK_BLOCKS = Arrays.asList(
            "reeds", "waterlily", "deadbush", "flower", "doubleplant", "tallgrass"
    );

    static {
        try {
            positionField = PacketPlayOutBlockChange.class.getDeclaredField("a");
            positionField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final FairFight plugin;

    public void handleReceivedPacket(PlayerConnection playerConnection, Packet packet) {
        try {
            final Player player = playerConnection.getPlayer();
            final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player);

            if (playerData == null) {
                return;
            }

            if (playerData.isSniffing()) {
                this.handleSniffedPacket(packet, playerData);
            }

            final String simpleName = packet.getClass().getSimpleName();

            switch (simpleName) {
                case "PacketPlayInCustomPayload": {
                    if (!playerData.getClient().isHacked()) {
                        this.handleCustomPayload((PacketPlayInCustomPayload) packet, playerData, player);
                        break;
                    }
                    break;
                }
                case "PacketPlayInPosition":
                case "PacketPlayInPositionLook":
                case "PacketPlayInLook":
                case "PacketPlayInFlying": {
                    this.handleFlyPacket((PacketPlayInFlying)packet, playerData);
                    break;
                }
                case "PacketPlayInKeepAlive": {
                    this.handleKeepAlive((PacketPlayInKeepAlive)packet, playerData, player);
                    break;
                }
                case "PacketPlayInUseEntity": {
                    this.handleUseEntity((PacketPlayInUseEntity)packet, playerData, player);
                    break;
                }
                case "PacketPlayInBlockPlace": {
                    playerData.setPlacing(true);
                    break;
                }
                case "PacketPlayInCloseWindow": {
                    playerData.setInventoryOpen(false);
                    break;
                }
                case "PacketPlayInClientCommand": {
                    if (((PacketPlayInClientCommand)packet).a() == PacketPlayInClientCommand.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                        playerData.setInventoryOpen(true);
                        break;
                    }
                    break;
                }
                case "PacketPlayInEntityAction": {
                    final PacketPlayInEntityAction.EnumPlayerAction actionType = ((PacketPlayInEntityAction)packet).b();
                    if (actionType == PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING) {
                        playerData.setSprinting(true);
                        break;
                    }
                    if (actionType == PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING) {
                        playerData.setSprinting(false);
                        break;
                    }
                    break;
                }
                case "PacketPlayInBlockDig": {
                    final PacketPlayInBlockDig.EnumPlayerDigType digType = ((PacketPlayInBlockDig)packet).c();

                    if (playerData.getFakeBlocks().contains(((PacketPlayInBlockDig) packet).a())) {
                        playerData.setInstantBreakDigging(false);
                        playerData.setFakeDigging(true);
                        playerData.setDigging(false);
                    } else {
                        playerData.setFakeDigging(false);

                        if (digType == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                            Block block = ((CraftWorld) player.getWorld()).getHandle().c(((PacketPlayInBlockDig) packet).a());

                            String tile = block.a().replace("tile.", "");

                            if (INSTANT_BREAK_BLOCKS.contains(tile)) {
                                playerData.setInstantBreakDigging(true);
                            } else {
                                playerData.setInstantBreakDigging(false);
                            }

                            playerData.setDigging(true);
                        } else if (digType == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK ||
                                   digType == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                            playerData.setInstantBreakDigging(false);
                            playerData.setDigging(false);
                        }
                    }
                    break;
                }
                case "PacketPlayInArmAnimation": {
                    playerData.setLastAnimationPacket(System.currentTimeMillis());
                    break;
                }
            }

            for (final Class<? extends ICheck> checkClass : PlayerData.CHECKS) {
                if (!FairFight.getInstance().getDisabledChecks().contains(checkClass.getSimpleName().toUpperCase())) {
                    final ICheck check = (playerData.getCheck(checkClass));

                    if (check != null && check.getType() == Packet.class) {
                        check.handleCheck(playerConnection.getPlayer(), packet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void handleSentPacket(final PlayerConnection playerConnection, final Packet packet) {
        try {
            final Player player = playerConnection.getPlayer();
            final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player);

            if (playerData == null) {
                return;
            }

            final String simpleName = packet.getClass().getSimpleName();

            switch (simpleName) {
                case "PacketPlayOutEntityVelocity": {
                    this.handleVelocityOut((PacketPlayOutEntityVelocity)packet, playerData, player);
                    break;
                }
                case "PacketPlayOutExplosion": {
                    this.handleExplosionPacket((PacketPlayOutExplosion)packet, playerData);
                    break;
                }
                case "PacketPlayOutEntityLook":
                case "PacketPlayOutRelEntityMove":
                case "PacketPlayOutRelEntityMoveLook":
                case "PacketPlayOutEntity": {
                    this.handleEntityPacket((PacketPlayOutEntity)packet, playerData, player);
                    break;
                }
                case "PacketPlayOutEntityTeleport": {
                    this.handleTeleportPacket((PacketPlayOutEntityTeleport)packet, playerData, player);
                    break;
                }
                case "PacketPlayOutPosition": {
                    this.handlePositionPacket((PacketPlayOutPosition)packet, playerData);
                    break;
                }
                case "PacketPlayOutKeepAlive": {
                    playerData.addKeepAliveTime(((PacketPlayOutKeepAlive)packet).getA());
                    break;
                }
                case "PacketPlayOutCloseWindow": {
                    if (!playerData.keepAliveExists(-1)) {
                        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutKeepAlive(-1));
                        break;
                    }

                    break;
                }
                case "PacketPlayOutMultiBlockChange":
                    for (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo info : ((PacketPlayOutMultiBlockChange) packet).getB()) {
                        BlockPosition position = info.a();

                        String name = info.c().getBlock().toString().replace("Block{minecraft:", "").replace("}", "");

                        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

                        if (elements.length == 19 && elements[3].getMethodName().equals("sendTo")) {
                            if (name.equals("air")) {
                                playerData.getFakeBlocks().remove(position);
                            } else {
                                playerData.getFakeBlocks().add(position);
                            }
                        }
                    }
                    break;
                case "PacketPlayOutBlockChange":
                    BlockPosition position = (BlockPosition) positionField.get(packet);

                    String name = ((PacketPlayOutBlockChange) packet).block.getBlock().toString()
                            .replace("Block{minecraft:", "").replace("}", "");

                    StackTraceElement[] elements = Thread.currentThread().getStackTrace();

                    if (elements.length == 13 && elements[3].getMethodName().equals("sendBlockChange")) {
                        if (name.equals("air")) {
                            playerData.getFakeBlocks().remove(position);
                        } else {
                            playerData.getFakeBlocks().add(position);
                        }
                    }
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleSniffedPacket(Packet packet, PlayerData playerData) {
        try {
            final StringBuilder builder = new StringBuilder();

            builder.append(packet.getClass().getSimpleName());
            builder.append(" (timestamp = ");
            builder.append(System.currentTimeMillis());

            final List<Field> fieldsList = new ArrayList<>();

            fieldsList.addAll(Arrays.asList(packet.getClass().getDeclaredFields()));
            fieldsList.addAll(Arrays.asList(packet.getClass().getSuperclass().getDeclaredFields()));

            for (final Field field : fieldsList) {
                if (field.getName().equalsIgnoreCase("timestamp")) {
                    continue;
                }

                field.setAccessible(true);
                builder.append(", ");
                builder.append(field.getName());
                builder.append(" = ");
                builder.append(field.get(packet));
            }

            builder.append(")");

            playerData.getSniffedPacketBuilder().append(builder.toString());
            playerData.getSniffedPacketBuilder().append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleCustomPayload(PacketPlayInCustomPayload packet, PlayerData playerData, Player player) {
        final String a = packet.a();
        int n = -1;
        switch (a.hashCode()) {
            case -1772699639: {
                if (a.equals("LOLIMAHCKER")) {
                    n = 0;
                    break;
                }
                break;
            }
            case 3059156: {
                if (a.equals("cock")) {
                    n = 1;
                    break;
                }
                break;
            }
            case 509975521: {
                if (a.equals("customGuiOpenBspkrs")) {
                    n = 2;
                    break;
                }
                break;
            }
            case 1228162850: {
                if (a.equals("0SO1Lk2KASxzsd")) {
                    n = 3;
                    break;
                }
                break;
            }
            case 633656225: {
                if (a.equals("mincraftpvphcker")) {
                    n = 4;
                    break;
                }
                break;
            }
            case 279718608: {
                if (a.equals("lmaohax")) {
                    n = 5;
                    break;
                }
                break;
            }
            case 1566847235: {
                if (a.equals("MCnetHandler")) {
                    n = 6;
                    break;
                }
                break;
            }
            case 24251720: {
                if (a.equals("L0LIMAHCKER")) {
                    n = 7;
                    break;
                }
                break;
            }
            case 2420330: {
                if (a.equals("OCMC")) {
                    n = 8;
                    break;
                }
                break;
            }
            case 92413603: {
                if (a.equals("REGISTER")) {
                    n = 9;
                    break;
                }
                break;
            }
        }

        EnumClientType type;

        clientCheck: {
            switch (n) {
                case 0: {
                    type = EnumClientType.HACKED_CLIENT_A;
                    break clientCheck;
                }
                case 1: {
                    type = EnumClientType.HACKED_CLIENT_B;
                    break clientCheck;
                }
                case 2: {
                    type = EnumClientType.HACKED_CLIENT_C;
                    break clientCheck;
                }
                case 3: {
                    type = EnumClientType.HACKED_CLIENT_C2;
                    break clientCheck;
                }
                case 4: {
                    type = EnumClientType.HACKED_CLIENT_C3;
                    break clientCheck;
                }
                case 5: {
                    type = EnumClientType.HACKED_CLIENT_D;
                    break clientCheck;
                }
                case 6: {
                    type = EnumClientType.HACKED_CLIENT_E;
                    break clientCheck;
                }
                case 7: {
                    type = EnumClientType.HACKED_CLIENT_M;
                    break clientCheck;
                }
                case 8: {
                    type = EnumClientType.OCMC;
                    break clientCheck;
                }
                case 9: {
                    try {
                        final String registerType = packet.b().toString(Charsets.UTF_8);

                        if (registerType.contains("CB-Client")) {
                            type = EnumClientType.CHEAT_BREAKER;
                        } else {
                            if (!registerType.equalsIgnoreCase("CC")) {
                                return;
                            }

                            type = EnumClientType.COSMIC_CLIENT;
                        }

                        break clientCheck;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }

            return;
        }

        playerData.setClient(type);

        if (type.isHacked()) {
            this.plugin.getServer().getPluginManager().callEvent(new PlayerAlertEvent(AlertType.RELEASE, player, type.getName(), null));

            playerData.setRandomBanRate(500.0);
            playerData.setRandomBanReason(type.getName());
            playerData.setRandomBan(true);
        }
    }
    
    private void handleFlyPacket(PacketPlayInFlying packet, PlayerData playerData) {
        CustomLocation customLocation = new CustomLocation(packet.a(), packet.b(), packet.c(), packet.d(), packet.e());
        CustomLocation lastLocation = playerData.getLastMovePacket();

        if (lastLocation != null) {
            if (!packet.g()) {
                customLocation.setX(lastLocation.getX());
                customLocation.setY(lastLocation.getY());
                customLocation.setZ(lastLocation.getZ());
            }

            if (!packet.h()) {
                customLocation.setYaw(lastLocation.getYaw());
                customLocation.setPitch(lastLocation.getPitch());
            }

            if (System.currentTimeMillis() - lastLocation.getTimestamp() > 110L) {
                playerData.setLastDelayedMovePacket(System.currentTimeMillis());
            }
        }

        if (playerData.isSetInventoryOpen()) {
            playerData.setInventoryOpen(false);
            playerData.setSetInventoryOpen(false);
        }

        playerData.setLastMovePacket(customLocation);
        playerData.setPlacing(false);
        playerData.setAllowTeleport(false);

        if (packet instanceof PacketPlayInFlying.PacketPlayInPositionLook && playerData.allowTeleport(customLocation)) {
            playerData.setAllowTeleport(true);
        }
    }
    
    private void handleKeepAlive(final PacketPlayInKeepAlive packet, final PlayerData playerData, final Player player) {
        final int id = packet.a();

        if (playerData.keepAliveExists(id)) {
            if (id == -1) {
                playerData.setSetInventoryOpen(true);
            } else {
                playerData.setPing(System.currentTimeMillis() - playerData.getKeepAliveTime(id));
            }

            playerData.removeKeepAliveTime(id);
        } else if (id != 0) {
            this.plugin.getServer().getPluginManager().callEvent(new PlayerAlertEvent(AlertType.RELEASE, player, "Illegal Packets", null));
        }
    }
    
    private void handleUseEntity(final PacketPlayInUseEntity packet, final PlayerData playerData, final Player player) {
        if (packet.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
            playerData.setLastAttackPacket(System.currentTimeMillis());

            if (playerData.isSendingVape()) {
                playerData.setSendingVape(false);
            }

            if (!playerData.isAttackedSinceVelocity()) {
                playerData.setVelocityX(playerData.getVelocityX() * 0.6);
                playerData.setVelocityZ(playerData.getVelocityZ() * 0.6);
                playerData.setAttackedSinceVelocity(true);
            }

            if (!playerData.isBanning() && playerData.isRandomBan() && Math.random() * playerData.getRandomBanRate() < 1.0) {
                playerData.setBanning(true);

                this.plugin.getServer().getPluginManager().callEvent(new PlayerBanEvent(player, playerData.getRandomBanReason()));
            }

            final Entity targetEntity = packet.a(((CraftPlayer)player).getHandle().getWorld());

            if (targetEntity instanceof EntityPlayer) {
                final Player target = (Player) targetEntity.getBukkitEntity();
                playerData.setLastTarget(target.getUniqueId());
            }
        }
    }
    
    private void handleVelocityOut(final PacketPlayOutEntityVelocity packet, final PlayerData playerData, final Player player) {
        if (packet.getA() == player.getEntityId()) {
            final double x = Math.abs(packet.getB() / 8000.0);
            final double y = packet.getC() / 8000.0;
            final double z = Math.abs(packet.getD() / 8000.0);
            if (x > 0.0 || z > 0.0) {
                playerData.setVelocityH((int)(((x + z) / 2.0 + 2.0) * 15.0));
            }
            if (y > 0.0) {
                playerData.setVelocityV((int)(Math.pow(y + 2.0, 2.0) * 5.0));
                if (playerData.isOnGround() && player.getLocation().getY() % 1.0 == 0.0) {
                    playerData.setVelocityX(x);
                    playerData.setVelocityY(y);
                    playerData.setVelocityZ(z);
                    playerData.setLastVelocity(System.currentTimeMillis());
                    playerData.setAttackedSinceVelocity(false);
                }
            }
        }
    }
    
    private void handleExplosionPacket(PacketPlayOutExplosion packet, PlayerData playerData) {
        final float x = Math.abs(packet.getF());
        final float y = packet.getG();
        final float z = Math.abs(packet.getH());

        if (x > 0.0f || z > 0.0f) {
            playerData.setVelocityH((int)(((x + z) / 2.0f + 2.0f) * 15.0f));
        }
        if (y > 0.0f) {
            playerData.setVelocityV((int)(Math.pow(y + 2.0f, 2.0) * 5.0));
        }
    }
    
    private void handleEntityPacket(PacketPlayOutEntity packet, PlayerData playerData, Player player) {
        final Entity targetEntity = ((CraftPlayer)player).getHandle().getWorld().a(packet.getA());

        if (targetEntity instanceof EntityPlayer) {
            final Player target = (Player) targetEntity.getBukkitEntity();
            final CustomLocation customLocation = playerData.getLastPlayerPacket(target.getUniqueId(), 1);

            if (customLocation != null) {
                final double x = packet.getB() / 32.0;
                final double y = packet.getC() / 32.0;
                final double z = packet.getD() / 32.0;
                float yaw = packet.getE() * 360.0f / 256.0f;
                float pitch = packet.getF() * 360.0f / 256.0f;

                if (!packet.isH()) {
                    yaw = customLocation.getYaw();
                    pitch = customLocation.getPitch();
                }

                playerData.addPlayerPacket(target.getUniqueId(), new CustomLocation(customLocation.getX() + x, customLocation.getY() + y, customLocation.getZ() + z, yaw, pitch));
            }
        }
    }
    
    private void handleTeleportPacket(final PacketPlayOutEntityTeleport packet, final PlayerData playerData, final Player player) {
        final Entity targetEntity = ((CraftPlayer)player).getHandle().getWorld().a(packet.getA());

        if (targetEntity instanceof EntityPlayer) {
            final Player target = (Player)targetEntity.getBukkitEntity();
            double x = packet.getB() / 32.0;
            double z = packet.getD() / 32.0;
            final double y = packet.getC() / 32.0;
            final float yaw = packet.getE() * 360.0f / 256.0f;
            final float pitch = packet.getF() * 360.0f / 256.0f;

            playerData.addPlayerPacket(target.getUniqueId(), new CustomLocation(x, y, z, yaw, pitch));
        }
    }
    
    private void handlePositionPacket(final PacketPlayOutPosition packet, final PlayerData playerData) {
        if (packet.getE() > 90.0f) {
            packet.setE(90.0f);
        } else if (packet.getE() < -90.0f) {
            packet.setE(-90.0f);
        } else if (packet.getE() == 0.0f) {
            packet.setE(0.492832f);
        }

        playerData.setVelocityY(0.0);
        playerData.setVelocityX(0.0);
        playerData.setVelocityZ(0.0);
        playerData.setAttackedSinceVelocity(false);
        playerData.addTeleportLocation(new CustomLocation(packet.getA(), packet.getB(), packet.getC(), packet.getD(), packet.getE()));
    }
    
    private float getAngle(final double posX, final double posZ, final CustomLocation location) {
        final double x = posX - location.getX();
        final double z = posZ - location.getZ();
        float newYaw = (float)Math.toDegrees(-Math.atan(x / z));

        if (z < 0.0 && x < 0.0) {
            newYaw = (float)(90.0 + Math.toDegrees(Math.atan(z / x)));
        } else if (z < 0.0 && x > 0.0) {
            newYaw = (float)(-90.0 + Math.toDegrees(Math.atan(z / x)));
        }

        return newYaw;
    }

}
