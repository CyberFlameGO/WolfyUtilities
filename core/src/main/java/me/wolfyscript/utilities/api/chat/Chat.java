package me.wolfyscript.utilities.api.chat;

import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.language.LanguageAPI;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.Pair;
import me.wolfyscript.utilities.util.chat.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat {

    public static final Map<UUID, PlayerAction> CLICK_DATA_MAP = new HashMap<>();

    private String CONSOLE_PREFIX;
    private String IN_GAME_PREFIX;


    private final WolfyUtilities wolfyUtilities;
    private final LanguageAPI languageAPI;
    private final Plugin plugin;

    public Chat(WolfyUtilities wolfyUtilities) {
        this.wolfyUtilities = wolfyUtilities;
        this.languageAPI = wolfyUtilities.getLanguageAPI();
        this.plugin = wolfyUtilities.getPlugin();
        this.CONSOLE_PREFIX = "[" + plugin.getName() + "]";
        this.IN_GAME_PREFIX = this.CONSOLE_PREFIX;
    }

    public String getIN_GAME_PREFIX() {
        return IN_GAME_PREFIX;
    }

    public void setCONSOLE_PREFIX(String CONSOLE_PREFIX) {
        this.CONSOLE_PREFIX = CONSOLE_PREFIX;
    }

    public void setIN_GAME_PREFIX(String IN_GAME_PREFIX) {
        this.IN_GAME_PREFIX = IN_GAME_PREFIX;
    }

    public String getCONSOLE_PREFIX() {
        return CONSOLE_PREFIX;
    }

    public void sendConsoleMessage(String message) {
        message = CONSOLE_PREFIX + languageAPI.replaceKeys(message);
        message = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public void sendConsoleWarning(String message) {
        sendConsoleMessage("[WARN] " + message);
    }

    public void sendConsoleMessage(String message, String... replacements) {
        message = CONSOLE_PREFIX + languageAPI.replaceKeys(message);
        List<String> keys = new ArrayList<>();
        Pattern pattern = Pattern.compile("%([A-Z]*?)(_*?)%");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            keys.add(matcher.group(0));
        }
        for (int i = 0; i < keys.size(); i++) {
            message = message.replace(keys.get(i), replacements[i]);
        }
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.convert(message));
    }

    public void sendConsoleMessage(String message, String[]... replacements) {
        if (replacements != null) {
            message = IN_GAME_PREFIX + languageAPI.replaceColoredKeys(message);
            for (String[] replace : replacements) {
                if (replace.length > 1) {
                    message = message.replaceAll(replace[0], replace[1]);
                }
            }
        }
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.convert(message));
    }

    public void sendMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(ChatColor.convert(IN_GAME_PREFIX + languageAPI.replaceKeys(message)));
        }
    }

    public void sendMessages(Player player, String... messages) {
        if (player != null) {
            for (String message : messages) {
                player.sendMessage(ChatColor.convert(IN_GAME_PREFIX + languageAPI.replaceKeys(message)));
            }
        }
    }

    @SafeVarargs
    public final void sendMessage(Player player, String message, Pair<String, String>... replacements) {
        if (player == null) return;
        if (replacements != null) {
            message = IN_GAME_PREFIX + languageAPI.replaceColoredKeys(message);
            for (Pair<String, String> pair : replacements) {
                message = message.replaceAll(pair.getKey(), pair.getValue());
            }
        }
        player.sendMessage(ChatColor.convert(message));
    }

    /*
    Sends a global message from an GuiCluster to the player!
     */
    public void sendKey(Player player, String clusterID, String msgKey) {
        sendMessage(player, "$inventories." + clusterID + ".global_messages." + msgKey + "$");
    }

    public void sendKey(Player player, GuiCluster<?> guiCluster, String msgKey) {
        sendMessage(player, "$inventories." + guiCluster.getId() + ".global_messages." + msgKey + "$");
    }

    public void sendKey(Player player, NamespacedKey namespacedKey, String msgKey) {
        sendMessage(player, "$inventories." + namespacedKey.getNamespace() + "." + namespacedKey.getKey() + ".messages." + msgKey + "$");
    }

    @SafeVarargs
    public final void sendKey(Player player, GuiCluster<?> guiCluster, String msgKey, Pair<String, String>... replacements) {
        sendMessage(player, "$inventories." + guiCluster.getId() + ".global_messages." + msgKey + "$", replacements);
    }

    @SafeVarargs
    public final void sendKey(Player player, NamespacedKey namespacedKey, String msgKey, Pair<String, String>... replacements) {
        sendMessage(player, "$inventories." + namespacedKey.getNamespace() + "." + namespacedKey.getKey() + ".messages." + msgKey + "$", replacements);
    }

    public void sendActionMessage(Player player, ClickData... clickData) {
        TextComponent[] textComponents = getActionMessage(IN_GAME_PREFIX, player, clickData);
        player.spigot().sendMessage(textComponents);
    }

    public TextComponent[] getActionMessage(String prefix, Player player, ClickData... clickData) {
        TextComponent[] textComponents = new TextComponent[clickData.length + 1];
        textComponents[0] = new TextComponent(prefix);
        for (int i = 1; i < textComponents.length; i++) {
            ClickData data = clickData[i - 1];
            TextComponent component = new TextComponent(languageAPI.replaceColoredKeys(data.getMessage()));
            if (data.getClickAction() != null) {
                UUID id = UUID.randomUUID();
                while (CLICK_DATA_MAP.containsKey(id)) {
                    id = UUID.randomUUID();
                }
                PlayerAction playerAction = new PlayerAction(wolfyUtilities, player, data);
                CLICK_DATA_MAP.put(id, playerAction);
                component.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/wua " + id.toString()));
            }
            for (ChatEvent<?, ?> chatEvent : data.getChatEvents()) {
                if (chatEvent instanceof HoverEvent) {
                    component.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(((HoverEvent) chatEvent).getAction(), ((HoverEvent) chatEvent).getValue()));
                } else if (chatEvent instanceof me.wolfyscript.utilities.api.chat.ClickEvent) {
                    component.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(((me.wolfyscript.utilities.api.chat.ClickEvent) chatEvent).getAction(), ((me.wolfyscript.utilities.api.chat.ClickEvent) chatEvent).getValue()));
                }
            }
            textComponents[i] = component;
        }
        return textComponents;
    }

    public void sendDebugMessage(String message) {
        if (wolfyUtilities.hasDebuggingMode()) {
            String prefix = org.bukkit.ChatColor.translateAlternateColorCodes('&', this.IN_GAME_PREFIX);
            message = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
            if (message.length() > 70) {
                int count = message.length() / 70;
                for (int text = 0; text <= count; text++) {
                    Bukkit.getServer().getConsoleSender().sendMessage(prefix + (text < count ? message.substring(text * 70, 70 + 70 * text) : message.substring(text * 70)));
                }
            } else {
                message = prefix + message;
                Bukkit.getServer().getConsoleSender().sendMessage(message);
            }
        }
    }

    public static class ChatListener implements Listener {

        @EventHandler
        public void actionRemoval(PlayerQuitEvent event) {
            CLICK_DATA_MAP.keySet().removeIf(uuid -> CLICK_DATA_MAP.get(uuid).getUuid().equals(event.getPlayer().getUniqueId()));
        }
    }

}
