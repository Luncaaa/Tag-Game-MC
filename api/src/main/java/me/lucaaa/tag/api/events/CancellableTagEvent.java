package me.lucaaa.tag.api.events;

import org.bukkit.event.Cancellable;

/**
 * Represents a {@link TagEvent} that can be cancelled.
 */
public class CancellableTagEvent extends TagEvent implements Cancellable {
    private boolean isCancelled = false;

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
