package com.amedacier.luckyman;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiCMD {

    private void placeItem(Inventory worldGui, int position, Material material, String displayName, ArrayList<String> lore, int amount) {
        placeItem(worldGui,position, material, displayName, lore, amount, true);
    }

    private void placeItem(Inventory worldGui, int position, Material material, String displayName, ArrayList<String> lore, int amount, boolean bEnchanted) {

        ItemStack ref1 = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta metaref1 = ref1.getItemMeta();

        ref1 = new ItemStack(material);
        ref1.setAmount(amount);
        metaref1 = ref1.getItemMeta();

        // Set Lore to Item
        metaref1.setLore(lore);
        metaref1.setDisplayName(displayName);

        ref1.setItemMeta(metaref1);
        if(bEnchanted) {
            ref1.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 5);
        }
        worldGui.setItem(position, ref1);
    }

    private void placeVoidItems(Inventory worldGui) {

        ArrayList<String> lore = new ArrayList<String>();

        for(int i=0; i<=26; i++) {
            placeItem(worldGui, i, Material.BLACKSTONE, "!!!", lore, 1, false);
        }
    }

    public GuiCMD(LuckyMan thisPlugin, CommandSender sender, String sGuiType, File file) throws IOException {

        File configf = new File(file,"config.yml");
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configf);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        int iLow = 0;
        int iMed = 0;
        int iHigh = 0;

        // If we take XP in exchange
        if(config.getBoolean("UseExpCost")) {
            iLow = config.getInt("ExpCostLow");
            iMed = config.getInt("ExpCostMedium");
            iHigh = config.getInt("ExpCostHigh");
        }
        Player player = null;
        if(sender instanceof Player) {
            player = (Player) sender;
        }
        if(player != null) {


            // Here we create our named help "inventory"
            Inventory worldGui = Bukkit.getServer().createInventory(player, InventoryType.CHEST, "LuckyMan: "+sGuiType);

            // We place a fake item on all and set the good one
            placeVoidItems(worldGui);

            switch(sGuiType.toLowerCase()) {
                case "getluck":

                    // 10 - 13 - 16

                    ////////////////////////////////////////////////////////////////////////////////////////////////////
                    //
                    // XP -> GETLUCK
                    //
                    /////////////////////////////////////////////////////////////////////////////////////////////////////

                    // SMALLER
                    ArrayList<String> lore = new ArrayList<String>();
                    lore.add("§2§cClick to exchange");

                    if(iLow > 0) {
                        lore.add("§c§2Cost " + iLow+"xp");
                    }
                    placeItem(worldGui, 10, Material.WOODEN_HOE, "§2Smaller chance!", lore, 1);

                    // MEDIUM
                    lore = new ArrayList<String>();
                    lore.add("§2§cClick to exchange");

                    if(iMed > 0) {
                        lore.add("§c§2Cost " + iMed+"xp");
                    }
                    placeItem(worldGui, 13, Material.IRON_HOE, "§2Medium chance!", lore, 1);

                    // HIGHER
                    lore = new ArrayList<String>();
                    lore.add("§2§cClick to exchange");

                    if(iHigh > 0) {
                        lore.add("§c§2Cost " + iHigh+"xp");
                    }
                    placeItem(worldGui, 16, Material.GOLDEN_HOE, "§2Higher chance!", lore, 1);

                    break;

                default:
                    System.out.println(sGuiType+" is not implemented sorry!");
                    return;
            }

            //Here opens the inventory
            player.openInventory(worldGui);
        }
    }

}