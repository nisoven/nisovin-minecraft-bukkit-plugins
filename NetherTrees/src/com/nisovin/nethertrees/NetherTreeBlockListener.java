package com.nisovin.nethertrees;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

public class NetherTreeBlockListener extends BlockListener {
	
	public NetherTreeBlockListener(NetherTrees plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_CANBUILD, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onBlockCanBuild(BlockCanBuildEvent event) {
		if (event.getMaterialId() == NetherTrees.SAPLING_TYPE && event.getBlock().getRelative(0,-1,0).getType() == Material.NETHERRACK) {
			NetherTreePopulator.generateTree(event.getBlock(), new Random());
			//event.setBuildable(true);
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			Block b = event.getBlock();
			if (b.getWorld().getEnvironment() == Environment.NETHER) {
				if (b.getTypeId() == NetherTrees.LEAF_TYPE && Math.random()*100 < NetherTrees.SAPLING_DROP_CHANCE) {
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(NetherTrees.SAPLING_TYPE, 1));
				}
			}
		}
	}

}
