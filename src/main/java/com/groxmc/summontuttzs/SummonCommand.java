package com.groxmc.summontuttzs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SummonCommand implements CommandExecutor {

    private final SummonTuttzs plugin;

    public SummonCommand(SummonTuttzs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("summontuttzs.admin")) {
            sender.sendMessage(ChatColor.RED + "Sem permissão.");
            return true;
        }

        Location loc;
        if (sender instanceof Player player) {
            loc = player.getLocation();
        } else {
            sender.sendMessage(ChatColor.RED + "Execute in-game.");
            return true;
        }

        TuttzsSummoner.summon(plugin, loc);
        sender.sendMessage(ChatColor.BLUE + "Invocando tuttzs...");
        return true;
    }
}
