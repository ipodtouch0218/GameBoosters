package net.iccraft.gameboosters.booster;

import org.bukkit.Material;

public enum BoosterType {

	@SuppressWarnings("deprecation")
	XP (Material.EXP_BOTTLE, "XP Booster"),
	FLY (Material.ELYTRA, "Fly Booster"),
	SPEED (Material.FEATHER, "Speed Booster"),
	MCMMO (Material.BLAZE_POWDER, "McMMO XP Booster");
	
	private Material item;
	private String customName;
	
	BoosterType(Material mat, String custom) {
		item = mat;
		customName = custom;
	}
	
	public Material getItemRep() {
		return item;
	}
	
	public String getCustomName() {
		return customName;
	}
}