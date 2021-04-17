package me.Tonus_.hatCosmetics;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    private final HashMap<Player, ItemStack> droppedCosmetic = new HashMap<>();
    public static HashMap<String, ItemStack> hats = new HashMap<>();

    public InventoryManager inventoryManager;
    public MessageManager messageManager;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        messageManager = new MessageManager(this);
        messageManager.createMessagesConfig();
        inventoryManager = new InventoryManager(this);
        inventoryManager.initHats();
        Objects.requireNonNull(getCommand("hatcosmetics")).setTabCompleter(new HatCosmeticTab());
        Objects.requireNonNull(getCommand("hatcosmetics")).setExecutor(new MainHatsCommand(this));
    }

    @Override
    public void onDisable() {

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

        Player player = (Player) event.getWhoClicked();

        if(!event.getView().getTitle().equals(messageManager.getMessage("gui_title"))) {
            if(event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) return;
            if(event.getCurrentItem().getItemMeta().getLore() == null) return;
            if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
                    player.sendMessage(messageManager.getPlayerMessage("hat_unequip_success", event.getCurrentItem()));
                }
                event.setCurrentItem(new ItemStack(Material.AIR));
                event.setCancelled(true);
            }
        } else {
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
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.GRAY + "Hat Cosmetic");
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
            }
        }
    }
}