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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// Version on spigot 1.0.8

public class LuckyMan extends JavaPlugin implements Listener{

	// SOME GLOBAL
	int iExpLevelDefault = 2;
	String sTypeChoosen = "small";
	String sErrorColor = "§c"; // LightRed
	String sObjectColor = "§9"; // LightBlue
	String sCorrectColor = "§2"; // Green
	String sResetColor = "§r";
	String sPluginName = "§2[LuckyMan] §r";
	int iVersion = 109; // 108 on spigot
	
	File getDataFolderLink  = getDataFolder();

    private File configf,languagef, playerdataf, objectsf;
    private FileConfiguration config, language, playerdata, objects;

    public FileConfiguration getLanguageConfig() {
        return this.language;
    }
    
    private void sendPluginMessage(Player player, String sMessage) {
    	player.sendMessage(sPluginName+sMessage);
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
		
    	// On enable plugin we create configs files
		try {
			createFiles();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		// This is for the auto complete in the chat of the player
		getCommand("luckyman").setTabCompleter(new LuckyManTabCompleter());
		
	 // Enable our class to check for new players using onPlayerJoin()
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		// You can do something here when your plugin was disabled by the owner
	}
	
	// This method checks for incoming players and sends them a message
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
	    Player player = event.getPlayer();
	    player.playSound(player.getLocation(),Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1F, 1F);
	    player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 20);
	}
	
	/**
	 * This will get the EXP from the player depend of small/medium/high
	 * @param sType
	 * @return
	 */
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
	
	/**
	 * Update the remain time after an execution
	 * @param player
	 * @param sType
	 * @return
	 */
	public boolean updateRemainTime(Player player, String sType) {
		
		// VALIDATE TYPE OF THE REMAINER
		switch(sType) {
			case "timeRemain":
			case "timeRemainForfun":
				// Nothing to do here we are OK
				break;
				
			default:
				sendPluginMessage(player, sErrorColor+"Sorry error! Look at console for more detail!");
				System.out.println(sType+" for the updateRemainTime is not implemented");
				return false;
		}
		
		//SET CALENDAR
	    Calendar calendar = Calendar.getInstance();
	    
	    // We set the time zone 
	    calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
	    
	    // CHECK IF PLAYER IS IN FILE
	    long iLeft = 0;
	    if(!playerdata.isSet("P_"+player.getUniqueId())) {
	    	// NOT SET SO CREATE IT
	    	try {
	    		playerdata.createSection("P_"+player.getUniqueId());
	    		playerdata.set("P_"+player.getUniqueId()+".name", player.getName());
	    		
	    		// Default value
	    		playerdata.set("P_"+player.getUniqueId()+".timeRemain", "1009558136984");
	    		playerdata.set("P_"+player.getUniqueId()+".timeRemainForfun", "1009558136984");
	    		
	    		// We update the type currently in use here
	    		playerdata.set("P_"+player.getUniqueId()+"."+sType, calendar.getTimeInMillis());
	    		
	    		// Save data to file
	    		playerdata.save(playerdataf);
	    		
	    		return true;
	    	} catch (IOException exception) {
	    		exception.printStackTrace();
	    	}
	    }
	    else {
	    	// DATA EXIST VERIFY THEN UPDATE IF NEEDED
	    	if(playerdata.isLong("P_"+player.getUniqueId()+"."+sType)) { // We check if exist
	    		iLeft = (calendar.getTimeInMillis() - playerdata.getLong("P_"+player.getUniqueId()+"."+sType))/1000;
	    	}
	    	else {
	    		// Is not exist so we need to create this (for old files versions)
	    		iLeft = config.getLong("iSecondDelay")+50; // We force an update
	    	}
	    	
	    	// CHECK IF WE CAN DO IT AGAIN BY CHECK IN config.yml
	    	if(iLeft >= config.getLong("iSecondDelay")) {
	    		// Ex: IF iLeft=70 AND iSecondeDelay=60 HE CAN REUSE IT
	    		// UPDATE DATA ALSO NAME IF CHANGE IT
	    		
	    		// We update some information, we update also the name in case of player have change his/her name
		    	try {
		    		playerdata.set("P_"+player.getUniqueId()+".name", player.getName());
		    		playerdata.set("P_"+player.getUniqueId()+"."+sType, calendar.getTimeInMillis());
		    		playerdata.save(playerdataf);
		    	} catch (IOException exception) {
		    		exception.printStackTrace();
		    	}
	    		return true;
	    	}
	    	else {
	    		// HERE MEAN STILL REMAIN TIME
	    		long iRemain = config.getLong("iSecondDelay") - iLeft;
	    		sendPluginMessage(player, sCorrectColor+String.format(language.getString("DelayReuse"), sErrorColor+iRemain, (iRemain>1?"s":"")+sCorrectColor));
	    		return false;
	    	}
	    }
	    return false;
	}

	/**
	 * This will play a litle music (nothing fancy)
	 * @param player
	 * @return
	 */
	private int playMusic(Player player) {
		
		int iTime = 0;
		for(float i=1; i<6; i++) {
			iTime = this.playSound(player,(float) ((i+1)*2), (int) (i*4));
		}
		return iTime;
	}
	
	/**
	 * This will return a sound
	 * @param player
	 * @param dTest
	 * @param iTick
	 * @return
	 */
	private int playSound(Player player, Float dTest, int iTick) {
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
	        public void run()
	        {
	        	player.playSound(player.getLocation().add(-10,5,1),Sound.BLOCK_NOTE_BLOCK_HARP, 0.8F, dTest/8);
	        	player.playSound(player.getLocation().add(-10,5,1),Sound.BLOCK_NOTE_BLOCK_BASS, 0.8F, dTest/8);
	        	player.playSound(player.getLocation().add(10,5,1),Sound.BLOCK_NOTE_BLOCK_GUITAR, 0.8F, dTest/8);
	        }
		}, iTick); // 20 ticks = 1 second -- So will update every 1 second

		return iTick;
	}
	
	/**
	 * This return the errorSound
	 * @param p
	 */
	private void playErrorSound(Player p) {
		playSound(p, 4F, 2);
		playSound(p, 1F, 4);
	}

	@Override
	public boolean onCommand(CommandSender sender,
		Command command,
		String label,
		String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(sPluginName+sErrorColor+"Cant be call from console");
			return true;
		}
		
		Player player1 = (Player) sender;
		
		// Are we in the good world?
		boolean bInGoodWorld = false;
		for(String world : config.getStringList("listWorlds")) {
			if(world.equalsIgnoreCase(player1.getWorld().getName().toLowerCase())) {
				bInGoodWorld = true;
				break; // No need to go further we got our world
			}
		}
		
		// If this is not in the good world advise the player
		if(!bInGoodWorld) {
			sendPluginMessage(player1, sErrorColor+" disabled for this world");
			return true;
		}
		
		// THE MAIN COMMAND
		if ((command.getName().equalsIgnoreCase("luckyman") && args.length == 0) || command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("help")) { // If the player typed /basic then do the following...
			// HELP COMMAND OR COMMAND WITHOUT ARGS
			
			// TAKE THE iEXP COST IN CONFIG IF NOT GOOD TAKE DEFAULT
			if(config.getBoolean("UseExpCost")) {
				sendPluginMessage(player1, sCorrectColor+String.format(language.getString("BewareLevel"),getExpLevelCost("small")));
			}
			sender.sendMessage(sObjectColor+command.getUsage());
			return true;
		}
		else if (command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("forfun")) {
			
			// UPDATE TIME AFTER ITEM AND CHECK IF OK (added to not abuse/spam it)
			if(!updateRemainTime(player1,"timeRemainForfun")) {
				playErrorSound(player1);
				return true;
			}
			
			int iStep = 0;
			
			// Take a random number between 0 to 5 
		    Random randomGenerator = new Random();
		    for (int idx = 0; idx <= 10; ++idx){
		    	iStep = randomGenerator.nextInt(6);
		    }
			
			// COMMAND FOR FUN HERE
		    PotionEffect Potion;
			switch(iStep) {
			
			case 0:
				Potion = new PotionEffect(PotionEffectType.CONFUSION, 200, 2);
				player1.playSound(player1.getLocation(),Sound.ENTITY_ZOMBIE_AMBIENT, 1F, 1F);
				player1.addPotionEffect(Potion);
				sendPluginMessage(player1,sObjectColor+language.getString("Zombify"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
			
			case 1:	
				Potion = new PotionEffect(PotionEffectType.BLINDNESS, 300, 2);
				player1.playSound(player1.getLocation(),Sound.ENTITY_BAT_DEATH, 1F, 1F);
				player1.addPotionEffect(Potion);
				sendPluginMessage(player1,sObjectColor+language.getString("lightPlz"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
				
			case 2:	
				Potion = new PotionEffect(PotionEffectType.SPEED, 400, 2);
				player1.playSound(player1.getLocation(),Sound.ITEM_FIRECHARGE_USE, 1F, 1F);
				player1.addPotionEffect(Potion);
				sendPluginMessage(player1,sObjectColor+language.getString("speedUp"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
				
			case 3:	
				Potion = new PotionEffect(PotionEffectType.GLOWING, 400, 2);
				player1.playSound(player1.getLocation(),Sound.BLOCK_GLASS_BREAK, 1F, 1F);
				player1.addPotionEffect(Potion);
				sendPluginMessage(player1,sObjectColor+language.getString("LightUp"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
				
			case 4:	
				Potion = new PotionEffect(PotionEffectType.JUMP, 400, 2);
				player1.playSound(player1.getLocation(),Sound.ENTITY_RABBIT_JUMP, 1F, 1F);
				player1.addPotionEffect(Potion);
				sendPluginMessage(player1,sObjectColor+language.getString("JumpingJack"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
			
			default: // default if we hit 5 or anything else if we change the 6 of anything else more than that
				player1.playSound(player1.getLocation(),Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1F, 1F);
				player1.playSound(player1.getLocation(),Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2F, 2F);
				sendPluginMessage(player1,sObjectColor+language.getString("ArgExplode"));
				player1.performCommand("me "+language.getString("BeenTroll"));
				break;
			
			}
			return true;
		}
		else if (command.getName().equalsIgnoreCase("luckyman") && args[0].equalsIgnoreCase("getluck")) {

			// If not arg after getluck
			if(args.length == 1) {
				sendPluginMessage(player1,sObjectColor+String.format(language.getString("helpType"), "small, medium, high"));
				return true;
			}
			
			String sType = "small";
			if(args.length > 1) {
				// switch between types
				sType = args[1].toLowerCase();
				switch(sType) {
					case "small":
					case "medium":
					case "high":
						this.sTypeChoosen = sType;
						break;
					
					default:
						sendPluginMessage(player1,sObjectColor+String.format(language.getString("getLuckType"),args[1]));
						return true;
				}
			}
			
			// If it from the console
			if (!(sender instanceof Player)) {
				sender.sendMessage(sPluginName+sErrorColor+"This command can only be run by a player.");
			} else {
				
				// check if player have something in hand
				if(player1.getInventory().getItemInMainHand().getType() == Material.AIR) {
					playErrorSound(player1);
					sendPluginMessage(player1,sObjectColor+language.getString("NothingHand"));
				}
				else if(config.getBoolean("UseExpCost") && player1.getLevel() < getExpLevelCost(sType)) {
					// If the player have not the level we need
					playErrorSound(player1);
					sendPluginMessage(player1,sObjectColor+String.format(language.getString("NeedXP"), getExpLevelCost(sType)));
				}
				else {
					
					// UPDATE TIME AFTER ITEM AND CHECK IF OK
					if(!updateRemainTime(player1, "timeRemain")) {
						playErrorSound(player1);
						return true;
					}
					
					ItemStack objStack = getChangeItemStack(player1.getInventory().getItemInMainHand(), sender);
					sendPluginMessage(player1,sCorrectColor+String.format(language.getString("ConvertItem"), sErrorColor+player1.getInventory().getItemInMainHand().getType().toString(), sCorrectColor, sObjectColor+objStack.getType().toString()));
					player1.getInventory().setItemInMainHand(new ItemStack(objStack));
					player1.setLevel(player1.getLevel()-getExpLevelCost(sType));
					// return to default type
					this.sTypeChoosen = "small";
					return true;
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
	        if (b.getType().toString().contains("_SIGN")) {
	   
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
	                	sendPluginMessage(p,sErrorColor+language.getString("signNotWork")+" /LuckyMan getLuck [small,medium,high]");
	                }
	            }
	        }
	    }
	}
	
	/**
	 * This return and object Stack
	 * @param sStackName
	 * @return
	 */
	public ItemStack getItemStackByName(String sStackName) {
		ItemStack objStack = null;
		if(Material.getMaterial(sStackName) != null) {
			objStack = new ItemStack(Material.getMaterial(sStackName), 1);
			
			Damageable im = (Damageable) objStack.getItemMeta();
			im.setDamage(im.getDamage()-im.getDamage()+10);
			objStack.setItemMeta((ItemMeta) im);
		}
		else {
			System.out.println("Error with Material "+sStackName);
		}
		
		return objStack;
	}
	
	/**
	 * This will exchange the old one with a new item
	 * @param ItemStackOld
	 * @param sender
	 * @return
	 */
	public ItemStack getChangeItemStack(ItemStack ItemStackOld, CommandSender sender) {
		
			// More than one object just throw all except one
			if(ItemStackOld.getAmount() > 1) {
			
				ItemStack ItemStackThrow = new ItemStack(ItemStackOld.getType());
				ItemStackThrow.setAmount(ItemStackOld.getAmount()-1);
				
				// If player have, for example 5 apples we throw in the air 4 of them and exchange only one apple
				Player player = (Player) sender;
				ItemStackOld.setAmount(1);
				player.getWorld().dropItem(player.getLocation().add(0, 5, 0), ItemStackThrow);
			}
		
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
		    
		    
			ItemStack objStack;
			
			// SET PERCENTAGE NO NEED TO CHECK Extra.percent... This is the last the rest
			int iPoorMaxPercent = objects.getInt("poor.percent");
			int iAverageMaxPercent = iPoorMaxPercent + objects.getInt("average.percent");
			int iBetterMaxPercent = iAverageMaxPercent + objects.getInt("better.percent");
			
			
			// POOR CHOICE HERE
			if(iChangeForGood >= 0 && iChangeForGood < iPoorMaxPercent) {
			
				for(String sItem : objects.getStringList("poor.objects")) {
					a_sItem.add(sItem);
				}
				
				// GET THE RANDOM OBJECT TO PASS
				iRandomItem = randomGenerator.nextInt(a_sItem.size());
				
				objStack = getItemStackByName((String) a_sItem.get(iRandomItem));
				objStack.setAmount(randomGenerator.nextInt(objects.getInt("poor.maxItem")+1)+1);
			}
			else if(iChangeForGood >= iPoorMaxPercent && iChangeForGood < iAverageMaxPercent) {
				
				for(String sItem : objects.getStringList("average.objects")) {
					a_sItem.add(sItem);
				}
				
				// GET THE RANDOM OBJECT TO PASS
				iRandomItem = randomGenerator.nextInt(a_sItem.size());

				objStack = getItemStackByName((String) a_sItem.get(iRandomItem));
				objStack.setAmount(randomGenerator.nextInt(objects.getInt("average.maxItem")+1)+1);
			}
			else if(iChangeForGood >= iAverageMaxPercent && iChangeForGood < iBetterMaxPercent) {
				
				for(String sItem : objects.getStringList("better.objects")) {
					a_sItem.add(sItem);
				}
				
				// GET THE RANDOM OBJECT TO PASS
				iRandomItem = randomGenerator.nextInt(a_sItem.size());
				
				objStack = getItemStackByName((String) a_sItem.get(iRandomItem));
				objStack.setAmount(randomGenerator.nextInt(objects.getInt("better.maxItem")+1)+1);
			}
			else if(iChangeForGood >= iBetterMaxPercent) {
				
				for (String key : objects.getConfigurationSection("extra.objects").getKeys(true)) {
					a_sItem.add(key);
				}
				
				// GET THE RANDOM OBJECT TO PASS
				iRandomItem = randomGenerator.nextInt(a_sItem.size());
				
				objStack = getItemStackByName((String) a_sItem.get(iRandomItem));
								
				for(String sEnchant : objects.getStringList("extra.objects."+(String) a_sItem.get(iRandomItem))) {
					for(Enchantment objEnchant : Enchantment.values()) {
						if(objEnchant.getKey().toString().contains(sEnchant.toLowerCase())){
							objStack.addUnsafeEnchantment(objEnchant, 2);
						}
					}
				}
			}
			else {
				// Bad error here we cancel the transaction and return the old stack!
				sendPluginMessage((Player) sender,sErrorColor+language.getString("Oupps"));
				return ItemStackOld;
			}
			
			// Successful we play the little music
			playMusic((Player) sender);
			return objStack;
	}
}
