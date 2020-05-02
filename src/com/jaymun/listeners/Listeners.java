package com.jaymun.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import com.jaymun.AssassinMiniGamePlugin;
import com.jaymun.Targeter;
import com.jaymun.assassin.Assassin;

public class Listeners implements Listener{
	private List<Assassin> players; 
	private Assassin[] all_players;
	private Assassin assassin;
	private World world;
	private boolean round_started = false, assassin_stop = false;
	private int remaining_players = 0;
	private Plugin plugin = AssassinMiniGamePlugin.getPlugin(AssassinMiniGamePlugin.class);
	// Set the players and get the world, then set the player's type and start the game
		public Listeners(List<Assassin> players) {
			this.players = new ArrayList<>();
			this.players = players;
			this.all_players = players.toArray(new Assassin[players.size()]);
			for (Assassin p : players) {
				if (p.isAssassin()) {
					assassin = p;
					break;
				}
			}
			remaining_players = players.size() - 1;
			world = players.get(0).getPlayer().getWorld();
			gameStart();
		}

		@EventHandler
		public void onPlayerLook(PlayerMoveEvent event) {
			if (isRound_started()) {
				if (assassin.getPlayer().getName().equals(event.getPlayer().getName()) && isAssassin_stop()) {
					event.setCancelled(true);
				}
				else if (assassin.getPlayer().getName().equals(event.getPlayer().getName())) {
					return;
				}
				else {
					final Player player = event.getPlayer();
					Player target_player = Targeter.getTargetPlayer(player);					
					if (target_player != null && assassin.getPlayer().getName().equals(target_player.getName())) {
						setAssassin_stop(true);
					}
					else {
						setAssassin_stop(false);
					}
				}
			} 
			else {
				return;
			}
		}
		
		@EventHandler
		public void assassinHit(EntityDamageByEntityEvent event) {
			if (isRound_started()) {
				if (event.getEntity() instanceof Player && event.getDamager() instanceof Player 
					&& event.getDamager().getName().equals(assassin.getPlayer().getName())) {
					Player whoHit = (Player)event.getDamager();
					Player hitted = (Player)event.getEntity();
					System.out.println(all_players.length);
					for (Iterator<Assassin> iterator= players.iterator(); iterator.hasNext();) {
						Assassin a = iterator.next();
						if (a.getPlayer().getName().equals(hitted.getName())) {
							iterator.remove();
							System.out.println(all_players.length);
							remaining_players--;
							if (remaining_players == 0) {
								for (Assassin player : all_players) {
									player.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + assassin.getPlayer().getName() + " has won the minigame");									
								}
								whoHit.sendMessage(ChatColor.RED + "Teleporting to spawn in 3 seconds");
								Bukkit.getScheduler().runTaskLater(plugin, ()-> { 
									whoHit.teleport(world.getSpawnLocation());
									HandlerList.unregisterAll(AssassinMiniGamePlugin.LISTENER);
								}, 60);
							}
							hitted.sendMessage(ChatColor.RED + "You got hit, teleporting to spawn in 3 seconds");
							Bukkit.getScheduler().runTaskLater(plugin, ()-> hitted.teleport(world.getSpawnLocation()), 60);																
						}
					}
				}
			}
			else {
				event.setCancelled(true);
			}
		}
		
		public void gameStart() {
			Assassin assassin = null;
			for (Assassin a : players) {
				if (a.isAssassin()) {
					assassin = a;
					break;
				}
			}
			for (Assassin a : players) {
				a.getPlayer().sendMessage(ChatColor.GOLD + assassin.getPlayer().getName() + " is the "+ ChatColor.WHITE + "Assassin");
				a.getPlayer().sendMessage(ChatColor.RED + "Game starts in 10 seconds");
			}
			Bukkit.getScheduler().runTaskLater(plugin, ()-> initializeGame(), 140);
		}

		private void initializeGame() {
			// Find some random coordinates to create the arena
			int x = getRandom(-10000, 10000);
			int z = getRandom(-10000, 10000);
			// This is to get the player more at the center of the arena
			for (Assassin player : players) {
				
				// Create a new location
				if (player.isAssassin()) {
					x += 80;					
				}
				else {
					z += 20;
				}
				// For every player we get the ground Coordinate
				int y = world.getHighestBlockAt(x, z).getY();
				Location l = new Location(world, x, y, z);
				// Load the chunks at this location and reset player's health and food
				world.getChunkAt(l).load();
				player.getPlayer().setHealth(player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
				player.getPlayer().setFoodLevel(20);
				// Check if the chunks are loaded and then wait a few seconds to be sure and then teleport the player
				if (world.getChunkAt(l).isLoaded()) {
					Bukkit.getScheduler().runTaskLater(plugin, ()->{					
						player.getPlayer().teleport(l);	
						// Clear their inventories and give them tools
						player.getPlayer().getInventory().clear();	
						setRound_started(true);
					}, 60);	 
				}
			}	
		}
		
		public int getRandom(int lowest, int highest) {
	        Random random = new Random();
	        return random.nextInt((highest - lowest) + 1) + lowest;  //99
		}

		public boolean isRound_started() {
			return round_started;
		}

		public void setRound_started(boolean round_started) {
			this.round_started = round_started;
		}

		public boolean isAssassin_stop() {
			return assassin_stop;
		}

		public void setAssassin_stop(boolean assassin_stop) {
			this.assassin_stop = assassin_stop;
		}
}
