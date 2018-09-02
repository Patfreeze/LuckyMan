package com.amedacier;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class LuckyManTabCompleter implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		
		List<String> list = new ArrayList<>();
		
		switch(cmd.getName().toLowerCase()) {
			
			case "luckyman":
				// TheClock module so check what we need
				
				switch(args.length) {
					case 1: // First level
						
						list.add("Help");
						list.add("GetLuck");
						list.add("ForFun");
						return list;
						
					case 2: // We are on a second level check on what
						switch(args[0].toLowerCase()) {
						
							case "getluck": // second list
								list.add("Small");
								list.add("Medium");
								list.add("High");
								return list;
													
							default:
								return null;
						}
						
					default:
						return null;
				}
				
			default: // Not a command from LuckMan so ignore it
				return null;
		
		}
	}
}
