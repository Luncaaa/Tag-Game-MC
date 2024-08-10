package me.lucaaa.tag.actions.actionTypes;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.Action;
import me.lucaaa.tag.game.Arena;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class TitleAction extends Action {
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleAction(TagGame plugin, ConfigurationSection actionSection) {
        super(plugin, List.of("title", "subtitle", "fadeIn", "stay", "fadeOut"), actionSection);
        this.title = actionSection.getString("title");
        this.subtitle = actionSection.getString("subtitle");
        this.fadeIn = actionSection.getInt("fadeIn", 20);
        this.stay = actionSection.getInt("stay");
        this.fadeOut = actionSection.getInt("fadeOut", 20);
    }

    @Override
    public void runAction(Arena arena, Player player) {
        String title = this.getTextString(this.title, player, arena.getPlaceholders());
        String subtitle = this.getTextString(this.subtitle, player, arena.getPlaceholders());
        player.sendTitle(title, subtitle, this.fadeIn, this.stay, this.fadeOut);
    }
}