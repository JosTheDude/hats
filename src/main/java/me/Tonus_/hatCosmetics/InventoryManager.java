package me.Tonus_.hatCosmetics;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryManager {
    private final Main main;
    private final MessageManager messageManager;
    private final ArrayList<String> hatOrder = new ArrayList<>();

    public InventoryManager(Main main) {
        this.main = main;
        this.messageManager = main.messageManager;
    }

    public void initHats() {
        hatOrder.clear();
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
            lore.add(messageManager.getMessage("hat_equip"));
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
            hatOrder.add(cosmetics);
        }
    }

    public Inventory openInv(Player player) {
        int invRows = main.getConfig().getInt("gui_rows");
        if(invRows < 1 || invRows > 4) {
            main.getLogger().warning("The GUI size is invalid! Defaulting to 4 rows...");
            invRows = 4;
        }
        Inventory inv = Bukkit.createInventory(null, (invRows*9)+18, messageManager.getMessage("gui_title"));

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

        // Check for equipped hat
        String currentHat = null;
        if (player.getEquipment() != null && player.getEquipment().getHelmet() != null) {
            if (player.getEquipment().getHelmet().getItemMeta() != null && player.getEquipment().getHelmet().getItemMeta().getLore() != null &&
                    player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                ItemStack helmet = player.getEquipment().getHelmet();
                NBTItem nbti = new NBTItem(helmet);
                currentHat = Arrays.asList(nbti.getString("Permission").split("\\.")).get(2);
            }
        }

        // Display cosmetics
        int slot = 9;
        for(String hat : hatOrder) {
            ItemStack GUIItem = new ItemStack(Main.hats.get(hat));
            if(slot-8 > invRows*9) {
                main.getLogger().warning("Hats are going beyond the GUI size! Please increase 'gui_rows' or reduce the amount of hats.");
                return inv;
            }
            if(currentHat != null) {
                NBTItem nbti = new NBTItem(GUIItem);
                if(nbti.getString("Permission").equals("hatcosmetics.hat." + currentHat)) {
                    GUIItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                    ItemMeta hatMeta = GUIItem.getItemMeta();
                    assert hatMeta != null;
                    List<String> lore = hatMeta.getLore();
                    assert lore != null;
                    lore.set(lore.size()-1, messageManager.getMessage("hat_unequip"));
                    hatMeta.setLore(lore);
                    hatMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    GUIItem.setItemMeta(hatMeta);
                }
            }
            inv.setItem(slot, GUIItem);
            slot++;
        }
        return inv;
    }

}
