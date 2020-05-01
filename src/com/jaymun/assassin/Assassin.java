package com.jaymun.assassin;

import org.bukkit.entity.Player;

public class Assassin {
	private Player player;
	private boolean assassin;
	
	public Assassin(Player player, boolean assassin) {
		this.setPlayer(player);
		this.setAssassin(assassin);
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public boolean isAssassin() {
		return assassin;
	}

	public void setAssassin(boolean assassin) {
		this.assassin = assassin;
	}
}
