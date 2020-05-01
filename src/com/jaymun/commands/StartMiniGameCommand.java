package com.jaymun.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.jaymun.AssassinMiniGamePlugin;
import com.jaymun.assassin.Assassin;
import com.jaymun.listeners.Listeners;

public class StartMiniGameCommand implements CommandExecutor{
	private List<Assassin> players = new ArrayList<>();
	private int p_count = 0, time, assassin_count = 0;
	protected BukkitTask task;
	private Plugin plugin = AssassinMiniGamePlugin.getPlugin(AssassinMiniGamePlugin.class);
	private static final int TIMER = 30;
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (time >= 0) {
			p_count++;
			if (sender instanceof Player) {
				Assassin a;
				if (args.length>0 && (args[0].equals("Assassin") || args[0].equals("assassin")) && assassin_count == 0) {
					a = new Assassin((Player)sender, true);
					assassin_count++;
				}
				else {
					a = new Assassin((Player)sender, false);
				}
				players.add(a);
			}
			if (p_count == 2) {
				//setTime(TIMER);
				setTime(2);
				task = new BukkitRunnable() {				
					@Override
					public void run() {
						if (time == 0) {
							AssassinMiniGamePlugin.LISTENER = new Listeners(players);
							plugin.getServer().getPluginManager().registerEvents(AssassinMiniGamePlugin.LISTENER, plugin);
							cancel();
							return;
						}
						else if (time == 15 || time == 30) {
							for (Assassin p : players) {
								p.getPlayer().sendMessage(ChatColor.RED + "Waiting " + time + " seconds for more players!");
							}
						}
						time--;
					}
				}.runTaskTimer(plugin, 0L, 20L);
			}
		}
		else {
			((Player)sender).sendMessage(ChatColor.RED + "Game has already started");
		}
		return true;
	}

	public List<Assassin> getPlayers() {
		return players;
	}

	public void setPlayers(List<Assassin> players) {
		this.players = players;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
