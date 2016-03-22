package de.themoep.entitydetection.commands;

import de.themoep.entitydetection.EntityDetection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 * <p/>
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */
public class PluginCommandExecutor implements CommandExecutor {
    private final EntityDetection plugin;

    private Map<String, Map<String, SubCommand>> subCommands = new HashMap<String, Map<String, SubCommand>>();

    public PluginCommandExecutor(EntityDetection plugin) {
        this.plugin = plugin;
        plugin.getCommand(plugin.getName().toLowerCase()).setExecutor(this);
    }

    public void register(SubCommand sub) {
        if(!subCommands.containsKey(sub.getCommand())) {
            subCommands.put(sub.getCommand(), new HashMap<String, SubCommand>());
        }
        subCommands.get(sub.getCommand()).put(sub.getPath(), sub);
        try {
            plugin.getServer().getPluginManager().addPermission(sub.getPermission());
        } catch(IllegalArgumentException ignore) {
            // Permission was already defined correctly in the plugin.yml
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) {
            List<String> helpText = new ArrayList<String>();
            helpText.add(ChatColor.YELLOW + plugin.getName() + ChatColor.GREEN + " v" + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthors());
            if(subCommands.containsKey(cmd.getName())) {
                for(SubCommand sub : subCommands.get(cmd.getName()).values()) {
                    if(!sender.hasPermission(sub.getPermission())) {
                        continue;
                    }
                    helpText.add(sub.getUsage(label));
                    helpText.add(ChatColor.GRAY + sub.getHelp());
                }
            } else {
                helpText.add("No sub commands found.");
            }
            sender.sendMessage(helpText.toArray(new String[helpText.size()]));
            return true;
        }

        SubCommand sub = getSubCommand(cmd, args);
        if(sub == null) {
            return false;
        }

        String argStr = args[0];
        for(int i = 1; i < args.length; i++) {
            argStr += args[i];
        }
        String[] subArgs = argStr.replace(sub.getPath(), "").trim().split(" ");
        if(!sub.execute(sender, subArgs)) {
            sender.sendMessage("Usage: " + sub.getUsage(label));
        }
        return true;
    }

    private SubCommand getSubCommand(Command cmd, String[] args) {
        if(subCommands.containsKey(cmd.getName())) {
            String path = "";
            int i = 0;
            while(!subCommands.get(cmd.getName()).containsKey(path) && i < args.length) {
                path += " " + args[i].toLowerCase();
                i++;
            }
            return subCommands.get(cmd.getName()).get(path);
        }
        return null;
    }
}
