package com.amedacier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LuckyMan extends JavaPlugin implements Listener{

	// SOME GLOBAL
	int iExpLevelDefault = 2;
	String sTypeChoosen = "small";
	String sErrorColor = "§c"; // LightRed
	String sObjectColor = "§9"; // LightBlue
	String sCorrectColor = "§2"; // Green

    private File configf,languagef, playerdataf, objectsf;
    private FileConfiguration config, language, playerdata, objects;

    public FileConfiguration getLanguageConfig() {
        return this.language;
    }
    
    public FileConfiguration getPlayerDataConfig() {
    	return this.playerdata;
    }
    
    public FileConfiguration getObjectsConfig() {
    	return this.objects;
    }
    
    private void createFiles() throws InvalidConfigurationException {

    	playerdataf = new File(getDataFolder(), "playerdata.yml");
        configf = new File(getDataFolder(), "config.yml");
        languagef = new File(getDataFolder(), "language.yml");
        objectsf = new File(getDataFolder(), "objects.yml");

        /**
         * 
         * TODO: Check version in file if exist
         * 
         */
        
        if (!playerdataf.exists()) {
        	playerdataf.getParentFile().mkdirs();
            saveResource("playerdata.yml", false);
        }
        if (!configf.exists()) {
        	configf.getParentFile().mkdirs();
        	saveResource("config.yml", false);
        }
        if (!languagef.exists()) {
        	languagef.getParentFile().mkdirs();
        	saveResource("language.yml", false);
        }
        if (!objectsf.exists()) {
        	objectsf.getParentFile().mkdirs();
        	saveResource("objects.yml", false);
        }

        playerdata = new YamlConfiguration();
        config = new YamlConfiguration();
        language = new YamlConfiguration();
        objects = new YamlConfiguration();
        
        try {
        	playerdata.load(playerdataf);
            config.load(configf);
            language.load(languagef);
            objects.load(objectsf);
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
	    player.playSound(player.getLocation(),Sound.ENTITY_FIREWORK_TWINKLE, 1F, 1F);
	    player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 20);
	}
	
	public int getExpLevelCost(String sType) {
		
		int iExp = iExpLevelDefault;
		// MAKE SURE EXP CONFIG IS INT AND MORE THAN OR EQUAL TO 0
		if(sType.equalsIgnoreCase("small")) {
			if(config.isInt("ExpCostLow") && config.getInt("ExpCostLow") > -1) {
				iExp = config.getInt("ExpCostLow");
			}
		}
		else if(sType.equalsIgnoreCase("medium")) {
			if(config.isInt("ExpCostMedium") && config.getInt("ExpCostMedium") > -1) {
				iExp = config.getInt("ExpCostMedium");
			}
		}
		else if(sType.equalsIgnoreCase("high")) {
			if(config.isInt("ExpCostHigh") && config.getInt("ExpCostHigh") > -1) {
				iExp = config.getInt("ExpCostHigh");
			}
		}
		return iExp;
	}
	
	public boolean updateRemainTime(Player player) {
		
		//SET CALENDAR
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
	    
	    // CHECK IF PLAYER IS IN FILE
	    long iLeft = 0;
	    if(!playerdata.isSet("P_"+player.getUniqueId())) {
	    	// NOT SET SO CREATE IT
	    	try {
	    		playerdata.createSection("P_"+player.getUniqueId());
	    		playerdata.set("P_"+player.getUniqueId()+".name", player.getName());
	    		playerdata.set("P_"+player.getUniqueId()+".timeRemain", calendar.getTimeInMillis());
	    		playerdata.save(playerdataf);
	    		
	    		return true;
	    	} catch (IOException exception) {
	    		exception.printStackTrace();
	    	}
	    }
	    else {
	    	// DATA EXIST VERIFY THEN UPDATE IF NEEDED
	    	iLeft = (calendar.getTimeInMillis() - playerdata.getLong("P_"+player.getUniqueId()+".timeRemain"))/1000;
	    	//System.out.println("Remain : "+iLeft);
	    	
	    	// CHECK IF WE CAN DO IT AGAIN BY CHECK IN config.yml
	    	if(iLeft >= config.getLong("iSecondDelay")) {
	    		// Ex: IF iLeft=70 AND iSecondeDelay=60 HE CAN REUSE IT
	    		// UPDATE DATA ALSO NAME IF CHANGE IT
		    	try {
		    		playerdata.set("P_"+player.getUniqueId()+".name", player.getName());
		    		playerdata.set("P_"+player.getUniqueId()+".timeRemain", calendar.getTimeInMillis());
		    		playerdata.save(playerdataf);
		    	} catch (IOException exception) {
		    		exception.printStackTrace();
		    	}
	    		return true;
	    	}
	    	else {
	    		// HERE MEAN STILL REMAIN TIME
	    		long iRemain = config.getLong("iSecondDelay") - iLeft;
	    		player.sendMessage(sCorrectColor+String.format(language.getString("DelayReuse"), sErrorColor+iRemain, (iRemain>1?"s":"")+sCorrectColor));
	    		return false;
	    	}
	    }
	    return false;
	}
	
	@Override
	public boolean onCommand(CommandSender sender,
		Command command,
		String label,
		String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(sErrorColor+"Cant be call from console");
			return true;
		}
		
		Player player = (Player) sender;
		
		// THE MAIN COMMAND
		if ((command.getName().equalsIgnoreCase("luckyman") && args.length == 0) || command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("help")) { // If the player typed /basic then do the following...
			// HELP COMMAND OR COMMAND WITHOUT ARGS
			
			// TAKE THE iEXP COST IN CONFIG IF NOT GOOD TAKE DEFAULT
			if(config.getBoolean("UseExpCost")) {
				sender.sendMessage(sCorrectColor+String.format(language.getString("BewareLevel"),getExpLevelCost("small")));	
			}
			sender.sendMessage(sObjectColor+command.getUsage());
			return true;
		}
		else if (command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("forfun")) {
			
			int iStep = 0;
			
		    Random randomGenerator = new Random();
		    for (int idx = 0; idx <= 5; ++idx){
		    	iStep = randomGenerator.nextInt(4);
		    }
			
			// COMMAND FOR FUN HERE
		    Player player1 = (Player) sender;
		    PotionEffect Potion;
			switch(iStep) {
			case 0:
				Potion = new PotionEffect(PotionEffectType.CONFUSION, 200, 2);
				player1.playSound(player1.getLocation(),Sound.ENTITY_ZOMBIE_AMBIENT, 1F, 1F);
				player1.addPotionEffect(Potion);
				sender.sendMessage(sObjectColor+language.getString("Zombify"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
			
			case 1:	
				Potion = new PotionEffect(PotionEffectType.BLINDNESS, 300, 2);
				player1.playSound(player1.getLocation(),Sound.ENTITY_BAT_DEATH, 1F, 1F);
				player1.addPotionEffect(Potion);
				sender.sendMessage(sObjectColor+language.getString("lightPlz"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
				
			case 2:	
				Potion = new PotionEffect(PotionEffectType.SPEED, 400, 2);
				player1.playSound(player1.getLocation(),Sound.ITEM_FIRECHARGE_USE, 1F, 1F);
				player1.addPotionEffect(Potion);
				sender.sendMessage(sObjectColor+language.getString("speedUp"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
			
			default:
				player1.playSound(player1.getLocation(),Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 1F, 1F);
				player1.playSound(player1.getLocation(),Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 2F, 2F);
				sender.sendMessage(sObjectColor+language.getString("ArgExplode"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
			
			}

			
			
			return true;
		}
		else if (command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("getluck")) {
			
		
			//DEFAULT TYPE IS SMALL
			String sType = "small";
			
			//System.out.println(args.length + " args");
			if(args.length == 1) {
				sender.sendMessage(sObjectColor+String.format(language.getString("helpType"), "small, medium, high"));
				return true;
			}
			
			if(args.length > 1) {
				if(args[1].equalsIgnoreCase("medium")) {
					sType = "medium";
					this.sTypeChoosen = sType;
				}
				else if(args[1].equalsIgnoreCase("small")) {
					sType = "small";
					this.sTypeChoosen = sType;
				}
				else if(args[1].equalsIgnoreCase("high")) {
					sType = "high";
					this.sTypeChoosen = sType;
				}
				else {
					sender.sendMessage(sObjectColor+String.format(language.getString("getLuckType"),args[1]));
					return true;
				}
				//System.out.println("arg are " + args[1]);
			}
			
			if (!(sender instanceof Player)) {
				sender.sendMessage(sErrorColor+"This command can only be run by a player.");
			} else {
				
				Player player1 = (Player) sender;
				// check if player have something in hand
				if(player1.getInventory().getItemInMainHand().getType() == Material.AIR) {
					sender.sendMessage(sObjectColor+language.getString("NothingHand"));
				}
				else if(config.getBoolean("UseExpCost") && player1.getLevel() < getExpLevelCost(sType)) {
					sender.sendMessage(sObjectColor+String.format(language.getString("NeedXP"), getExpLevelCost(sType)));
				}
				else {
					
					// UPDATE TIME AND CHECK IF OK
					if(!updateRemainTime(player)) {
						return true;
					}
					
					ItemStack objStack = getChangeItemStack(player1.getInventory().getItemInMainHand(), sender);
					sender.sendMessage(sCorrectColor+String.format(language.getString("ConvertItem"), sErrorColor+player1.getInventory().getItemInMainHand().getType().toString(), sCorrectColor, objStack.getType().toString()));
					player1.getInventory().setItemInMainHand(new ItemStack(objStack));
					player1.playSound(player1.getLocation(),Sound.BLOCK_NOTE_HARP, 1F, 1F);
					player1.playSound(player1.getLocation(),Sound.BLOCK_NOTE_HARP, 3F, 3F);
					player1.setLevel(player1.getLevel()-getExpLevelCost(sType));
					// return to default type
					this.sTypeChoosen = "small";
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * This is call when sign is change
	 * @param e
	 */
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
	    if (e.getPlayer().hasPermission("sign.color")) {
	    	
	    	if(ChatColor.stripColor(e.getLine(0)).equalsIgnoreCase("[luckyman]")){
	    		if(ChatColor.stripColor(e.getLine(1)).equalsIgnoreCase("small")) {
	    			e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&2[LuckyMan]"));
	    			e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&9small"));
	    			
	    			// Check if XP needed or not
	    			if(config.getBoolean("UseExpCost") == true) {
	    				e.setLine(3, "§c"+String.format(language.getString("neededXp"), getExpLevelCost("small")));
	    			}
	    		}
	    		else if(ChatColor.stripColor(e.getLine(1)).equalsIgnoreCase("medium")) {
	    			e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&2[LuckyMan]"));
	    			e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&9medium"));
	    			
	    			// Check if XP needed or not
	    			if(config.getBoolean("UseExpCost") == true) {
	    				e.setLine(3, "§c"+String.format(language.getString("neededXp"), getExpLevelCost("medium")));
	    			}
	    		}
	    		else if(ChatColor.stripColor(e.getLine(1)).equalsIgnoreCase("high")) {
	    			e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&2[LuckyMan]"));
	    			e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&9high"));
	    			
	    			// Check if XP needed or not
	    			if(config.getBoolean("UseExpCost") == true) {
	    				e.setLine(3, "§c"+String.format(language.getString("neededXp"), getExpLevelCost("high")));
	    			}
	    		}
	    	}
	    }
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {

	    if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
	        return;
	    }

	    Player p = e.getPlayer();
	    if (p.hasPermission("sign.use")) {
	 
	        Block b = e.getClickedBlock();
	        if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
	   
	            Sign sign = (Sign) b.getState();
	            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[LuckyMan]")) {

	                if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("small")) {
	                	Bukkit.dispatchCommand(p, "luckyman getLuck small");
	                }
	                else if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("medium")) {
	                	Bukkit.dispatchCommand(p, "luckyman getLuck medium");
	                }
	                else if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("high")) {
	                	Bukkit.dispatchCommand(p, "luckyman getLuck high");
	                }
	                else {
	                	p.sendMessage(sErrorColor+language.getString("signNotWork")+" /LuckyMan getLuck [small,medium,high]");
	                }

	            }
	   
	        }
	 
	    }

	}
	
	public ItemStack getChangeItemStack(ItemStack ItemStackOld, CommandSender sender) {
		
		int iChangeForGood = 0;
		List<String> a_sItem = new ArrayList<String>();
		
		int iRandomInt = 0;
		int iRandomItem = 0;
	    //note a single Random object is reused here
	    Random randomGenerator = new Random();
	    for (int idx = 0; idx <= 10; ++idx){
	    	iRandomInt = randomGenerator.nextInt(100);
	    }
		
	    iChangeForGood = iRandomInt;

	    switch(this.sTypeChoosen) {
	    	case "medium":
	    		iChangeForGood = iRandomInt + randomGenerator.nextInt(15);
	    		break;
	    		
	    	case "high":
	    		iChangeForGood = iRandomInt + randomGenerator.nextInt(35);
	    		break;
	    		
	    	default:
	    		// NOTHING TO DO SMALL IS HERE TOO
	    		break;
	    }
	    
	    //System.out.println(this.sTypeChoosen + " " + iRandomInt);
	    
		ItemStack objStack;
		
		// SET PERCENTAGE NO NEED TO CHECK Extra.percent... This is the last the rest
		int iPoorMaxPercent = objects.getInt("poor.percent");
		int iAverageMaxPercent = iPoorMaxPercent + objects.getInt("average.percent");
		int iBetterMaxPercent = iAverageMaxPercent + objects.getInt("better.percent");
		
		//System.out.println("0 "+iPoorMaxPercent+" "+iAverageMaxPercent+" "+iBetterMaxPercent);
		
		// POOR CHOICE HERE
		if(iChangeForGood >= 0 && iChangeForGood < iPoorMaxPercent) {
		
			for(String sItem : objects.getStringList("poor.objects")) {
				a_sItem.add(sItem);
			}
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(randomGenerator.nextInt(objects.getInt("poor.maxItem")+1)+1);
		}
		else if(iChangeForGood >= iPoorMaxPercent && iChangeForGood < iAverageMaxPercent) {
			
			for(String sItem : objects.getStringList("average.objects")) {
				a_sItem.add(sItem);
			}
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(randomGenerator.nextInt(objects.getInt("average.maxItem")+1)+1);
		}
		else if(iChangeForGood >= iAverageMaxPercent && iChangeForGood < iBetterMaxPercent) {
			
			for(String sItem : objects.getStringList("better.objects")) {
				a_sItem.add(sItem);
			}
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(randomGenerator.nextInt(objects.getInt("better.maxItem")+1)+1);
		}
		else if(iChangeForGood >= iBetterMaxPercent) {
			
			for (String key : objects.getConfigurationSection("extra.objects").getKeys(true)) {
				a_sItem.add(key);
			}
			
			// GET THE RANDOM OBJECT TO PASS
			iRandomItem = randomGenerator.nextInt(a_sItem.size());
			
			objStack = new ItemStack(Material.getMaterial((String) a_sItem.get(iRandomItem)));
			objStack.setAmount(1);
			objStack.setDurability((short) 299);
			
			for(String sEnchant : objects.getStringList("extra.objects."+(String) a_sItem.get(iRandomItem))) {
				objStack.addEnchantment(Enchantment.getByName(sEnchant), 2);
			}
			
		}
		else {
			sender.sendMessage(sErrorColor+language.getString("Oupps"));
			return ItemStackOld;
		}
		
		
		return objStack;
	}
}
