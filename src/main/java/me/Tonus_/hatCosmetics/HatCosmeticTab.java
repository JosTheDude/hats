package me.Tonus_.hatCosmetics;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class HatCosmeticTab implements TabCompleter {
    List<String> arguments = new ArrayList<>();

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(arguments.isEmpty()) {
            arguments.add("help"); arguments.add("unequip"); arguments.add("reload");
        }

        List<String> result = new ArrayList<>();
        if(args.length == 1) {
            for(String a : arguments) {
                if(a.toLowerCase().startsWith(args[0].toLowerCase())) result.add(a);
            }
            return result;
        }

        return null;
    }
}
