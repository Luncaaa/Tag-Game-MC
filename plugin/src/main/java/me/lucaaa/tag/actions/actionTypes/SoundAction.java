package me.lucaaa.tag.actions.actionTypes;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.Action;
import me.lucaaa.tag.game.Arena;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class SoundAction extends Action {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAction(TagGame plugin, ConfigurationSection actionSection) {
        super(plugin, List.of("sound", "volume", "pitch"), actionSection);
        Sound sound = null;
        try {
            sound = Sound.valueOf(actionSection.getString("sound"));
        } catch (IllegalArgumentException exception) {
            plugin.log(Level.WARNING, "Invalid sound found on action \"" + actionSection.getName() + "\": " + actionSection.getString("sound"));
            this.isCorrect = false;
        }

        this.sound = sound;
        this.volume = (float) actionSection.getDouble("volume");
        this.pitch = (float) actionSection.getDouble("pitch");
    }

    @Override
    public void runAction(Arena arena, Player player) {
        player.playSound(player.getLocation(), this.sound, this.volume, this.pitch);
    }
}