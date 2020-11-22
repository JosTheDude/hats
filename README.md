**What does this plugin do?**

HatCosmetics provides a simple way of implementing custom resource pack models using custom model data to your server. All the provided "hats" are put into a customizable GUI where the player can equip a hat, so long as they do not have actual armor on and they have permissions to that hat. On top of providing an easy way to obtain the custom models, there are features to ensure it stays on the player's head and not somewhere else.

**What features does this plugin offer?**
- A GUI to access all hats: This GUI acts as the plugin's main menu where players can browse and selects hats to equip. They are able to view all the hats, but can only equip ones they have permissions to. The border surrounding the hats can be set to any item, including air!
- Prevent removal of hat from death/inventory: Even if keep inventory is disabled, players will not be able to drop their hat or put it somewhere else in their inventory (creative players can freely move hats in their inventory). The only way to unequip hats outside of creative mode is to use the command for it.
- Plugin's config can be reloaded without restarting server: If you need to quickly add a hat, change the GUI size, or items, you do not have to worry about restarting your server to make those changes!


**What are the commands for the plugin?**

- Alias for /hats: /hatcosmetics
- /hats (Opens GUI for hat selection)
  - hatcosmetics.hat.<hat> (Access to specified hat)
- /hats help (Opens command list for plugin)
- /hats unequip (Removes hat if player has one)
- /hats reload (Reloads config)
  - hatcosmetics.reload