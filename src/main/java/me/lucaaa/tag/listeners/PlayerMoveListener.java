package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import me.lucaaa.tag.utils.AreaLeaveActions;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        PlayerData playerData = TagGame.playersManager.getPlayerData(event.getPlayer().getName());
        if (!playerData.isInArena()) return;

        Arena arena = playerData.arena;
        AreaLeaveActions action = AreaLeaveActions.valueOf(TagGame.mainConfig.getConfig().getString("borders-leave"));
        if (action == AreaLeaveActions.NOTHING) return;

        double highX;
        double highY;
        double highZ;

        double lowX;
        double lowY;
        double lowZ;

        if (playerData.inWaitingArea) {
            highX = Math.max(arena.getWaitingCorner1().getX(), arena.getWaitingCorner2().getX());
            highY = Math.max(arena.getWaitingCorner1().getY(), arena.getWaitingCorner2().getY());
            highZ = Math.max(arena.getWaitingCorner1().getZ(), arena.getWaitingCorner2().getZ());

            lowX = Math.min(arena.getWaitingCorner1().getX(), arena.getWaitingCorner2().getX());
            lowY = Math.min(arena.getWaitingCorner1().getY(), arena.getWaitingCorner2().getY());
            lowZ = Math.min(arena.getWaitingCorner1().getZ(), arena.getWaitingCorner2().getZ());

        } else {
            highX = Math.max(arena.getArenaCorner1().getX(), arena.getArenaCorner2().getX());
            highY = Math.max(arena.getArenaCorner1().getY(), arena.getArenaCorner2().getY());
            highZ = Math.max(arena.getArenaCorner1().getZ(), arena.getArenaCorner2().getZ());

            lowX = Math.min(arena.getArenaCorner1().getX(), arena.getArenaCorner2().getX());
            lowY = Math.min(arena.getArenaCorner1().getY(), arena.getArenaCorner2().getY());
            lowZ = Math.min(arena.getArenaCorner1().getZ(), arena.getArenaCorner2().getZ());
        }

        Location to = event.getTo();
        assert to != null;
        if ((to.getX() <= highX && to.getX() >= lowX) && (to.getY() <= highY && to.getY() >= lowY) && (to.getZ() <= highZ && to.getZ() >= lowZ)) return;

        switch (action) {
            case PREVENT -> event.setCancelled(true);
            case LEAVE_GAME_TP -> arena.playerLeave(event.getPlayer(), true);
            case LEAVE_GAME -> arena.playerLeave(event.getPlayer(), false);
        }
    }
}