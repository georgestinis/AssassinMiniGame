package com.jaymun;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
 
public class Targeter {
 
    public static Player getTargetPlayer(final Player player) {
    	Location origin = player.getEyeLocation();
    	Vector direction = player.getEyeLocation().getDirection();
    	int range = 10;
    	for (double i = 1; i <= range; i+=0.5) {
    		direction.multiply(i);
    		origin.add(direction);
    		for(Entity entity : player.getWorld().getNearbyEntities(origin, 0, 2, 0)) {
    			if (entity instanceof Player && !entity.getName().equals(player.getName())) {
    				return (Player)entity;
    			}
    		}
    		origin.subtract(direction);
    		direction.normalize();
    	}
    	return null;
        //return getTarget(player, player.getWorld().getPlayers());
    }
 
    public static Entity getTargetEntity(final Entity entity) {
        return getTarget(entity, entity.getWorld().getEntities());
    }
 
    public static <T extends Entity> T getTarget(final Entity entity,
            final Iterable<T> entities) {
        if (entity == null)
            return null;
        Player p = null;
        if (entity instanceof Player) {
        	p = (Player)entity;
	        T target = null;
	        final double threshold = 1;
	        for (final T other : entities) {
	            final Vector n = other.getLocation().toVector()
	                    .subtract(p.getEyeLocation().toVector());
	            if (p.getEyeLocation().getDirection().normalize().crossProduct(n)
	                    .length() < threshold
	                    && n.normalize().dot(
	                            p.getEyeLocation().getDirection().normalize()) >= 0) {
	                if (target == null
	                        || target.getLocation().distance(
	                                p.getEyeLocation()) > other.getLocation()
	                                .distance(p.getEyeLocation()))
	                    target = other;
	            }
	        }
	        return target;
        }
        return null;
    }
 
}