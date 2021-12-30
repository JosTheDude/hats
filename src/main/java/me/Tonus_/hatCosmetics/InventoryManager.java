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

import java.util.*;

public class InventoryManager {
    private final Main main;
    private final MessageManager messageManager;
    private final ArrayList<String> hatOrder = new ArrayList<>();
    private final HashMap<UUID, ArrayList<Integer>> playerHats = new HashMap<>();
    private final HashMap<UUID, Integer> playerGUIPage = new HashMap<>();

    // Returns what page the player is currently on
    public int getPlayerPage(UUID uuid) {
        return playerGUIPage.get(uuid);
    }

    // Sets what page the player is currently on
    public void setPlayerPage(UUID uuid, int page) {
        playerGUIPage.put(uuid, page);
    }

    // Removes the page integer for the given player
    public void removePlayerPage(UUID uuid) {
        playerGUIPage.remove(uuid);
    }

    // Removes the list of hat indexes for the given player
    public void removePlayerHats(UUID uuid) { playerHats.remove(uuid); }

    // Returns a list of hat indexes the given player has access to
    private ArrayList<Integer> getPlayerHats(Player player) {
        // Prepare new array of integers
        ArrayList<Integer> playerHats = new ArrayList<>();

        // Loop through all of the hats in order
        for(int i = 0; i < hatOrder.size(); i++) {
            // Initialize itemstack and its NBT data
            String hatName = hatOrder.get(i);
            ItemStack hatItem = new ItemStack(Main.hats.get(hatName));
            NBTItem nbti = new NBTItem(hatItem);

            // If the player has the permission found in the NBT data, add the index to the list
            if(player.hasPermission(nbti.getString("Permission"))) {
                playerHats.add(i);
            }
        }

        return playerHats;
    }

    public InventoryManager(Main main) {
        this.main = main;
        this.messageManager = main.getMessageManager();
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
            for(String descText : main.getConfig().getStringList("hats." + cosmetics + ".description")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', descText));
            }
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
        boolean hideHats = main.getConfig().getBoolean("hide_hats");

        int hatsLength;

        // If hats are supposed to be hidden to the player, make a list of hats they have access to
        if(hideHats) {
            playerHats.put(player.getUniqueId(), getPlayerHats(player));
            hatsLength = playerHats.get(player.getUniqueId()).size();
        }
        else {
            hatsLength = hatOrder.size();
        }

        if(invRows == -1) {
            double autoRows = (double) hatsLength / 9;
            autoRows = Math.min(autoRows, 4);
            invRows = (int) Math.ceil(autoRows);
        }
        else if(invRows < 1 || invRows > 4) {
            main.getLogger().warning("The GUI size is invalid! Defaulting to 4 rows...");
            invRows = 4;
        }

        Inventory inv;
        int currentPage = 1;
        int pages = 1;

        // If there are multiple pages of hats, be sure to include the page number in the title
        if(hatsLength > invRows*9) {
            // If the GUI is just being opened, create the player's page index
            playerGUIPage.putIfAbsent(player.getUniqueId(), 1);

            currentPage = playerGUIPage.get(player.getUniqueId());
            pages = (int) Math.ceil((double) hatsLength / (invRows*9));
            inv = Bukkit.createInventory(null, (invRows*9)+18, messageManager.getMessage("gui_title") + ChatColor.DARK_GRAY + " (" + currentPage + "/" + pages + ")");
        }
        else {
            inv = Bukkit.createInventory(null, (invRows*9)+18, messageManager.getMessage("gui_title"));
        }

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

        ItemStack GUIItem;

        // If there are multiple pages, add next/prev page buttons
        if(hatsLength > invRows*9) {
            GUIItem = new ItemStack(Material.ARROW);
            ItemMeta GUIMeta = GUIItem.getItemMeta();
            assert GUIMeta != null;

            // If this is not the first page, add a previous page button
            if(playerGUIPage.get(player.getUniqueId()) > 1) {
                GUIMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f&lPrevious Page"));
                GUIItem.setItemMeta(GUIMeta);
                inv.setItem((invRows+1)*9 + 3, GUIItem);
            }

            if(currentPage != pages) {
                GUIMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f&lNext Page"));
                GUIItem.setItemMeta(GUIMeta);
                inv.setItem((invRows+1)*9 + 5, GUIItem);
            }
        }

        for(int i = (currentPage-1)*9; i < hatsLength; i++) {
            String hat;
            // If hiding hats is enabled, get the list of hats from the list of hat integers
            if(hideHats) {
                hat = hatOrder.get(playerHats.get(player.getUniqueId()).get(i));
            }
            // Otherwise, just use the original list of hat strings
            else {
                hat = hatOrder.get(i);
            }

            GUIItem = new ItemStack(Main.hats.get(hat));
            NBTItem nbti = new NBTItem(GUIItem);

            // If there are no more open slots for hats, return the inventory
            if(slot-8 > invRows*9) {
                return inv;
            }

            if(currentHat != null && nbti.getString("Permission").equals("hatcosmetics.hat." + currentHat)) {
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
            inv.setItem(slot, GUIItem);
            slot++;
        }
        if(slot == 9 && hideHats) {
            player.sendMessage(messageManager.getPlayerMessage("no_hats_gui", null));
            return null;
        }
        return inv;
    }

}
