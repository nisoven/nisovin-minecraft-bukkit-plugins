package com.nisovin.bookworm;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;

public class BookWormSpoutInventoryListener extends InventoryListener {

	public BookWormSpoutInventoryListener(BookWorm plugin) {
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack itemClicked = event.getItem();
		ItemStack itemHolding = event.getCursor();
		if (itemClicked != null && itemClicked.getType() == Material.BOOK) {
			if (itemHolding != null && itemHolding.getType() == Material.BOOK && itemClicked.getDurability() != itemHolding.getDurability()) {
				event.setCancelled(true);
			}
		}
	}
	
}
