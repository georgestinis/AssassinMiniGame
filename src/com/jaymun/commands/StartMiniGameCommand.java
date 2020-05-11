package com.jaymun.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	private int time = 0;
	public static int P_COUNT = 0, ASSASSIN_COUNT = 0;
	protected BukkitTask task;
	private Plugin plugin = AssassinMiniGamePlugin.getPlugin(AssassinMiniGamePlugin.class);
	private static final int TIMER = 30;
	
	// When you write /join [assassin || Assassin](not required) this method is triggered
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Check if there is some time left and add the player
		if (time >= 0) {
			P_COUNT++;
			if (sender instanceof Player) {
				new BukkitRunnable() {					
					@Override
					public void run() {
						if (P_COUNT < 2) {
							for (Assassin player : PLAYERS) {
								player.getPlayer().sendMessage(ChatColor.GOLD + "No other player has joined the minigame queue, game has been reset");
							}
							P_COUNT = 0;
							ASSASSIN_COUNT = 0;
							PLAYERS.clear();
						}
					}
				}.runTaskLater(plugin, 6000);
				if (P_COUNT >= 2) {
					for (Assassin assassin : PLAYERS) {
						if (assassin.getPlayer().getName().equals(((Player)sender).getName())) {
							((Player)sender).sendMessage(ChatColor.GOLD + "You already wait in queue");
							P_COUNT--;
							return true;
						}
					}
				}
				Assassin a;
				// If there are arguments and they are equal to [A||a]ssassin and if the assassin counter is 0, add a new assassin 
				if (args.length>0 && (args[0].equalsIgnoreCase("Assassin")) && ASSASSIN_COUNT == 0) {
					a = new Assassin((Player)sender, true);
					// Increase the assassin count
					ASSASSIN_COUNT++;
				}
				// Else add a normal player
				else {
					a = new Assassin((Player)sender, false);
				}
				// Add the player to the list
				PLAYERS.add(a);
			}
			// If there are at least 2 players in queue start a 30 second count down for other players to join
			if (P_COUNT == 2) {
				setTime(TIMER);
				// Task that runs every second
				task = new BukkitRunnable() {				
					@Override
					public void run() {
						// If time reaches 0 
						if (time == 0) {
							time--;
							// Check if no one wanted to be assassin
							if (ASSASSIN_COUNT == 0) {
								// If there was no assassin, select a random one
								Random rand = new Random();
							    PLAYERS.get(rand.nextInt(PLAYERS.size())).setAssassin(true);
							    // Then set a new listener with our methods
								AssassinMiniGamePlugin.LISTENER = new Listeners(PLAYERS);	
							}
							// Else if there is an assassin
							else {
								// Set a new listener
								AssassinMiniGamePlugin.LISTENER = new Listeners(PLAYERS);
							}						
							// Register to the plugin our listener
							plugin.getServer().getPluginManager().registerEvents(AssassinMiniGamePlugin.LISTENER, plugin);
							// Set a quit command
							((AssassinMiniGamePlugin) plugin).getCommand("quit").setExecutor((CommandExecutor)new QuitMiniGameCommand());
							// Cancel the task and return
							cancel();
							return;
						}
						// If the time is 15 or 30 info all the players
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
		// if time is less than 0 send a message to the player that the game is already started
		else {
			((Player)sender).sendMessage(ChatColor.RED + "Game has already started");
		}
		return true;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
