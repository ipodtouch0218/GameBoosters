package net.iccraft.gameboosters.commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.iccraft.gameboosters.BoosterMain;
import net.milkbowl.vault.economy.Economy;

public class ThankCMD implements CommandExecutor {

	private BoosterMain main;
	private ArrayList<UUID> thanked = new ArrayList<UUID>();
	
	public ThankCMD(BoosterMain main) {
		this.main = main;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(main.getMsgFromFile("command.generic.consoleDeny"));
			return true;
		}
		
		Player player = (Player) sender;
		if (main.getBooster() == null) {
			sender.sendMessage(main.getMsgFromFile("command.thank.noBooster"));
			return true;
		}
		if (thanked.contains(player.getUniqueId())) {
			sender.sendMessage(main.getMsgFromFile("command.thank.alreadyTipped", Bukkit.getOfflinePlayer(main.getBooster().getBoosterOwner()), main.getBooster()));
			return true;
		}
		if (player.getUniqueId().equals(main.getBooster().getBoosterOwner())) {
			sender.sendMessage(main.getMsgFromFile("command.thank.selftip"));
			return true;
		}
		
		Economy econ = main.getEcon();
		
		econ.depositPlayer(player, 15);
		econ.depositPlayer(Bukkit.getOfflinePlayer(main.getBooster().getBoosterOwner()), 15);
		sender.sendMessage(main.getMsgFromFile("command.thank.successful", Bukkit.getOfflinePlayer(main.getBooster().getBoosterOwner()), main.getBooster()));
		thanked.add(player.getUniqueId());
		
		return true;
	}

	public void clearThanked() {
		thanked.clear();
	}
}
