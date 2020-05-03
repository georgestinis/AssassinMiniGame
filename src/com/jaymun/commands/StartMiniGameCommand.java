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
	public static List<Assassin> PLAYERS = new ArrayList<>();
	private int time;
	public static int P_COUNT = 0, ASSASSIN_COUNT = 0;
	protected BukkitTask task;
	private Plugin plugin = AssassinMiniGamePlugin.getPlugin(AssassinMiniGamePlugin.class);
	private static final int TIMER = 30;
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (time >= 0) {
			P_COUNT++;
			if (sender instanceof Player) {
				Assassin a;
				if (args.length>0 && (args[0].equals("Assassin") || args[0].equals("assassin")) && ASSASSIN_COUNT == 0) {
					a = new Assassin((Player)sender, true);
					ASSASSIN_COUNT++;
				}
				else {
					a = new Assassin((Player)sender, false);
				}
				PLAYERS.add(a);
			}
			if (P_COUNT == 2) {
				//setTime(TIMER);
				setTime(2);
				task = new BukkitRunnable() {				
					@Override
					public void run() {
						if (time == 0) {
							AssassinMiniGamePlugin.LISTENER = new Listeners(PLAYERS);
							plugin.getServer().getPluginManager().registerEvents(AssassinMiniGamePlugin.LISTENER, plugin);
							cancel();
							return;
						}
						else if (time == 15 || time == 30) {
							for (Assassin p : PLAYERS) {
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
		return PLAYERS;
	}

	public void setPlayers(List<Assassin> players) {
		this.PLAYERS = players;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
