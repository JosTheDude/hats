package me.Tonus_.hatCosmetics;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    public Inventory inv;
    public HashMap<Player, ItemStack> droppedCosmetic = new HashMap<>();
    public static HashMap<String, ItemStack> hats = new HashMap<>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        createInv();
        Objects.requireNonNull(this.getCommand("hatcosmetics")).setTabCompleter(new HatCosmeticTab());
    }

    @Override
    public void onDisable() {

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(label.equalsIgnoreCase("hatcosmetics") || label.equalsIgnoreCase("hats")) {
            if(args.length == 0) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage("You cannot do this in console!");
                    return true;
                }
                Player player = (Player) sender;
                // Open GUI
                player.openInventory(inv);
                return true;
            } else {
                if(args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&8&m------------&8[ &3&lHats &8]&m------------\n"
                                    + "&f/hats&7: Opens hat GUI\n"
                                    + "&f/hats equip <hat> &8[player]&7: Equips specified hat\n"
                                    + "&f/hats unequip &8[player]&7: Unequips current hat\n"
                                    + "&f/hats help&7: Displays this text"));
                    if(sender.hasPermission("hatcosmetics.reload")) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/hats reload&7: Reloads plugin"));
                }
                else if(args[0].equalsIgnoreCase("equip")) {
                    // Check if a hat is specified and if it exists
                    if(!(args.length > 1)) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou must specify a hat to equip!"));
                        return true;
                    } else if(!hats.containsKey(args[1])) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bThat hat does not exist!"));
                        return true;
                    }
                    Player player;
                    if(args.length > 2) {
                        if(!sender.hasPermission("hatcosmetics.equip.other")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou do not have permission to do this."));
                            return true;
                        }
                        player = getServer().getPlayer(args[2]);
                        if(player == null || !player.isOnline()) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bThat player is not currently online!"));
                            return true;
                        }
                    } else {
                        if(!(sender instanceof Player)) {
                            sender.sendMessage("You cannot do this in console!");
                            return true;
                        }
                        player = (Player) sender;
                    }
                    if(player.getEquipment() == null) return true;

                    // First check if the player has permission to the hat
                    ItemStack item = hats.get(args[1]);
                    NBTItem nbti = new NBTItem(item);
                    if(!player.hasPermission(nbti.getString("permission"))) {
                        if (args.length > 2) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &b" + player.getName() + "&b does not have permission to equip the hat."));
                        else player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou do not have permission to equip that hat."));
                        return true;
                    }

                    // Then check if the player has a helmet equipped and cancel if so
                    if (player.getEquipment().getHelmet() != null) {
                        if (player.getEquipment().getHelmet().getItemMeta() != null || player.getEquipment().getHelmet().getItemMeta().getLore() != null ||
                        !(player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic"))) {
                            if (args.length > 2) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &b" + player.getName() + "&b already has something on their head!"));
                            else player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bSomething else is already on your head!"));
                            return true;
                        }
                    }

                    // Set player's helmet slot to specified hat
                    player.getEquipment().setHelmet(item);
                    if (args.length > 2) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &b" + player.getName() + "&b equipped the hat successfully!"));
                    else player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou equipped your hat successfully!"));
                }
                else if(args[0].equalsIgnoreCase("unequip")) {
                    Player player;
                    if(args.length > 1) {
                        if(!sender.hasPermission("hatcosmetics.unequip.other")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou do not have permission to do this."));
                            return true;
                        }
                        player = getServer().getPlayer(args[1]);
                        if(player == null || !player.isOnline()) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bThat player is not currently online!"));
                            return true;
                        }
                    } else {
                        if(!(sender instanceof Player)) {
                            sender.sendMessage("You cannot do this in console!");
                            return true;
                        }
                        player = (Player) sender;
                    }
                    // Take cosmetic off if it exists
                    if(player.getEquipment() == null || player.getEquipment().getHelmet() == null ||
                    player.getEquipment().getHelmet().getItemMeta() == null || player.getEquipment().getHelmet().getItemMeta().getLore() == null) return true;
                    if(player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                        player.getEquipment().setHelmet(new ItemStack(Material.AIR));
                        if(args.length > 1) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &b" + player.getName() + "&b's hat has been unequipped!"));
                        else player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYour hat has been unequipped!"));
                        return true;
                    }
                    if(args.length > 1) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &b" + player.getName() + "&b doesn't have a hat equipped!"));
                    else player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou don't have a hat equipped!"));
                }
                else if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("hatcosmetics.reload")) {
                        this.reloadConfig();
                        hats.clear();
                        createInv();
                        if(sender instanceof Player) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bPlugin has been reloaded!"));
                        getLogger().info("Plugin has been reloaded!");
                        return true;
                    }
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou do not have permission to do this."));
                }
            }
        }

        return false;
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
                helmCheck: {
                    if (player.getEquipment() != null && player.getEquipment().getHelmet() != null) {
                        if (player.getEquipment().getHelmet().getItemMeta() != null && player.getEquipment().getHelmet().getItemMeta().getLore() != null &&
                                player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic"))
                            break helmCheck;
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bSomething else is already on your head!"));
                        player.closeInventory();
                        return;
                    }
                }
                ItemStack item = event.getCurrentItem();
                NBTItem nbti = new NBTItem(item);
                if(player.hasPermission(nbti.getString("permission"))) {
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Hat Cosmetic");
                    meta.setLore(lore);
                    item.setItemMeta(meta);

                    player.getEquipment().setHelmet(item);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYour hat was equipped successfully!"));
                    player.closeInventory();
                    return;
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lHats >> &bYou do not have permission to equip this hat!"));
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
        item.setType(Material.BARRIER);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lClose Menu"));
        item.setItemMeta(meta);
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
        item.setType(material);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Hat Cosmetic");
        lore.add(" ");
        lore.add(ChatColor.AQUA + "Click to equip");
        meta.setLore(lore);
        int i = 9;
        ConfigurationSection cosmeticsSection = getConfig().getConfigurationSection("hats");
        if(cosmeticsSection == null) {
            getLogger().severe("The hats section of the config is missing! Delete your file and restart the server to regenerate.");
            return;
        }
        for(String cosmetics : cosmeticsSection.getKeys(false)) {
            if(i-8 > invRows*9) {
                getLogger().warning("Hats are going beyond the GUI size! Please increase 'gui_rows' or reduce the amount of hats.");
                return;
            }
            meta.setCustomModelData(getConfig().getInt("hats." + cosmetics + ".data"));
            String name = getConfig().getString("hats." + cosmetics + ".name");
            if(name == null) {
                getLogger().warning("The item '" + configItem + "' does not have a name defined!");
                continue;
            }
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
            NBTItem nbti = new NBTItem(item);
            nbti.setString("permission", "hatcosmetics.hat." + cosmetics);
            item = nbti.getItem();
            hats.put(cosmetics, item); // Will later be moved to reload for player specific GUIs
            inv.setItem(i, item);
            i++;
        }
    }
}