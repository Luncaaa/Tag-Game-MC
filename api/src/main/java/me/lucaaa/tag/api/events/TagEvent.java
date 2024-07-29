package me.lucaaa.tag.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for TagGame's events.
 */
@SuppressWarnings("unused")
public class TagEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    /**
     * Required by Spigot's {@link Event}
     * @return The HandlerList.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
