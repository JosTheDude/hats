package me.Tonus_.hatCosmetics.manager;

import me.Tonus_.hatCosmetics.Main;
import org.bukkit.Material;

public class ConfigManager {
    private final Main main;

    public Material item;
    public Material border;
    public int guiRows;
    public boolean hideHats;

    public ConfigManager(Main main) {
        this.main = main;
        reload();
    }

    // Gets config options and saves them in variables
    public void reload() {
        item = Material.matchMaterial(main.getConfig().getString("item", "FEATHER").toUpperCase());
        border = Material.matchMaterial(main.getConfig().getString("border", "LIGHT_BLUE_STAINED_GLASS_PANE").toUpperCase());
        guiRows = main.getConfig().getInt("gui_rows", 1);
        hideHats = main.getConfig().getBoolean("hide_hats", false);
    }
}
