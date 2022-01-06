package me.Tonus_.hatCosmetics;

import me.Tonus_.hatCosmetics.manager.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HatCosmeticTab implements TabCompleter {
    private final ConfigManager configManager;
    private final List<String> arguments = new ArrayList<>();
    private final Set<String> hats = Main.hats.keySet();

    public HatCosmeticTab(Main main) {
        this.configManager = main.getConfigManager();
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(arguments.isEmpty()) {
            arguments.add("help"); arguments.add("equip"); arguments.add("unequip"); arguments.add("reload");
        }

        List<String> result = new ArrayList<>();
        if(args.length == 1) {
            for(String a : arguments) {
                if(a.toLowerCase().startsWith(args[0].toLowerCase())) result.add(a);
            }
            return result;
        }
        if(args.length > 1) {
            if(args[0].equalsIgnoreCase("equip")) {
                if(args.length == 2) {
                    for(String a : hats) {
                        if(a.toLowerCase().startsWith(args[1])) {
                            if(configManager.hideHats && !sender.hasPermission("hatcosmetics.hat." + a)) continue;
                            result.add(a);
                        }
                    }
                    return result;
                }
            }
        }

        return null;
    }
}
