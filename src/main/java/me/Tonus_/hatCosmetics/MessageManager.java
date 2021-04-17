package me.Tonus_.hatCosmetics;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageManager {
    private final Main main;
    private FileConfiguration messagesConfig;
    private final FileConfiguration defaultMessagesConfig;

    public MessageManager(Main main) {
        this.main = main;
        InputStream messagesStream = main.getClass().getResourceAsStream("/messages.yml");
        InputStreamReader streamReader = new InputStreamReader(messagesStream);
        defaultMessagesConfig = YamlConfiguration.loadConfiguration(streamReader);
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
        return ChatColor.translateAlternateColorCodes('&', msg);
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
        return ChatColor.translateAlternateColorCodes('&', prefix + suffix + msg);
    }
}
