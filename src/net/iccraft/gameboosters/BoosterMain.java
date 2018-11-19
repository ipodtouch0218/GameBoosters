package net.iccraft.gameboosters;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.confuser.barapi.BarAPI;
import net.iccraft.gameboosters.booster.BoosterType;
import net.iccraft.gameboosters.booster.GameBooster;
import net.iccraft.gameboosters.commands.BoosterCMD;
import net.iccraft.gameboosters.commands.ThankCMD;
import net.milkbowl.vault.economy.Economy;

public class BoosterMain extends JavaPlugin {

	private Economy econ;
	private GameBooster activeBooster;
	private ThankCMD thankCmd = new ThankCMD(this);
	private ArrayList<GameBooster> boosterQueue = new ArrayList<GameBooster>();
	
	@Override
	public void onEnable() {
		
		saveDefaultConfig();
		if (!new File(getDataFolder() + "messages.yml").exists())
			saveResource("messages.yml", false);
		
		GameBooster.setMain(this);
		setupEconomy();
		if (econ == null) {
			Logger.getLogger("GameBooster").log(Level.SEVERE, "Could not get an economy through Vault. Is an economy handling plugin installed?");
		}
		getCommand("thank").setExecutor(thankCmd);
		getCommand("booster").setExecutor(new BoosterCMD(this));
		getServer().getPluginManager().registerEvents(new EventListeners(this), this);
		
		if (getConfig().isSet("activeBooster"))
			activeBooster = GameBooster.deserialize(getConfig().getString("activeBooster"));
		
		if (getConfig().isSet("boosterQueue"))
			getConfig().getStringList("boosterQueue").forEach(str -> boosterQueue.add(GameBooster.deserialize(str)));
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				for (Player pl : Bukkit.getOnlinePlayers()) {
					if (activeBooster == null) {
						if (!pl.hasPermission("essentials.fly") && !(pl.getGameMode() == GameMode.CREATIVE || pl.getGameMode() == GameMode.SPECTATOR)) {
							pl.setAllowFlight(false);
						} 
						continue;
					}
					if (activeBooster.getBoosterType() == BoosterType.FLY) break;
					if (!pl.hasPermission("essentials.fly") && !(pl.getGameMode() == GameMode.CREATIVE || pl.getGameMode() == GameMode.SPECTATOR)) {
						pl.setAllowFlight(false);
					}
				}
				if (activeBooster != null) {
					activeBooster.setTicksRemaining(activeBooster.getTicksRemaining()-1);
					if (activeBooster.getTicksRemaining() <= 0) {
						Bukkit.broadcastMessage(getMsgFromFile("booster.ending", Bukkit.getOfflinePlayer(activeBooster.getBoosterOwner()), activeBooster));
						activeBooster = null;
						return;
					}

					DateFormat timeRemaining = new SimpleDateFormat("HH:mm:ss");
					timeRemaining.setTimeZone(TimeZone.getTimeZone("GMT+0"));
					String timeStr = timeRemaining.format(new Date(activeBooster.getTicksRemaining() * 1000));
					
					BarAPI.setMessage(getMsgFromFile("booster.message", Bukkit.getOfflinePlayer(activeBooster.getBoosterOwner()), activeBooster, timeStr));
					
					for (Player pl : Bukkit.getOnlinePlayers()) {
						switch(activeBooster.getBoosterType()) {
						case FLY:
							pl.setAllowFlight(true);
							return;
						case SPEED:
							pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false), true);
						default:
						}
					}
					return;
				} 
				
				checkForNewBoosters();
			}
		}, 0, 20);
	}
	
	@Override
	public void onDisable() {
		saveConfiguration();
		GameBooster.clrMain();
	}
	
	public ArrayList<GameBooster> getQueue() {
		return boosterQueue;
	}
	
	private void saveConfiguration() {
		if (activeBooster != null)
			getConfig().set("activeBooster", activeBooster.serialize());
		else 
			getConfig().set("activeBooster", null);
		
		if (!boosterQueue.isEmpty()) {
			List<String> serializedQueue = new ArrayList<String>();
			boosterQueue.forEach(b -> serializedQueue.add(b.serialize()));
			getConfig().set("boosterQueue", serializedQueue);
		} else {
			getConfig().set("boosterQueue", null);
		}
		
		saveConfig();
	}
	
	private void checkForNewBoosters() {
		saveConfig();
		thankCmd.clearThanked();
		if (!boosterQueue.isEmpty()) {
			activeBooster = boosterQueue.get(0);
			boosterQueue.remove(0);
			Bukkit.broadcastMessage(getMsgFromFile("booster.activate", Bukkit.getOfflinePlayer(activeBooster.getBoosterOwner()), activeBooster));
		}
		saveConfiguration();
	}
	
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
        	Logger.getLogger("GameBooster").log(Level.SEVERE, "Couldn't find Vault, disabling...!");
            getServer().getPluginManager().disablePlugin(this);
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

	public void addBooster(GameBooster booster) {
		boosterQueue.add(booster);
	}
	
	public void stopBooster() {
		activeBooster = null;
	}
	
	public void forceBooster(GameBooster booster) {
		activeBooster = null;
		boosterQueue.add(0, booster);
	}
	
	public GameBooster getBooster() {
		return activeBooster;
	}
	
	public Economy getEcon() {
		return econ;
	}
	
	public FileConfiguration getPlayerConfig(UUID pl) {
		File pFile = new File(getDataFolder() + "/player-storage", pl.toString() + ".yml");
		return YamlConfiguration.loadConfiguration(pFile);
	}
	
	public void savePlayerConfig(FileConfiguration cfg, UUID pl) {
		File pFile = new File(getDataFolder() + "/player-storage", pl.toString() + ".yml");
		try {
			cfg.save(pFile);
		} catch (IOException e) {
			Logger.getLogger("GameBooster").log(Level.SEVERE, "Couldn't save " + Bukkit.getOfflinePlayer(pl).getName() + "'s Storage...");
		}
	}
	
	
	public String getMsgFromFile(String str) {
		FileConfiguration msgs = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
		if (msgs.getString("prefix").equals("")) {
			return ChatColor.translateAlternateColorCodes('&', msgs.getString(str));
		} else {
			return ChatColor.translateAlternateColorCodes('&', msgs.getString("prefix")) + " " + ChatColor.translateAlternateColorCodes('&', msgs.getString(str));
		}
	}
	
	public String getMsgFromFile(String str, OfflinePlayer pl, GameBooster booster, String... params) {
		FileConfiguration msgs = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
		String baseStr; 
		if (msgs.getString("prefix").equals("") || str.equals("booster.message")) {
			baseStr = ChatColor.translateAlternateColorCodes('&', msgs.getString(str));
		} else {
			baseStr = ChatColor.translateAlternateColorCodes('&', msgs.getString("prefix")) + " " + ChatColor.translateAlternateColorCodes('&', msgs.getString(str));
		}
		
		if (pl != null)
			baseStr = baseStr.replace("%player%", pl.getName());
		if (booster != null)
			baseStr = baseStr.replace("%booster%", booster.getBoosterType().getCustomName());
		
		switch (str) {
		case "booster.message":
			return baseStr.replace("%time%", params[0]);
		case "inventory.noboosters":
			return baseStr.replace("%booster%", params[0]);
		case "command.booster.admin.queueremove.successful":
			return baseStr.replace("%number%", params[0]);
		case "command.booster.admin.queueremove.invalidIndex":
			return baseStr.replace("%number%", params[0]);
		case "command.booster.queue.body":
			return baseStr.replace("%number%", params[0]);
		case "command.generic.invalid-booster":
			return baseStr.replace("%booster%", params[0]);
		case "command.booster.admin.settime.successful":
			return baseStr.replace("%time%", params[0]);
		}
		return baseStr;
	}
}
