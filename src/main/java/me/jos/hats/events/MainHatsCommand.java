package me.jos.hats.events;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.jos.hats.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainHatsCommand implements CommandExecutor {
    private final Main main;
    private final me.jos.hats.manager.MessageManager messageManager;

    public MainHatsCommand(Main main) {
        this.main = main;
        this.messageManager = main.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if(label.equalsIgnoreCase("hatcosmetics") || label.equalsIgnoreCase("hats")) {
            if(args.length == 0) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage("You cannot do this in console!");
                    return true;
                }
                Player player = (Player) sender;
                // Open GUI
                Inventory inv = main.getInventoryManager().openInv(player);
                if(inv != null) {
                    player.openInventory(inv);
                }
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
                        sender.sendMessage(messageManager.getPlayerMessage("no_hat_given", null));
                        return true;
                    } else if(!Main.hats.containsKey(args[1])) {
                        sender.sendMessage(messageManager.getPlayerMessage("hat_not_exist", null));
                        return true;
                    }
                    Player player;
                    if(args.length > 2) {
                        if(!sender.hasPermission("hatcosmetics.equip.other")) {
                            sender.sendMessage(messageManager.getPlayerMessage("no_permission", null));
                            return true;
                        }
                        player = main.getServer().getPlayer(args[2]);
                        if(player == null || !player.isOnline()) {
                            sender.sendMessage(messageManager.getPlayerMessage("not_online", null));
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
                    ItemStack item = new ItemStack(Main.hats.get(args[1]));
                    NBTItem nbti = new NBTItem(item);
                    if(!player.hasPermission(nbti.getString("Permission"))) {
                        if (args.length > 2) sender.sendMessage(messageManager.getPlayerMessage("no_hat_permission_other", item));
                        else player.sendMessage(messageManager.getPlayerMessage("no_hat_permission", item));
                        return true;
                    }

                    // Then check if the player has a helmet equipped and cancel if so
                    if (player.getEquipment().getHelmet() != null) {
                        if (player.getEquipment().getHelmet().getItemMeta() != null || player.getEquipment().getHelmet().getItemMeta().getLore() != null ||
                                !(player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic"))) {
                            if (args.length > 2) sender.sendMessage(messageManager.getPlayerMessage("helmet_exist_other", null));
                            else player.sendMessage(messageManager.getPlayerMessage("helmet_exist", null));
                            return true;
                        }
                    }

                    // Set player's helmet slot to specified hat
                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;
                    List<String> lore = meta.getLore();
                    assert lore != null;
                    lore.remove(lore.size()-1);
                    lore.remove(lore.size()-1);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    player.getEquipment().setHelmet(item);
                    if (args.length > 2) sender.sendMessage(messageManager.getPlayerMessage("hat_success_other", item));
                    else player.sendMessage(messageManager.getPlayerMessage("hat_success", item));
                }
                else if(args[0].equalsIgnoreCase("unequip")) {
                    Player player;
                    if(args.length > 1) {
                        if(!sender.hasPermission("hatcosmetics.unequip.other")) {
                            sender.sendMessage(messageManager.getPlayerMessage("no_permission", null));
                            return true;
                        }
                        player = main.getServer().getPlayer(args[1]);
                        if(player == null || !player.isOnline()) {
                            sender.sendMessage(messageManager.getPlayerMessage("not_online", null));
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
                            player.getEquipment().getHelmet().getItemMeta() == null || player.getEquipment().getHelmet().getItemMeta().getLore() == null) {
                        if(args.length > 1) sender.sendMessage(messageManager.getPlayerMessage("no_hat_other", null));
                        else player.sendMessage(messageManager.getPlayerMessage("no_hat", null));
                        return true;
                    }
                    if(player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                        if(args.length > 1) sender.sendMessage(messageManager.getPlayerMessage("hat_unequip_success_other", player.getEquipment().getHelmet()));
                        else player.sendMessage(messageManager.getPlayerMessage("hat_unequip_success", player.getEquipment().getHelmet()));

                        player.getEquipment().setHelmet(new ItemStack(Material.AIR));
                        return true;
                    }
                    if(args.length > 1) sender.sendMessage(messageManager.getPlayerMessage("no_hat_other", null));
                    else player.sendMessage(messageManager.getPlayerMessage("no_hat", null));
                }
                else if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("hatcosmetics.reload")) {
                        main.saveDefaultConfig();
                        main.reloadConfig();
                        messageManager.createMessagesConfig();
                        Main.hats.clear();
                        main.getInventoryManager().initHats();
                        if(sender instanceof Player) sender.sendMessage(messageManager.getPlayerMessage("plugin_reload", null));
                        main.getLogger().info("Plugin has been reloaded!");
                        return true;
                    }
                    sender.sendMessage(messageManager.getPlayerMessage("no_permission", null));
                }
            }
        }

        return false;
    }
}
