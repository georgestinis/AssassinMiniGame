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
	
	@Override
	public void onEnable() {
		instance = this;
		instance.getCommand("join").setExecutor((CommandExecutor)new StartMiniGameCommand());
	}
	
	@Override
	public void onDisable() {
		instance = null;
	}
	
	public static Listeners getListener() {
		return LISTENER;
	}

	public static void setListener(Listeners listener) {
		AssassinMiniGamePlugin.LISTENER = listener;
	}
}
