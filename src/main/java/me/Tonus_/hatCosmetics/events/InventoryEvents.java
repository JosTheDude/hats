package me.Tonus_.hatCosmetics.events;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.Tonus_.hatCosmetics.InventoryManager;
import me.Tonus_.hatCosmetics.Main;
import me.Tonus_.hatCosmetics.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InventoryEvents implements Listener {
    private final Main main;
    private final MessageManager messageManager;
    private final InventoryManager inventoryManager;

    public InventoryEvents(Main main) {
        this.main = main;
        this.messageManager = main.getMessageManager();
        this.inventoryManager = main.getInventoryManager();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Unequips hat if there is no helmet on the cursor
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

        Player player = (Player) event.getWhoClicked();

        // Unequips hat if clicked on in invalid slot
        if(!event.getView().getTitle().contains(messageManager.getMessage("gui_title"))) {
            if(event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) return;
            if(event.getCurrentItem().getItemMeta().getLore() == null) return;
            if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
                    player.sendMessage(messageManager.getPlayerMessage("hat_unequip_success", event.getCurrentItem()));
                }
                event.setCurrentItem(new ItemStack(Material.AIR));
                event.setCancelled(true);
            }
        }

        // GUI Management
        else {
            if(!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

            event.setCancelled(true);

            if(event.getSlot() >= 9 && event.getSlot() < event.getInventory().getSize()-9 && event.getCurrentItem().getItemMeta().getLore() != null &&
                    event.getCurrentItem().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                helmCheck: {
                    if (player.getEquipment() != null && player.getEquipment().getHelmet() != null) {
                        if (player.getEquipment().getHelmet().getItemMeta() != null && player.getEquipment().getHelmet().getItemMeta().getLore() != null &&
                                player.getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic"))
                            break helmCheck;
                        player.sendMessage(messageManager.getPlayerMessage("helmet_exist", null));
                        player.closeInventory();
                        return;
                    }
                }
                ItemStack item = event.getCurrentItem();
                NBTItem nbti = new NBTItem(item);
                ItemMeta meta = item.getItemMeta();
                int lastLine = meta.getLore().size() - 1;
                if(meta.getLore().get(lastLine).equals(messageManager.getMessage("hat_equip"))) {
                    if (player.hasPermission(nbti.getString("Permission"))) {
                        List<String> lore = meta.getLore();
                        assert lore != null;
                        lore.remove(lore.size()-1);
                        lore.remove(lore.size()-1);
                        meta.setLore(lore);
                        item.setItemMeta(meta);

                        player.getEquipment().setHelmet(item);
                        player.sendMessage(messageManager.getPlayerMessage("hat_success", item));
                        player.closeInventory();
                        return;
                    }
                    player.sendMessage(messageManager.getPlayerMessage("no_hat_permission", item));
                } else if(meta.getLore().get(lastLine).equals(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("hat_unequip")))) {
                    player.closeInventory();
                    player.sendMessage(messageManager.getPlayerMessage("hat_unequip_success", item));
                    player.getEquipment().setHelmet(new ItemStack(Material.AIR));
                }
            } else if(event.getSlot() == event.getInventory().getSize()-5 && event.getCurrentItem().getType().equals(Material.BARRIER) && event.getCurrentItem().getItemMeta().getDisplayName().contains("Close Menu")) {
                player.closeInventory();
            } else if(event.getSlot() == event.getInventory().getSize()-4 && event.getCurrentItem().getType().equals(Material.ARROW) && event.getCurrentItem().getItemMeta().getDisplayName().contains("Next Page")) {
                int currentPage = inventoryManager.getPlayerPage(player.getUniqueId());
                inventoryManager.setPlayerPage(player.getUniqueId(), currentPage + 1);
                player.openInventory(inventoryManager.openInv(player));
            } else if(event.getSlot() == event.getInventory().getSize()-6 && event.getCurrentItem().getType().equals(Material.ARROW) && event.getCurrentItem().getItemMeta().getDisplayName().contains("Previous Page")) {
                int currentPage = inventoryManager.getPlayerPage(player.getUniqueId());
                inventoryManager.setPlayerPage(player.getUniqueId(), currentPage - 1);
                player.openInventory(inventoryManager.openInv(player));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Clears the current page the player was on when they close the GUI
        if(event.getView().getTitle().contains(messageManager.getMessage("gui_title"))) {
            Player player = (Player) event.getPlayer();
            Bukkit.getScheduler().runTaskLater(main, () -> {
                if(!player.getOpenInventory().getTitle().contains(messageManager.getMessage("gui_title"))) {
                    inventoryManager.removePlayerPage(player.getUniqueId());
                    inventoryManager.removePlayerHats(player.getUniqueId());
                }
            }, 1L);
        }
    }
}
