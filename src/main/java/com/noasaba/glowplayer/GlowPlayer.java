package com.noasaba.glowplayer;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class GlowPlayer extends JavaPlugin implements Listener, TabExecutor {

    private static final GlowColor FALLBACK_DEFAULT_COLOR = GlowColor.GOLD;
    private static final String DEFAULT_COLOR_CONFIG_PATH = "default-color";
    private static final String COMMAND_PERMISSION = "glowplayer.command";
    private static final String LIST_PERMISSION = "glowplayer.list";
    private static final String TOGGLE_PERMISSION = "glowplayer.toggle";
    private static final String DEFAULT_PERMISSION = "glowplayer.default";
    private static final String ADMIN_PERMISSION = "glowplayer.admin";

    private final Map<UUID, GlowState> glowStates = new HashMap<>();
    private final Map<UUID, String> previousTeams = new HashMap<>();
    private final Map<GlowColor, Team> glowTeams = new EnumMap<>(GlowColor.class);
    private GlowColor defaultColor = FALLBACK_DEFAULT_COLOR;
    private Scoreboard scoreboard;
    private boolean tabCompatibilityMode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadDefaultColor();
        tabCompatibilityMode = Bukkit.getPluginManager().getPlugin("TAB") != null;

        if (!tabCompatibilityMode && Bukkit.getScoreboardManager() == null) {
            getLogger().severe("Scoreboard manager is not available. GlowPlayer cannot start.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (tabCompatibilityMode) {
            getLogger().info("TAB detected: scoreboard team handling is disabled to prevent conflicts.");
            getLogger().info("Add %glowplayer_glowcolor% to the end of TAB's tagprefix.");
        } else {
            scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            registerGlowTeams();
        }

        registerPlaceholderExpansion();
        Bukkit.getPluginManager().registerEvents(this, this);

        PluginCommand glowCommand = getCommand("glow");
        if (glowCommand == null) {
            getLogger().severe("Command /glow is missing from plugin.yml.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        glowCommand.setExecutor(this);
        glowCommand.setTabCompleter(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            applyGlow(player, stateFor(player));
        }
        getLogger().info("GlowPlayer enabled for Paper 26.2 / Java 25.");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            clearGlowTeam(player);
            player.setGlowing(false);
        }
        getLogger().info("GlowPlayer disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyGlow(event.getPlayer(), stateFor(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GlowState state = stateFor(player);
        Bukkit.getScheduler().runTaskLater(this, () -> applyGlow(player, state), 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearGlowTeam(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("glow")) {
            return false;
        }

        if (!hasAnyPermission(sender, COMMAND_PERMISSION)) {
            deny(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            handleConsoleCommand(sender, args);
            return true;
        }

        if (args.length == 0) {
            toggleGlow(player);
            return true;
        }

        if (args.length == 1) {
            String requested = args[0].toLowerCase(Locale.ROOT);
            switch (requested) {
                case "on" -> setGlowEnabled(player, true);
                case "off", "clear", "none" -> setGlowEnabled(player, false);
                case "list", "colors", "colours" -> sendColorList(player);
                case "default" -> sendDefaultColor(player);
                case "help" -> sendUsage(player);
                default -> setGlowColor(player, requested);
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("default")) {
            setDefaultColor(player, args[1]);
            return true;
        }

        if (args.length > 2) {
            sendUsage(player);
            return true;
        }

        sendUsage(player);
        return true;
    }

    private void handleConsoleCommand(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("default")) {
            sendDefaultColor(sender);
            return;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("default")) {
            setDefaultColor(sender, args[1]);
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendUsage(sender);
            return;
        }

        sender.sendMessage("コンソールから使用できる操作は /glow default と /glow default <色> のみです。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("glow")) {
            return List.of();
        }
        if (!(sender instanceof Player player) || !hasAnyPermission(player, COMMAND_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return completeFirstArgument(player, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("default") && hasAnyPermission(player, DEFAULT_PERMISSION)) {
            return completeColors(player, args[1], DEFAULT_PERMISSION);
        }

        return List.of();
    }

    private List<String> completeFirstArgument(Player player, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();
        addIfAllowed(completions, "on", prefix, player, TOGGLE_PERMISSION);
        addIfAllowed(completions, "off", prefix, player, TOGGLE_PERMISSION);
        addIfAllowed(completions, "list", prefix, player, LIST_PERMISSION);
        addIfAllowed(completions, "default", prefix, player, DEFAULT_PERMISSION);
        completions.addAll(completeColors(player, prefix, "glowplayer.color.*"));
        return completions;
    }

    private List<String> completeColors(Player player, String input, String... extraPermissions) {
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();
        for (GlowColor color : GlowColor.values()) {
            List<String> permissions = new ArrayList<>(List.of(extraPermissions));
            permissions.add(color.permission());
            addIfAllowed(completions, color.key(), prefix, player, permissions.toArray(String[]::new));
        }
        return completions;
    }

    private void toggleGlow(Player player) {
        if (!hasAnyPermission(player, TOGGLE_PERMISSION)) {
            deny(player);
            return;
        }

        GlowState current = stateFor(player);
        GlowState next = new GlowState(!current.enabled(), current.color());
        glowStates.put(player.getUniqueId(), next);
        applyGlow(player, next);
        player.sendMessage(ChatColor.GREEN + "グローを" + (next.enabled() ? "オン" : "オフ") + "にしました。");
    }

    private void setGlowEnabled(Player player, boolean enabled) {
        if (!hasAnyPermission(player, TOGGLE_PERMISSION)) {
            deny(player);
            return;
        }

        GlowState current = stateFor(player);
        GlowState next = new GlowState(enabled, current.color());
        glowStates.put(player.getUniqueId(), next);
        applyGlow(player, next);
        player.sendMessage(ChatColor.GREEN + "グローを" + (enabled ? "オン" : "オフ") + "にしました。");
    }

    private void setGlowColor(Player player, String requested) {
        GlowColor color = GlowColor.fromInput(requested);
        if (color == null) {
            player.sendMessage(ChatColor.RED + "未知の色です: " + requested);
            sendColorList(player);
            return;
        }

        if (!hasAnyPermission(player, color.permission(), "glowplayer.color.*")) {
            deny(player);
            return;
        }

        GlowState next = new GlowState(true, color);
        glowStates.put(player.getUniqueId(), next);
        applyGlow(player, next);
        player.sendMessage(ChatColor.GREEN + "グロー色を " + color.displayName() + ChatColor.GREEN + " にしました。");
    }

    private void setDefaultColor(CommandSender sender, String requested) {
        if (!hasAnyPermission(sender, DEFAULT_PERMISSION)) {
            deny(sender);
            return;
        }

        GlowColor color = GlowColor.fromInput(requested);
        if (color == null) {
            sender.sendMessage(ChatColor.RED + "未知のデフォルト色です: " + requested);
            sender.sendMessage(ChatColor.GRAY + "使用可能な色: " + String.join(", ", GlowColor.keys()));
            return;
        }

        defaultColor = color;
        getConfig().set(DEFAULT_COLOR_CONFIG_PATH, color.key());
        saveConfig();
        sender.sendMessage(ChatColor.GREEN + "デフォルトのグロー色を " + color.displayName() + ChatColor.GREEN + " に保存しました。");
    }

    private void sendDefaultColor(CommandSender sender) {
        if (!hasAnyPermission(sender, DEFAULT_PERMISSION)) {
            deny(sender);
            return;
        }

        sender.sendMessage(ChatColor.AQUA + "現在のデフォルトグロー色: " + defaultColor.displayName());
        sender.sendMessage(ChatColor.GRAY + "変更: /glow default <色>");
    }

    private void sendColorList(Player player) {
        if (!hasAnyPermission(player, LIST_PERMISSION)) {
            deny(player);
            return;
        }

        List<String> allowedColors = new ArrayList<>();
        for (GlowColor color : GlowColor.values()) {
            if (hasAnyPermission(player, color.permission(), "glowplayer.color.*")) {
                allowedColors.add(color.displayName() + ChatColor.RESET);
            }
        }

        if (allowedColors.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "使用可能なグロー色はありません。");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "使用可能なグロー色: " + String.join(ChatColor.GRAY + ", ", allowedColors));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "使い方: /glow [on|off|list|default|色]");
        sender.sendMessage(ChatColor.GRAY + "例: /glow blue, /glow red, /glow off, /glow default blue");
    }

    private void deny(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "この操作を実行する権限がありません。");
    }

    private boolean hasAnyPermission(CommandSender sender, String... permissions) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            return true;
        }
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    private GlowState stateFor(Player player) {
        return glowStates.computeIfAbsent(player.getUniqueId(), uuid -> new GlowState(true, defaultColor));
    }

    String glowColorCode(UUID playerId) {
        GlowState state = glowStates.get(playerId);
        GlowColor color = state == null ? defaultColor : state.color();
        return "&" + color.chatColor().getChar();
    }

    String defaultGlowColorCode() {
        return "&" + defaultColor.chatColor().getChar();
    }

    boolean isGlowEnabled(UUID playerId) {
        GlowState state = glowStates.get(playerId);
        return state == null || state.enabled();
    }

    private void registerPlaceholderExpansion() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (tabCompatibilityMode) {
                getLogger().warning("PlaceholderAPI is required for TAB glow colors. Install it and configure %glowplayer_glowcolor% in TAB.");
            }
            return;
        }

        if (new GlowPlayerPlaceholderExpansion(this).register()) {
            getLogger().info("Registered PlaceholderAPI placeholders for TAB compatibility.");
        } else {
            getLogger().warning("Could not register PlaceholderAPI placeholders.");
        }
    }

    private void loadDefaultColor() {
        String configuredColor = getConfig().getString(DEFAULT_COLOR_CONFIG_PATH, FALLBACK_DEFAULT_COLOR.key());
        GlowColor loadedColor = GlowColor.fromInput(configuredColor);
        if (loadedColor == null) {
            getLogger().warning("Invalid default-color '" + configuredColor + "' in config.yml. Falling back to " + FALLBACK_DEFAULT_COLOR.key() + ".");
            loadedColor = FALLBACK_DEFAULT_COLOR;
            getConfig().set(DEFAULT_COLOR_CONFIG_PATH, loadedColor.key());
            saveConfig();
        }
        defaultColor = loadedColor;
    }

    private void applyGlow(Player player, GlowState state) {
        player.setGlowing(state.enabled());
        if (tabCompatibilityMode) {
            return;
        }

        clearGlowTeam(player);
        if (!state.enabled()) {
            return;
        }

        Team current = scoreboard.getEntityTeam(player);
        if (current != null && !isGlowTeam(current)) {
            previousTeams.putIfAbsent(player.getUniqueId(), current.getName());
            current.removeEntity(player);
        }
        glowTeams.get(state.color()).addEntity(player);
    }

    private void clearGlowTeam(Player player) {
        if (tabCompatibilityMode || scoreboard == null) {
            return;
        }

        for (Team team : glowTeams.values()) {
            if (team.hasEntity(player)) {
                team.removeEntity(player);
            }
        }

        String previousTeamName = previousTeams.remove(player.getUniqueId());
        if (previousTeamName == null || scoreboard.getEntityTeam(player) != null) {
            return;
        }

        Team previousTeam = scoreboard.getTeam(previousTeamName);
        if (previousTeam != null) {
            previousTeam.addEntity(player);
        }
    }

    private void registerGlowTeams() {
        for (GlowColor color : GlowColor.values()) {
            String teamName = "gp_" + color.key();
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }
            team.color(color.namedTextColor());
            glowTeams.put(color, team);
        }
    }

    private boolean isGlowTeam(Team team) {
        return team.getName().startsWith("gp_");
    }

    private void addIfAllowed(List<String> completions, String candidate, String prefix, Player player, String... permissions) {
        if (candidate.startsWith(prefix) && hasAnyPermission(player, permissions)) {
            completions.add(candidate);
        }
    }

    private record GlowState(boolean enabled, GlowColor color) {
    }

    private enum GlowColor {
        WHITE("white", ChatColor.WHITE, "white"),
        GRAY("gray", ChatColor.GRAY, "gray", "grey"),
        DARK_GRAY("dark_gray", ChatColor.DARK_GRAY, "dark_gray", "darkgrey", "darkgray"),
        BLACK("black", ChatColor.BLACK, "black"),
        RED("red", ChatColor.RED, "red"),
        DARK_RED("dark_red", ChatColor.DARK_RED, "dark_red", "darkred"),
        GOLD("gold", ChatColor.GOLD, "gold", "orange"),
        YELLOW("yellow", ChatColor.YELLOW, "yellow"),
        GREEN("green", ChatColor.GREEN, "green"),
        DARK_GREEN("dark_green", ChatColor.DARK_GREEN, "dark_green", "darkgreen"),
        AQUA("aqua", ChatColor.AQUA, "aqua", "cyan"),
        DARK_AQUA("dark_aqua", ChatColor.DARK_AQUA, "dark_aqua", "darkcyan", "darkaqua"),
        BLUE("blue", ChatColor.BLUE, "blue"),
        DARK_BLUE("dark_blue", ChatColor.DARK_BLUE, "dark_blue", "darkblue"),
        LIGHT_PURPLE("light_purple", ChatColor.LIGHT_PURPLE, "light_purple", "pink", "magenta"),
        DARK_PURPLE("dark_purple", ChatColor.DARK_PURPLE, "dark_purple", "purple");

        private final String key;
        private final ChatColor chatColor;
        private final NamedTextColor namedTextColor;
        private final List<String> aliases;

        GlowColor(String key, ChatColor chatColor, String... aliases) {
            this.key = key;
            this.chatColor = chatColor;
            this.namedTextColor = NamedTextColor.NAMES.value(key);
            this.aliases = List.of(aliases);
        }

        private static GlowColor fromInput(String input) {
            String normalized = input.toLowerCase(Locale.ROOT).replace('-', '_');
            for (GlowColor color : values()) {
                if (color.aliases.contains(normalized)) {
                    return color;
                }
            }
            return null;
        }

        private static List<String> keys() {
            List<String> keys = new ArrayList<>();
            for (GlowColor color : values()) {
                keys.add(color.key());
            }
            return keys;
        }

        private String key() {
            return key;
        }

        private NamedTextColor namedTextColor() {
            return namedTextColor;
        }

        private ChatColor chatColor() {
            return chatColor;
        }

        private String permission() {
            return "glowplayer.color." + key;
        }

        private String displayName() {
            return chatColor + key;
        }
    }
}
