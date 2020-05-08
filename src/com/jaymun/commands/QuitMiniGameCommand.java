package com.jaymun.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitMiniGameCommand implements CommandExecutor{
	
	// When you write /quit a playerQuitEvent is created with your player
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			PlayerQuitEvent event = new PlayerQuitEvent((Player) sender, "");
			Bukkit.getServer().getPluginManager().callEvent(event);
		}
		return true;
	}

}
