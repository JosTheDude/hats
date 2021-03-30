package me.Tonus_.hatCosmetics;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryManager {
    private final Main main;

    public InventoryManager(Main main) {
        this.main = main;
    }

    public void initHats() {
        ConfigurationSection cosmeticsSection = main.getConfig().getConfigurationSection("hats");
        if(cosmeticsSection == null) {
            main.getLogger().severe("The hats section of the config is missing! Delete your file and restart the server to regenerate.");
            return;
        }
        String configItem = main.getConfig().getString("item");
        if(configItem == null) {
            main.getLogger().warning("An item was not provided in the config!");
            return;
        }
        Material material = Material.matchMaterial(configItem);
        if(material == null) {
            main.getLogger().warning("The item '" + configItem + "' is not a material! Please check Spigot-API materials.");
            return;
        }

        for(String cosmetics : cosmeticsSection.getKeys(false)) {
            ItemStack hatItem;
            String hatItemTypeString = main.getConfig().getString("hats." + cosmetics + ".item");
            Material hatMaterial;
            if(hatItemTypeString != null) {
                hatMaterial = Material.matchMaterial(hatItemTypeString);
                if(hatMaterial != null) hatItem = new ItemStack(hatMaterial);
                else {
                    main.getLogger().warning("The item '" + hatItemTypeString + "' is not a material! Please check Spigot-API materials.");
                    hatItem = new ItemStack(material);
                }
            }
            else {
                hatItem = new ItemStack(material);
            }
            ItemMeta hatMeta = hatItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Hat Cosmetic");
            lore.add(" ");
            lore.add(ChatColor.AQUA + "Click to equip");
            assert hatMeta != null;
            hatMeta.setLore(lore);
            hatMeta.setCustomModelData(main.getConfig().getInt("hats." + cosmetics + ".data"));
            String name = main.getConfig().getString("hats." + cosmetics + ".name");
            if(name == null) {
                main.getLogger().warning("The item '" + configItem + "' does not have a name defined!");
                continue;
            }
            hatMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            hatItem.setItemMeta(hatMeta);
            NBTItem nbti = new NBTItem(hatItem);
            nbti.setString("Permission", "hatcosmetics.hat." + cosmetics);
            hatItem = nbti.getItem();
            Main.hats.put(cosmetics, hatItem);
        }
    }

    public Inventory openInv() {
        int invRows = main.getConfig().getInt("gui_rows");
        if(invRows < 1 || invRows > 4) {
            main.getLogger().warning("The GUI size is invalid! Defaulting to 4 rows...");
            invRows = 4;
        }
        Inventory inv = Bukkit.createInventory(null, (invRows*9)+18, ChatColor.AQUA + "" + ChatColor.BOLD + "Hats");

        // Start items & make border items
        String configItem = main.getConfig().getString("border");
        if(configItem == null) {
            main.getLogger().warning("A border was not provided in the config!");
            return null;
        }
        Material material = Material.matchMaterial(configItem);
        if(material == null) {
            main.getLogger().warning("The item '" + configItem + "' is not a material! Please check Spigot-API materials.");
            return null;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta;

        if(item.getItemMeta() != null) {
            meta = item.getItemMeta();
            meta.setDisplayName(" ");
            item.setItemMeta(meta);

            for(int i = 0; i < 9; i++) {
                inv.setItem(i, item);
            }
            for(int i = 0; i < 9; i++) {
                inv.setItem((invRows+1)*9 + i, item);
            }
        }

        // Create close menu item
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        assert closeMeta != null;
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lClose Menu"));
        closeItem.setItemMeta(closeMeta);
        inv.setItem((invRows+1)*9 + 4, closeItem);

        // Display cosmetics
        int slot = 9;
        List<ItemStack> hatItems = new ArrayList<>(Main.hats.values());
        Collections.reverse(hatItems);
        for(ItemStack hatItem : hatItems) {
            if(slot-8 > invRows*9) {
                main.getLogger().warning("Hats are going beyond the GUI size! Please increase 'gui_rows' or reduce the amount of hats.");
                return inv;
            }
            inv.setItem(slot, hatItem);
            slot++;
        }
        return inv;
    }

}
