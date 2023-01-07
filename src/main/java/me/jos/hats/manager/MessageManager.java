package me.jos.hats.manager;

import me.jos.hats.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    private final Main main;
    private FileConfiguration messagesConfig;
    private final FileConfiguration defaultMessagesConfig;

    // Pattern for hex color codes
    private final Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");

    public MessageManager(Main main) {
        this.main = main;
        InputStream messagesStream = main.getClass().getResourceAsStream("/messages.yml");
        assert messagesStream != null;
        InputStreamReader streamReader = new InputStreamReader(messagesStream);
        defaultMessagesConfig = YamlConfiguration.loadConfiguration(streamReader);

        createMessagesConfig();
    }

    public FileConfiguration getMessagesConfig() {
        return this.messagesConfig;
    }

    public void createMessagesConfig() {
        File messagesConfigFile = new File(main.getDataFolder(), "messages.yml");
        if (!messagesConfigFile.exists()) {
            main.saveResource("messages.yml", false);
        }

        messagesConfig = new YamlConfiguration();
        try {
            messagesConfig.load(messagesConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String msgConfOption) {
        String msg = getMessagesConfig().getString(msgConfOption);
        if(msg == null) {
            msg = defaultMessagesConfig.getString(msgConfOption);
            main.getLogger().warning("\"" + msgConfOption + "\" could not be found in messages.yml. Resorting to default value...");
        }
        assert msg != null;
        return formatMessage(msg);
    }

    public String getPlayerMessage(String msgConfOption, ItemStack hat) {
        String prefix = this.getMessagesConfig().getString("prefix");
        if(prefix == null) {
            prefix = defaultMessagesConfig.getString("prefix");
            main.getLogger().warning("\"prefix\" could not be found in messages.yml. Resorting to default value...");
        }
        String suffix = this.getMessagesConfig().getString("suffix");
        if(suffix == null) {
            suffix = defaultMessagesConfig.getString("suffix");
            main.getLogger().warning("\"suffix\" could not be found in messages.yml. Resorting to default value...");
        }
        String msg = getMessagesConfig().getString(msgConfOption);
        if(msg == null) {
            msg = defaultMessagesConfig.getString(msgConfOption);
            main.getLogger().warning("\"" + msgConfOption + "\" could not be found in messages.yml. Resorting to default value...");
        }
        if(hat != null) {
            ItemMeta meta = hat.getItemMeta();
            if(meta != null) {
                assert msg != null;
                msg = msg.replaceAll("\\{hat}", meta.getDisplayName());
            }
        }
        return formatMessage(prefix + suffix + msg);
    }

    // Formats the given message with color codes, including hex
    public String formatMessage(String msg) {
        Matcher match = pattern.matcher(msg);
        while(match.find()) {
            String code = msg.substring(match.start()+1, match.end());
            msg = msg.replace('&' + code, ChatColor.of(code) + "");
            match = pattern.matcher(msg);
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
