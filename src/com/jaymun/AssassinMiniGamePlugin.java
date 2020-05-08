package com.jaymun;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaymun.commands.StartMiniGameCommand;
import com.jaymun.listeners.Listeners;

public class AssassinMiniGamePlugin extends JavaPlugin{
	private static AssassinMiniGamePlugin instance;
	public static Listeners LISTENER;
	
	public AssassinMiniGamePlugin getInstance() {
		return AssassinMiniGamePlugin.instance;
	}
	
	// When the plugin is enabled set a /join command
	@Override
	public void onEnable() {
		instance = this;
		instance.getCommand("join").setExecutor((CommandExecutor)new StartMiniGameCommand());
	}
	
	// On disable set the plugin to null
	@Override
	public void onDisable() {
		instance = null;
	}
	
	// Create a static listener
	public static Listeners getListener() {
		return LISTENER;
	}

	public static void setListener(Listeners listener) {
		AssassinMiniGamePlugin.LISTENER = listener;
	}
	//
}
