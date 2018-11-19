package net.iccraft.gameboosters;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import net.iccraft.gameboosters.booster.BoosterType;
import net.iccraft.gameboosters.booster.GameBooster;

public class EventListeners implements Listener {

	private BoosterMain main;
	
	public EventListeners(BoosterMain main) {
		this.main = main;
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent ede) {
		if (main.getBooster() == null)								{ return; }
		if (main.getBooster().getBoosterType() != BoosterType.XP) 	{ return; }
		if (ede.getEntityType() == EntityType.PLAYER) 				{ return; }
		
		ede.setDroppedExp(ede.getDroppedExp()*2);
	}
	
	@EventHandler
	public void mcmmoXpGet(McMMOPlayerXpGainEvent e) {
		if (main.getBooster() == null) 									{ return; }
		if (main.getBooster().getBoosterType() != BoosterType.MCMMO) 	{ return; }
		
		e.setRawXpGained(e.getRawXpGained()*2);
	}
	
	@EventHandler
	public void invClick(InventoryClickEvent e) {
		if (e.getInventory() == null)	{ return; }
		if (!e.getInventory().getName().equals(ChatColor.BOLD + "Booster GUI")) 	{ return; }
		e.setCancelled(true);
		if (e.getCurrentItem() == null) { return; }
		Player pl = (Player) e.getWhoClicked();
		
		for (BoosterType type : BoosterType.values()) {
			if (e.getCurrentItem().getType() == type.getItemRep()) {
				int amt = main.getPlayerConfig(pl.getUniqueId()).getInt("booster_storage." + type.name());
				if (amt < 1) {
					pl.sendMessage(main.getMsgFromFile("inventory.noBoosters", pl, null, type.getCustomName()));
					pl.closeInventory();
					return;
				}
				
				GameBooster boosterAdd = new GameBooster(type, pl.getUniqueId());
				pl.sendMessage(main.getMsgFromFile("inventory.addToQueue", pl, boosterAdd));
				
				pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
				main.addBooster(boosterAdd);
				FileConfiguration cfg = main.getPlayerConfig(pl.getUniqueId());
				cfg.set("booster_storage." + type.name(), amt - 1);
				main.savePlayerConfig(cfg, pl.getUniqueId());
				pl.closeInventory();
			}
		}
	}
}
