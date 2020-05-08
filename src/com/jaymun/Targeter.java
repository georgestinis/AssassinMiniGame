package com.jaymun;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
 
public class Targeter {
	
	// Get the player you look
    public static Player getTargetPlayer(final Player player) {
    	// Get the location of player's eye
    	Location origin = player.getEyeLocation();
    	// Get a vector with the direction the player look
    	Vector direction = player.getEyeLocation().getDirection();
    	// Set the range of look
    	int range = 200;
    	// Every loop the direction coordinates multiply by i, and you set your location origin as the direction coordinates
    	for (double i = 1; i <= range; i+=0.5) {
    		// Multiply the direction with the i iterator
    		direction.multiply(i);
    		// Then add the direction to the location
    		origin.add(direction);
    		// Get the nearby entities that are in the specific point you look with 2 blocks height difference
    		for(Entity entity : player.getWorld().getNearbyEntities(origin, 0, 2, 0)) {
    			// If the entity is a player and it's not you return him/her
    			if (entity instanceof Player && !entity.getName().equals(player.getName())) {
    				return (Player)entity;
    			}
    		}
    		// Then subtract the direction you added
    		origin.subtract(direction);
    		// Normalize the vectors
    		direction.normalize();
    	}
    	return null;
    }
 
}