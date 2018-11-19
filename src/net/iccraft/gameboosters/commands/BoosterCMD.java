package net.iccraft.gameboosters.commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.iccraft.gameboosters.BoosterMain;
import net.iccraft.gameboosters.booster.BoosterType;
import net.iccraft.gameboosters.booster.GameBooster;

public class BoosterCMD implements CommandExecutor {

	private BoosterMain main;
	
	public BoosterCMD(BoosterMain main) {
		this.main = main;
	}
	
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(main.getMsgFromFile("command.generic.consoleDeny"));
				return true;
			}
			((Player) sender).openInventory(createGUI(((Player) sender).getUniqueId()));
			return true;
		}
		
		if (args[0].equalsIgnoreCase("admin")) {
			if (!sender.hasPermission("booster.admin")) {
				sender.sendMessage(main.getMsgFromFile("command.booster.admin.noPermission"));
				return true;
			}
			if (args.length < 2) {
				sender.sendMessage(main.getMsgFromFile("command.booster.admin.help"));
				return true;
			}
			
			switch (args[1].toLowerCase()) {
			case "queueremove":
				if (args.length < 3) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.queueremove.help"));
					return true;
				}
				try {
					Integer.parseInt(args[2]);
				} catch (NumberFormatException nfe) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.queueremove.help"));
					return true;
				}
				int toRemove = Integer.parseInt(args[2])-1;
				if (toRemove < 0) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.queueremove.invalidIndex", null, null, (toRemove+1) + ""));
					return true;
				}
				if (main.getQueue().size() <= toRemove) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.queueremove.invalidIndex", null, null, (toRemove+1) + ""));
					return true;
				}
				main.getQueue().remove(toRemove);
				sender.sendMessage(main.getMsgFromFile("command.booster.admin.queueremove.successful", null, null, (toRemove+1) + ""));
				return true;
			case "give":
				if (args.length < 4) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.give.help"));
					return true;
				}
				try {
					BoosterType.valueOf(args[3].toUpperCase());
				} catch (IllegalArgumentException e) {
					sender.sendMessage(main.getMsgFromFile("command.generic.invalid-booster", null, null, args[3]));
					return true;
				}
				OfflinePlayer giveTarget = Bukkit.getOfflinePlayer(args[2]);
				BoosterType giveType = BoosterType.valueOf(args[3].toUpperCase());
				
				FileConfiguration cfg = main.getPlayerConfig(giveTarget.getUniqueId());
				cfg.set("booster_storage." + giveType.name(), main.getPlayerConfig(giveTarget.getUniqueId()).getInt("booster_storage." + giveType.name()) + 1);
				main.savePlayerConfig(cfg, giveTarget.getUniqueId());
				if (giveTarget.isOnline()) {
					((Player) giveTarget).sendMessage(main.getMsgFromFile("command.booster.admin.give.received", giveTarget, new GameBooster(giveType, UUID.randomUUID())));
				}
				sender.sendMessage(main.getMsgFromFile("command.booster.admin.give.given", giveTarget, new GameBooster(giveType, UUID.randomUUID())));
				return true;
				
			case "start":
				if (args.length < 3) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.start.help"));
					return true;
				}
				try {
					BoosterType.valueOf(args[2].toUpperCase());
				} catch (IllegalArgumentException e) { 
					sender.sendMessage(main.getMsgFromFile("command.generic.invalid-booster", null, null, args[2]));
					return true;
				}
				OfflinePlayer startPlayer = null;
				if (args.length >= 4) {
					startPlayer = Bukkit.getOfflinePlayer(args[3]);
				} else if (!(sender instanceof Player)) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.start.help"));
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.start.consoleHelp"));
					return true;
				}
				BoosterType startType = BoosterType.valueOf(args[2].toUpperCase());
				if (startPlayer == null) {
					main.forceBooster(new GameBooster(startType, ((Player) sender).getUniqueId()));
				} else {
					main.forceBooster(new GameBooster(startType, startPlayer.getUniqueId()));
				}
				sender.sendMessage(main.getMsgFromFile("command.booster.admin.start.successful", null, new GameBooster(startType, UUID.randomUUID())));
				return true;
				
			case "settime": 
				if (args.length < 3) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.settime.help"));
					return true;
				}
				try {
					Long.parseLong(args[2]);
				} catch (NumberFormatException nfe) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.settime.help"));
					return true;
				}
				main.getBooster().setTicksRemaining(Long.parseLong(args[2]));
				sender.sendMessage(main.getMsgFromFile("command.booster.admin.settime.successful", null, null, args[2]));
				return true;
				
			case "stop":
				if (main.getBooster() == null) {
					sender.sendMessage(main.getMsgFromFile("command.booster.admin.stop.noBooster"));
					return true;
				}
				main.stopBooster();
				sender.sendMessage(main.getMsgFromFile("command.booster.admin.stop.successful"));
				return true;
			}
			sender.sendMessage(main.getMsgFromFile("command.booster.admin.help"));
			return true;
		}
		if (args[0].equalsIgnoreCase("activate")) {
			if (args.length < 2) {
				sender.sendMessage(main.getMsgFromFile("command.booster.activate.help"));
				return true;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(main.getMsgFromFile("command.booster.activate.help"));
				sender.sendMessage(main.getMsgFromFile("command.booster.activate.consoleHelp"));
				return true;
			}
			try {
				BoosterType.valueOf(args[1].toUpperCase());
			} catch (IllegalArgumentException e) {
				sender.sendMessage(main.getMsgFromFile("command.generic.invalid-booster", null, null, args[1]));
				return true;
			}
			FileConfiguration cfg = main.getPlayerConfig((((Player) sender).getUniqueId()));
			BoosterType type = BoosterType.valueOf(args[1].toUpperCase());
			if (cfg.getInt("booster_storage." + type.name()) == 0) {
				sender.sendMessage(main.getMsgFromFile("inventory.noBoosters", null, new GameBooster(type, UUID.randomUUID())));
				return true;
			}
			
			sender.sendMessage(main.getMsgFromFile("inventory.addToQueue", null, new GameBooster(type, UUID.randomUUID())));
			main.addBooster(new GameBooster(type, ((Player) sender).getUniqueId()));
			cfg.set("booster_storage." + type.name(), cfg.getInt("booster_storage." + type.name()) - 1);
			main.savePlayerConfig(cfg, ((Player) sender).getUniqueId());
			return true;
		}
		if (args[0].equalsIgnoreCase("queue")) {
			sender.sendMessage(main.getMsgFromFile("command.booster.queue.header"));
			if (main.getQueue().size() <= 0) {
				sender.sendMessage(main.getMsgFromFile("command.booster.queue.noBoosters"));
				return true;
			}
			for (int amt = 0; amt < main.getQueue().size() ; amt++) {
				GameBooster booster = main.getQueue().get(amt);
				sender.sendMessage(main.getMsgFromFile("command.booster.queue.body", Bukkit.getOfflinePlayer(booster.getBoosterOwner()), booster, amt+1+""));
			}
		}
		
		sender.sendMessage(main.getMsgFromFile("command.booster.help"));
		
		return true;
	}

	
	
	private Inventory createGUI(UUID uuid) {
		FileConfiguration playerConfig = main.getPlayerConfig(uuid);
		
		Inventory tempInv = Bukkit.createInventory(null, 27, ChatColor.BOLD + "Booster GUI");
		int count = 10;
		ChatColor col = ChatColor.RESET;
		for (BoosterType type : BoosterType.values()) {
			count++;
			if (count == 13) count++;
			switch (count) {
			case 11: col = ChatColor.AQUA;
			case 12: col = ChatColor.RED;
			case 14: col = ChatColor.YELLOW;
			case 15: col = ChatColor.GREEN;
			}
			
			tempInv.setItem(count, buildItem(type.getItemRep(), playerConfig.getInt("booster_storage." + type.name()), col + type.getCustomName(), "", "&7You have &e" + playerConfig.getInt("booster_storage." + type.name()) + "&7 " + type.getCustomName() + "(s)."));
		}
		
		return tempInv;
		
	}
	
	private ItemStack buildItem(Material material, int amount, String name, String... lore) {
		ItemStack tempItemStack = new ItemStack(material);
		if (amount > 64) {
			tempItemStack.setAmount(64);
		} else if (amount < 1) {
			tempItemStack.setAmount(1);
			tempItemStack.setType(Material.BARRIER);
		} else {
			tempItemStack.setAmount(amount);
		}
		ItemMeta meta = tempItemStack.getItemMeta();
		if (name != null) {
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		}
		if (lore != null) {
			ArrayList<String> al = new ArrayList<String>();
			for (String str : lore) {
				al.add(ChatColor.translateAlternateColorCodes('&', str));
			}
			meta.setLore(al);
		}
		tempItemStack.setItemMeta(meta);
		return tempItemStack;
	}
}
