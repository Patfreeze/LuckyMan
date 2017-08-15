package com.amedacier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LuckyMan extends JavaPlugin implements Listener{

	// SOME GLOBAL
	int iExpLevelDefault = 5;
	String sErrorColor = "§c"; // LightRed
	String sObjectColor = "§9"; // LightBlue
	String sCorrectColor = "§2"; // Green

    private File configf,languagef;
    private FileConfiguration config, language;

    public FileConfiguration getLanguageConfig() {
        return this.language;
    }
    
    private void createFiles() throws InvalidConfigurationException {

        configf = new File(getDataFolder(), "config.yml");
        languagef = new File(getDataFolder(), "language.yml");

        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        if (!languagef.exists()) {
        	languagef.getParentFile().mkdirs();
        	saveResource("language.yml", false);
        }

        config = new YamlConfiguration();
        language = new YamlConfiguration();
        
        try {
            config.load(configf);
            language.load(languagef);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    @Override
    public void onEnable(){
		
		try {
			createFiles();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
	 // Enable our class to check for new players using onPlayerJoin()
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
	}
	
	// This method checks for incoming players and sends them a message
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
	    Player player = event.getPlayer();
	    player.playSound(player.getLocation(),Sound.ENTITY_LIGHTNING_IMPACT, 1F, 1F);
	    player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 1);
	}
	
	public int getExpLevelCost() {
		
		int iExp = iExpLevelDefault;
		// MAKE SURE EXP CONFIG IS INT AND MORE THAN OR EQUAL TO 0
		if(config.isInt("ExpCost") && config.getInt("ExpCost") > -1) {
			iExp = config.getInt("ExpCost");
		}
		return iExp;
	}
	
	@Override
	public boolean onCommand(CommandSender sender,
		Command command,
		String label,
		String[] args) {
		
		// THE MAIN COMMAND
		if ((command.getName().equalsIgnoreCase("luckyman") && args.length == 0) || command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("help")) { // If the player typed /basic then do the following...
			// HELP COMMAND OR COMMAND WITHOUT ARGS
			
			// TAKE THE iEXP COST IN CONFIG IF NOT GOOD TAKE DEFAULT
			if(config.getBoolean("UseExpCost")) {
				sender.sendMessage(sCorrectColor+String.format(language.getString("Beware"), getExpLevelCost()));	
			}
			sender.sendMessage(sObjectColor+command.getUsage());
			return true;
		}
		else if (command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("forfun")) {
			
			int iStep = 0;
			
		    Random randomGenerator = new Random();
		    for (int idx = 0; idx <= 5; ++idx){
		    	iStep = randomGenerator.nextInt(3);
		    }
			
			// COMMAND FOR FUN HERE
		    Player player = (Player) sender;
			switch(iStep) {
			case 0:
				
				PotionEffect Potion = new PotionEffect(PotionEffectType.CONFUSION, 200, 2);
				player.playSound(player.getLocation(),Sound.ENTITY_ZOMBIE_AMBIENT, 1F, 1F);
				player.addPotionEffect(Potion);
				sender.sendMessage(sObjectColor+language.getString("Zombify"));
				player.performCommand("me "+language.getString("BeenTroll"));
				break;
				
				default:
					player.playSound(player.getLocation(),Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 1F, 1F);
					player.playSound(player.getLocation(),Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 2F, 2F);
					sender.sendMessage(sObjectColor+language.getString("ArgExplode"));
					player.performCommand("me "+language.getString("BeenTroll"));
					break;
			
			}

			
			
			return true;
		}
		else if (command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("getluck")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(sErrorColor+"This command can only be run by a player.");
			} else {
				Player player = (Player) sender;
				// check if player have something in hand
				if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
					sender.sendMessage(sObjectColor+language.getString("NothingHand"));
				}
				else if(config.getBoolean("UseExpCost") && player.getLevel() < getExpLevelCost()) {
					sender.sendMessage(sObjectColor+String.format(language.getString("NeedXP"), getExpLevelCost()));
				}
				else {
					ItemStack objStack = getChangeItemStack(player.getInventory().getItemInMainHand(), sender);
					sender.sendMessage(sCorrectColor+String.format(language.getString("ConvertItem"), sErrorColor+player.getInventory().getItemInMainHand().getType().toString(), sCorrectColor, objStack.getType().toString()));
					player.getInventory().setItemInMainHand(new ItemStack(objStack));
					player.playSound(player.getLocation(),Sound.BLOCK_NOTE_HARP, 1F, 1F);
					player.playSound(player.getLocation(),Sound.BLOCK_NOTE_HARP, 3F, 3F);
					player.setLevel(player.getLevel()-getExpLevelCost());
				}
			}
			return true;
		}
		return false;
	}
	
	public ItemStack getChangeItemStack(ItemStack ItemStackOld, CommandSender sender) {
		
		int iChangeForGood = 0;
		List<String> a_sItem = new ArrayList<String>();
		List<String> a_sEnchantment = new ArrayList<String>();
		
		int iRandomInt = 0;
		int iRandomItem = 0;
	    //note a single Random object is reused here
	    Random randomGenerator = new Random();
	    for (int idx = 0; idx <= 10; ++idx){
	    	iRandomInt = randomGenerator.nextInt(100);
	    }
		
	    iChangeForGood = iRandomInt;
	    /** TODO: FUTURE CODE MAKE SOME EXCHANGE BETTER THAN OTHERS **/
	    /**
		switch(ItemStackOld.getType().toString()) {
			default: // WILL RANDOMLY THE NUMBER
				iChangeForGood = iRandomInt;
				break;
		}
		**/
	    
		ItemStack objStack;
		if(iChangeForGood >= 0 && iChangeForGood < 55) {
			// POOR CHOICE HERE
			a_sItem.add("BONE");
			a_sItem.add("SAPLING");
			a_sItem.add("SAND");
			a_sItem.add("LEAVES");
			a_sItem.add("GLASS");
			a_sItem.add("SANDSTONE");
			a_sItem.add("YELLOW_FLOWER");
			a_sItem.add("RED_ROSE");
			a_sItem.add("MOSSY_COBBLESTONE");
			a_sItem.add("WORKBENCH");
			a_sItem.add("FURNACE");
			a_sItem.add("SIGN");
			a_sItem.add("STONE_BUTTON");
			a_sItem.add("WOOD_BUTTON");
			a_sItem.add("STICK");
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(randomGenerator.nextInt(4)+2);
		}
		else if(iChangeForGood > 56 && iChangeForGood < 85) {
			// AVERAGE CHOICE HERE
			a_sItem.add("COAL");
			a_sItem.add("COBBLESTONE");
			a_sItem.add("WOOD");
			a_sItem.add("LOG");
			a_sItem.add("GRAVEL");
			a_sItem.add("FEATHER");
			a_sItem.add("POTATO_ITEM");
			a_sItem.add("CARROT_ITEM");
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(randomGenerator.nextInt(4)+1);
		}
		else if(iChangeForGood > 86 && iChangeForGood < 95) {
			// BETTER CHOICE HERE
			a_sItem.add("DIAMOND");
			a_sItem.add("GOLD_ORE");
			a_sItem.add("IRON_ORE");
			a_sItem.add("COAL_ORE");
			a_sItem.add("ANVIL");
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(randomGenerator.nextInt(2)+1);
		}
		else if(iChangeForGood > 96) {
			
			a_sItem.add("DIAMOND_SWORD"); // OK
			a_sEnchantment.add("FIRE_ASPECT");
			
			a_sItem.add("DIAMOND_SPADE"); // OK
			a_sEnchantment.add("DURABILITY");
			
			a_sItem.add("DIAMOND_PICKAXE"); // OK
			a_sEnchantment.add("LOOT_BONUS_BLOCKS");
			
			a_sItem.add("DIAMOND_AXE"); // OK
			a_sEnchantment.add("DURABILITY");
			
			a_sItem.add("DIAMOND_HOE"); // OK
			a_sEnchantment.add("DURABILITY");
			
			a_sItem.add("DIAMOND_HELMET"); // OK
			a_sEnchantment.add("OXYGEN");
			
			a_sItem.add("DIAMOND_CHESTPLATE"); // OK
			a_sEnchantment.add("PROTECTION_EXPLOSIONS");
			
			a_sItem.add("DIAMOND_LEGGINGS"); // OK
			a_sEnchantment.add("PROTECTION_FIRE");
			
			a_sItem.add("DIAMOND_BOOTS"); // OK
			a_sEnchantment.add("FROST_WALKER");
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(1);
			objStack.setDurability((short) 299);
			
			if(a_sEnchantment.get(iRandomItem) != "") {
				objStack.addEnchantment(Enchantment.getByName(a_sEnchantment.get(iRandomItem)), 2);
			}
			
		}
		else {
			sender.sendMessage(sErrorColor+language.getString("Oupps"));
			return ItemStackOld;
		}
		
		
		return objStack;
	}
}
