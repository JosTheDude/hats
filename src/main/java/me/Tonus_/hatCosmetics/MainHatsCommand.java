package me.Tonus_.hatCosmetics;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainHatsCommand implements CommandExecutor {
    private final Main main;

    public MainHatsCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(label.equalsIgnoreCase("hatcosmetics") || label.equalsIgnoreCase("hats")) {
            if(args.length == 0) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage("You cannot do this in console!");
                    return true;
                }
                Player player = (Player) sender;
                // Open GUI
                player.openInventory(main.inv);
                return true;
            } else {
                String header = main.getMessagesConfig().getString("prefix")+main.getMessagesConfig().getString("suffix");
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
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_hat_given")));
                        return true;
                    } else if(!Main.hats.containsKey(args[1])) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("hat_not_exist")));
                        return true;
                    }
                    Player player;
                    if(args.length > 2) {
                        if(!sender.hasPermission("hatcosmetics.equip.other")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_permission")));
                            return true;
                        }
                        player = main.getServer().getPlayer(args[2]);
                        if(player == null || !player.isOnline()) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("not_online")));
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
                    ItemStack item = Main.hats.get(args[1]);
                    NBTItem nbti = new NBTItem(item);
                    if(!player.hasPermission(nbti.getString("Permission"))) {
                        player.sendMessage(nbti.toString());
                        if (args.length > 2) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_hat_permission_other")));
                        else player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_hat_permission")));
                        return true;
                    }

                    // Then check if the player has a helmet equipped and cancel if so
                    if (player.getEquipment().getHelmet() != null) {
                        if (player.getEquipment().getHelmet().getItemMeta() != null || player.getEquipment().getHelmet().getItemMeta().getLore() != null ||
                                !(player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic"))) {
                            if (args.length > 2) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("helmet_exist_other")));
                            else player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("helmet_exist")));
                            return true;
                        }
                    }

                    // Set player's helmet slot to specified hat
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Hat Cosmetic");
                    assert meta != null;
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    player.getEquipment().setHelmet(item);
                    if (args.length > 2) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("hat_success_other")));
                    else player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("hat_success")));
                }
                else if(args[0].equalsIgnoreCase("unequip")) {
                    Player player;
                    if(args.length > 1) {
                        if(!sender.hasPermission("hatcosmetics.unequip.other")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_permission")));
                            return true;
                        }
                        player = main.getServer().getPlayer(args[1]);
                        if(player == null || !player.isOnline()) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("not_online")));
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
                        if(args.length > 1) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("hat_unequip_success_other")));
                        else player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("hat_unequip_success")));
                        return true;
                    }
                    if(args.length > 1) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_hat_other")));
                    else player.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_hat")));
                }
                else if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("hatcosmetics.reload")) {
                        main.reloadConfig();
                        main.createMessagesConfig();
                        Main.hats.clear();
                        main.createInv();
                        if(sender instanceof Player) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("plugin_reload")));
                        main.getLogger().info("Plugin has been reloaded!");
                        return true;
                    }
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header + main.getMessagesConfig().getString("no_permission")));
                }
            }
        }

        return false;
    }
}
