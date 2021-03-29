package me.Tonus_.hatCosmetics;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    public Inventory inv;
    public HashMap<Player, ItemStack> droppedCosmetic = new HashMap<>();
    public static HashMap<String, ItemStack> hats = new HashMap<>();

    public FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        createMessagesConfig();
        createInv();
        Objects.requireNonNull(getCommand("hatcosmetics")).setTabCompleter(new HatCosmeticTab());
        Objects.requireNonNull(getCommand("hatcosmetics")).setExecutor(new MainHatsCommand(this));
    }

    @Override
    public void onDisable() {

    }

    public FileConfiguration getMessagesConfig() {
        return this.messagesConfig;
    }

    public void createMessagesConfig() {
        File messagesConfigFile = new File(getDataFolder(), "messages.yml");
        if (!messagesConfigFile.exists()) {
            messagesConfigFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        messagesConfig= new YamlConfiguration();
        try {
            messagesConfig.load(messagesConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    // Ensure cosmetics don't drop if the player dies
    @EventHandler
    public void onDrop(PlayerDeathEvent event) {
        // Check if player had cosmetic before trying to remove drop
        if(event.getKeepInventory()) return;
        if(event.getEntity().getEquipment() == null) return;
        if(event.getEntity().getEquipment().getHelmet() == null) return;
        if(event.getEntity().getEquipment().getHelmet().getItemMeta() == null) return;
        if(event.getEntity().getEquipment().getHelmet().getItemMeta().getLore() == null) return;
        if(event.getEntity().getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
            // Find cosmetic location and remove drop
            int i = 0;
            for(ItemStack item : event.getDrops()) {
                if(item.getItemMeta() == null) {
                    i++;
                    continue;
                }
                if(item.getItemMeta().getLore() == null) {
                    i++;
                    continue;
                }
                if(item.getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                    event.getDrops().remove(i);
                    // Prepare for retrieval
                    droppedCosmetic.put(event.getEntity(), item);
                    return;
                }
                else i++;
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(!droppedCosmetic.containsKey(event.getPlayer())) return;
        Player player = event.getPlayer();
        if(player.getEquipment() == null) return;
        player.getEquipment().setHelmet(droppedCosmetic.get(player));
        droppedCosmetic.remove(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
            if(event.getCursor() != null &&
                    event.getCursor().getItemMeta() != null &&
                    event.getCursor().getItemMeta().getLore() != null &&
                    event.getCursor().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                if(event.getCurrentItem() != null && !event.getCurrentItem().getType().toString().contains("HELMET")) {
                    event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        if(!event.getInventory().equals(inv)) {
            if(event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) return;
            if(event.getCurrentItem().getItemMeta().getLore() == null) return;
            if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) event.setCancelled(true);
        } else {
            if(!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if(event.getSlot() >= 9 && event.getSlot() < event.getInventory().getSize()-9 && event.getCurrentItem().getItemMeta().getLore() != null &&
            event.getCurrentItem().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                String header = this.getMessagesConfig().getString("prefix")+this.getMessagesConfig().getString("suffix");
                helmCheck: {
                    if (player.getEquipment() != null && player.getEquipment().getHelmet() != null) {
                        if (player.getEquipment().getHelmet().getItemMeta() != null && player.getEquipment().getHelmet().getItemMeta().getLore() != null &&
                                player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic"))
                            break helmCheck;
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + this.getMessagesConfig().getString("helmet_exist")));
                        player.closeInventory();
                        return;
                    }
                }
                ItemStack item = event.getCurrentItem();
                NBTItem nbti = new NBTItem(item);
                if(player.hasPermission(nbti.getString("Permission"))) {
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Hat Cosmetic");
                    meta.setLore(lore);
                    item.setItemMeta(meta);

                    player.getEquipment().setHelmet(item);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + this.getMessagesConfig().getString("hat_success")));
                    player.closeInventory();
                    return;
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + this.getMessagesConfig().getString("no_hat_permission")));
            } else if(event.getSlot() == event.getInventory().getSize()-5 && event.getCurrentItem().getType().equals(Material.BARRIER) && event.getCurrentItem().getItemMeta().getDisplayName().contains("Close Menu")) {
                player.closeInventory();
            }
        }
    }

    public void createInv() {
        int invRows = getConfig().getInt("gui_rows");
        if(invRows < 1 || invRows > 4) {
            getLogger().warning("The GUI size is invalid! Defaulting to 4 rows...");
            invRows = 4;
        }
        inv = Bukkit.createInventory(null, (invRows*9)+18, ChatColor.AQUA + "" + ChatColor.BOLD + "Hats");

        // Start items & make border items
        String configItem = getConfig().getString("border");
        if(configItem == null) {
            getLogger().warning("A border was not provided in the config!");
            return;
        }
        Material material = Material.matchMaterial(configItem);
        if(material == null) {
            getLogger().warning("The item '" + configItem + "' is not a material! Please check Spigot-API materials.");
            return;
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
        inv.setItem((invRows+1)*9 + 4, item);

        // Display cosmetics
        configItem = getConfig().getString("item");
        if(configItem == null) {
            getLogger().warning("An item was not provided in the config!");
            return;
        }
        material = Material.matchMaterial(configItem);
        if(material == null) {
            getLogger().warning("The item '" + configItem + "' is not a material! Please check Spigot-API materials.");
            return;
        }
        int i = 9;
        ConfigurationSection cosmeticsSection = getConfig().getConfigurationSection("hats");
        if(cosmeticsSection == null) {
            getLogger().severe("The hats section of the config is missing! Delete your file and restart the server to regenerate.");
            return;
        }

        for(String cosmetics : cosmeticsSection.getKeys(false)) {
            ItemStack hatItem;
            String hatItemTypeString = getConfig().getString("hats." + cosmetics + ".item");
            Material hatMaterial;
            if(i-8 > invRows*9) {
                getLogger().warning("Hats are going beyond the GUI size! Please increase 'gui_rows' or reduce the amount of hats.");
                return;
            }
            if(hatItemTypeString != null) {
                hatMaterial = Material.matchMaterial(hatItemTypeString);
                if(hatMaterial != null) hatItem = new ItemStack(hatMaterial);
                else {
                    getLogger().warning("The item '" + hatItemTypeString + "' is not a material! Please check Spigot-API materials.");
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
            hatMeta.setCustomModelData(getConfig().getInt("hats." + cosmetics + ".data"));
            String name = getConfig().getString("hats." + cosmetics + ".name");
            if(name == null) {
                getLogger().warning("The item '" + configItem + "' does not have a name defined!");
                continue;
            }
            hatMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            hatItem.setItemMeta(hatMeta);
            NBTItem nbti = new NBTItem(hatItem);
            nbti.setString("Permission", "hatcosmetics.hat." + cosmetics);
            hatItem = nbti.getItem();
            hats.put(cosmetics, hatItem); // Will later be moved to reload for player specific GUIs
            inv.setItem(i, hatItem);
            i++;
        }
    }
}