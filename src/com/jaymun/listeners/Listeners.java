package com.jaymun.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.jaymun.AssassinMiniGamePlugin;
import com.jaymun.Targeter;
import com.jaymun.assassin.Assassin;
import com.jaymun.commands.StartMiniGameCommand;

// TODO deathEvent, clear inventory, add /quit minigame
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
		public void onPlayerLeave(PlayerQuitEvent event) {
			if (event.getPlayer().getName().equals(assassin.getPlayer().getName()) && isRound_started()) {				
				for (Assassin player : all_players) {
					player.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + assassin.getPlayer().getName() + " has decided to ragequit the minigame");									
				}
				for (Assassin player : players) {
					player.getPlayer().sendMessage(ChatColor.RED + "Teleporting to spawn in 3 seconds");
					Bukkit.getScheduler().runTaskLater(plugin, ()-> { 
						player.getPlayer().teleport(world.getSpawnLocation());						
					}, 60);
				}
				HandlerList.unregisterAll(AssassinMiniGamePlugin.LISTENER);
				StartMiniGameCommand.ASSASSIN_COUNT = 0;
				StartMiniGameCommand.P_COUNT = 0;
				StartMiniGameCommand.PLAYERS.clear();
			}
			else if (isRound_started()) {
				remaining_players--;
				if (remaining_players == 0) {
					for (Assassin player : all_players) {
						player.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + assassin.getPlayer().getName() + " has won the minigame");									
					}
					assassin.getPlayer().sendMessage(ChatColor.RED + "Teleporting to spawn in 3 seconds");
					Bukkit.getScheduler().runTaskLater(plugin, ()-> { 
						assassin.getPlayer().teleport(world.getSpawnLocation());
						event.getPlayer().teleport(world.getSpawnLocation());
						HandlerList.unregisterAll(AssassinMiniGamePlugin.LISTENER);
					}, 60);
					StartMiniGameCommand.ASSASSIN_COUNT = 0;
					StartMiniGameCommand.P_COUNT = 0;
					StartMiniGameCommand.PLAYERS.clear();
				}
				else {
					assassin.getPlayer().sendMessage(ChatColor.RED + "" + remaining_players + " people remaining!");
					event.getPlayer().teleport(world.getSpawnLocation());
				}
			}
		}
		
		@EventHandler(priority = EventPriority.HIGHEST)
	    public void onPlayerItemHeld(PlayerItemHeldEvent event){  
			// Check if the round is started otherwise do nothing
			if (event.getPlayer().getName().equals(assassin.getPlayer().getName()) && isRound_started()) {				
				new BukkitRunnable() {
					@Override
					public void run() {
						// Get the player and the item that he/she holds in main hand
				        ItemStack i = assassin.getPlayer().getInventory().getItemInMainHand();
				        // If it's a compass find the hunted player and get his/her coordinates and show them in hunter's chat
						if(i.getType() == Material.COMPASS){
							double closest = Double.MAX_VALUE;
							Player closestp = null;
							for (Assassin a : players) {
								if (!a.isAssassin()) {
									double dist = a.getPlayer().getLocation().distance(assassin.getPlayer().getLocation());
									if (closest == Double.MAX_VALUE || dist < closest) {
										closest = dist;
										closestp = a.getPlayer();
									}
								}
							}
							if (closestp != null) {
								assassin.getPlayer().setCompassTarget(closestp.getLocation());	
							}				        	
							return;
				        }
						// If it's not a compass cancel the task and the event
				        else {
				        	cancel();
				        	event.setCancelled(true);
				        }
					}			
				}.runTaskTimer(plugin, 0L, 20L);
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
					for (Iterator<Assassin> iterator= players.iterator(); iterator.hasNext();) {
						Assassin a = iterator.next();
						if (a.getPlayer().getName().equals(hitted.getName())) {
							iterator.remove();
							iterator= players.iterator();
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
								StartMiniGameCommand.ASSASSIN_COUNT = 0;
								StartMiniGameCommand.P_COUNT = 0;
								StartMiniGameCommand.PLAYERS.clear();
							}
							else {
								assassin.getPlayer().sendMessage(ChatColor.RED + "" + remaining_players + " people remaining!");
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
						assassin.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS, 1));
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
