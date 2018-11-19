package net.iccraft.gameboosters.booster;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.iccraft.gameboosters.BoosterMain;

public class GameBooster {

	private BoosterType boosterType;
	private UUID boosterOwner;
	private long ticksRemaining;
	
	private static BoosterMain main;
	
	public static void setMain(BoosterMain maine) 	{ main = maine; } 
	public static void clrMain() 					{ main = null; }
	
	public GameBooster(BoosterType type, UUID owner) {
		boosterType = type;
		boosterOwner = owner;
		ticksRemaining = main.getConfig().getLong("booster_length");
	}
	
	public BoosterType getBoosterType() {
		return boosterType;
	}
	
	public UUID getBoosterOwner() {
		return boosterOwner;
	}
	
	public long getTicksRemaining() {
		return ticksRemaining;
	}
	
	public void setTicksRemaining(long ticksToSet) {
		ticksRemaining = ticksToSet;
	}
	
	//Serialization\\
	public String serialize() {
		StringBuilder serializedBuilder = new StringBuilder();
		serializedBuilder.append(boosterType.name()).append(",").append(boosterOwner).append(",").append(ticksRemaining);
		return serializedBuilder.toString();
	}
	
	public static GameBooster deserialize(String serializedString) {
		String[] fields = serializedString.split(",");
		GameBooster gameBooster = new GameBooster(BoosterType.valueOf(fields[0]), UUID.fromString(fields[1]));
		try {
			gameBooster.ticksRemaining = Long.parseLong(fields[2]);
		} catch (Exception ex) {
			Logger.getLogger("GameBoosters").log(Level.SEVERE, "String incorrect when deserializing GameBooster. Maybe it is corrupted?");
			Logger.getLogger("GameBoosters").log(Level.SEVERE, "Outputting Stacktrace for debugging purposes. Send this to @ipodtouch0218!");
			ex.printStackTrace();
		}
		return gameBooster;
	}
}
