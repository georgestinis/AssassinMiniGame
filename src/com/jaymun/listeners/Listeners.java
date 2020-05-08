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
import org.bukkit.event.entity.PlayerDeathEvent;
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

public class Listeners implements Listener{
	private List<Assassin> players; 
	private Assassin[] all_players;
	private Assassin assassin;
	private World world;
	private boolean round_started = false, assassin_stop = false;
	private int remaining_players = 0;
	private Plugin plugin = AssassinMiniGamePlugin.getPlugin(AssassinMiniGamePlugin.class);
	// Set an array with all the players and one with the remaining,then set the assassin, get the world and call start game method.
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
	
	// This event triggers when a player move or look
	@EventHandler
	public void onPlayerLook(PlayerMoveEvent event) {
		// Check if round is started
		if (isRound_started()) {
			// Check if the player is the assassin and if he/she must stop
			if (assassin.getPlayer().getName().equals(event.getPlayer().getName()) && isAssassin_stop()) {
				// If the isAssassin_stop is true means that the assassin can't move, and we cancel the event 
				event.setCancelled(true);
			}
			// Check if the player is the assassin and can move then just let the event do it's job
			else if (assassin.getPlayer().getName().equals(event.getPlayer().getName())) {
				return;
			}
			// If the player is not the assassin
			else {
				// Set the player
				final Player player = event.getPlayer();
				// Get the player you look, returns null if you look at anything else
				Player target_player = Targeter.getTargetPlayer(player);			
				// Check if the target player is not null and is the assassin
				if (target_player != null && assassin.getPlayer().getName().equals(target_player.getName())) {
					// Then set the assassin to stop
					setAssassin_stop(true);
				}
				else {
					// Else let him move freely
					setAssassin_stop(false);
				}
			}
		} 
		// If the round is not started just return
		else {
			return;
		}
	}
	
	// This event triggers when a player leave the server
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		// Check if the player was the assassin
		if (event.getPlayer().getName().equals(assassin.getPlayer().getName()) && isRound_started()) {				
			// Send message to all the players message
			for (Assassin player : all_players) {
				player.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + assassin.getPlayer().getName() + " has decided to ragequit the minigame");									
			}
			// Teleport all the remaining players to spawn and clear their inventories after 3 seconds
			for (Assassin player : players) {
				player.getPlayer().sendMessage(ChatColor.RED + "Teleporting to spawn in 3 seconds");
				player.getPlayer().getInventory().clear();
				Bukkit.getScheduler().runTaskLater(plugin, ()-> { 
					player.getPlayer().teleport(world.getSpawnLocation());						
				}, 60);
			}
			// Unregister the current listener 
			HandlerList.unregisterAll(AssassinMiniGamePlugin.LISTENER);
			// Reset command class variables
			StartMiniGameCommand.ASSASSIN_COUNT = 0;
			StartMiniGameCommand.P_COUNT = 0;
			StartMiniGameCommand.PLAYERS.clear();
		}
		// Check if the round is started and the left player wasn't the assassin
		else if (isRound_started()) {
			// Iterate through the players and remove the event player from the remaining players
			for (Iterator<Assassin> iterator= players.iterator(); iterator.hasNext();) {
				Assassin a = iterator.next();
				if (a.getPlayer().getName().equals(event.getPlayer().getName())) {
					iterator.remove();
					iterator= players.iterator();
					remaining_players--;
					// Check if the game has ended
					checkIfGameEnded();
					// After 3 seconds clear player's inventory and teleport him to spawn 
					// (doesn't work if he leaves the server, but I use it for /quit command which triggers this event)
					Bukkit.getScheduler().runTaskLater(plugin, ()-> {
						event.getPlayer().getInventory().clear();
						event.getPlayer().teleport(world.getSpawnLocation());
					}, 60);																
				}
			}
		}
	}
	
	// This event triggers when a player dies
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		// Check if the round is started and if the dead entity is a player
		if (isRound_started() && event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			// If the player was the assassin send message to all players
			if (p.getName().equals(assassin.getPlayer().getName())) {
				for (Assassin player : all_players) {
					player.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + assassin.getPlayer().getName() + " has died, minigame has ended");									
				}
				// Teleport the remaining players to spawn in 3 seconds except the assassin because he/she will re-spawn at spawn point
				for (Assassin player : players) {
					if (!player.isAssassin()) {
						player.getPlayer().sendMessage(ChatColor.RED + "Teleporting to spawn in 3 seconds");
						player.getPlayer().getInventory().clear();
						Bukkit.getScheduler().runTaskLater(plugin, ()-> { 
							player.getPlayer().teleport(world.getSpawnLocation());						
						}, 60);
					}
				}
				// Unregister the current listener 
				HandlerList.unregisterAll(AssassinMiniGamePlugin.LISTENER);
				// Reset command class variables
				StartMiniGameCommand.ASSASSIN_COUNT = 0;
				StartMiniGameCommand.P_COUNT = 0;
				StartMiniGameCommand.PLAYERS.clear();
			}
			// If the player wasn't the assassin remove him from the list of the remaining players and teleport him to spawn
			else {
				for (Iterator<Assassin> iterator= players.iterator(); iterator.hasNext();) {
					Assassin a = iterator.next();
					if (a.getPlayer().getName().equals(p.getName())) {
						iterator.remove();
						iterator= players.iterator();
						remaining_players--;
						checkIfGameEnded();
						p.sendMessage(ChatColor.RED + "You died");														
					}
				}
			}
		}		
	}
	
	// This event triggers when you hold an item
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event){  
		// Check if the round is started and the player is the assassin otherwise do nothing
		if (event.getPlayer().getName().equals(assassin.getPlayer().getName()) && isRound_started()) {				
			// Every second run this task
			new BukkitRunnable() {
				@Override
				public void run() {
					// Get the player and the item that he/she holds in main hand
			        ItemStack i = assassin.getPlayer().getInventory().getItemInMainHand();
			        // If it's a compass find the closest player and get his/her coordinates
					if(i.getType() == Material.COMPASS){
						double closest = Double.MAX_VALUE;
						Player closestp = null;
						// For every remaining player
						for (Assassin a : players) {
							// If the player is not the assassin
							if (!a.isAssassin()) {
								// Find the closest player
								double dist = a.getPlayer().getLocation().distance(assassin.getPlayer().getLocation());
								if (closest == Double.MAX_VALUE || dist < closest) {
									closest = dist;
									closestp = a.getPlayer();
								}
							}
						}
						// If you find a player set the compass target point at this player
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
	
	// This event triggers when an entity damage another entity
	@EventHandler
	public void assassinHit(EntityDamageByEntityEvent event) {
		// Check if the round is started otherwise cancel the event
		if (isRound_started()) {
			// Check if the entities are players and if the damager is the assassin
			if (event.getEntity() instanceof Player && event.getDamager() instanceof Player 
				&& event.getDamager().getName().equals(assassin.getPlayer().getName())) {
				Player damaged = (Player)event.getEntity();
				for (Iterator<Assassin> iterator= players.iterator(); iterator.hasNext();) {
					Assassin a = iterator.next();
					// Remove the damaged player from the list
					if (a.getPlayer().getName().equals(damaged.getName())) {
						iterator.remove();
						iterator= players.iterator();
						remaining_players--;
						// Check if the game is ended
						checkIfGameEnded();
						// Teleport the damaged player to spawn in 3 seconds and clear the inventory
						damaged.sendMessage(ChatColor.RED + "You got hit, teleporting to spawn in 3 seconds");
						Bukkit.getScheduler().runTaskLater(plugin, ()-> {
							damaged.teleport(world.getSpawnLocation());
							damaged.getInventory().clear();
						}, 60);																
					}
				}
			}
		}
		else {
			event.setCancelled(true);
		}
	}
	
	// Send message to all the players, and run initializeGame in 7 seconds
	public void gameStart() {
		for (Assassin a : players) {
			a.getPlayer().sendMessage(ChatColor.GOLD + assassin.getPlayer().getName() + " is the "+ ChatColor.WHITE + "Assassin");
			a.getPlayer().sendMessage(ChatColor.RED + "Game starts in 10 seconds");
		}
		Bukkit.getScheduler().runTaskLater(plugin, ()-> initializeGame(), 140);
	}

	// Check if there are players left and end the mini game else show the remaining players to assassin
	public void checkIfGameEnded() {
		if (remaining_players == 0) {
			for (Assassin player : all_players) {
				player.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + assassin.getPlayer().getName() + " has won the minigame");									
			}
			assassin.getPlayer().sendMessage(ChatColor.RED + "Teleporting to spawn in 3 seconds");
			assassin.getPlayer().getInventory().clear();
			Bukkit.getScheduler().runTaskLater(plugin, ()-> { 
				assassin.getPlayer().teleport(world.getSpawnLocation());
				HandlerList.unregisterAll(AssassinMiniGamePlugin.LISTENER);
			}, 60);
			StartMiniGameCommand.ASSASSIN_COUNT = 0;
			StartMiniGameCommand.P_COUNT = 0;
			StartMiniGameCommand.PLAYERS.clear();
		}
		else {
			assassin.getPlayer().sendMessage(ChatColor.RED + "" + remaining_players + " people remaining!");
		}
	}
	
	// Initialize the mini game
	private void initializeGame() {
		// Find some random coordinates to teleport the players
		int x = getRandom(-10000, 10000);
		int z = getRandom(-10000, 10000);
		// For every player find some coordinates to teleport them
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
					// Clear their inventories and give assassin a compass as a tracker
					player.getPlayer().getInventory().clear();	
					assassin.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS, 1));
					// Start the round
					setRound_started(true);
				}, 60);	 
			}
		}	
	}
	
	// A function to get random numbers
	public int getRandom(int lowest, int highest) {
        Random random = new Random();
        return random.nextInt((highest - lowest) + 1) + lowest;
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
