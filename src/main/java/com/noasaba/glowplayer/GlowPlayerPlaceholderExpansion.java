package com.noasaba.glowplayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

final class GlowPlayerPlaceholderExpansion extends PlaceholderExpansion {

    private final GlowPlayer plugin;

    GlowPlayerPlaceholderExpansion(GlowPlayer plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "glowplayer";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return switch (params.toLowerCase(Locale.ROOT)) {
            case "glowcolor", "color" -> player == null
                    ? plugin.defaultGlowColorCode()
                    : plugin.glowColorCode(player.getUniqueId());
            case "enabled" -> player == null
                    ? Boolean.TRUE.toString()
                    : Boolean.toString(plugin.isGlowEnabled(player.getUniqueId()));
            default -> null;
        };
    }
}
