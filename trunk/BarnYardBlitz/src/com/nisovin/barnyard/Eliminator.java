package com.nisovin.barnyard;

import org.bukkit.Bukkit;

public class Eliminator implements Runnable {

	boolean stopped = false;
	BarnYardBlitz plugin;
	long nextElimination = 0;
	
	public Eliminator(BarnYardBlitz plugin) {
		this.plugin = plugin;
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < plugin.eliminationIntervals.length; i++) {
				nextElimination = System.currentTimeMillis() + plugin.eliminationIntervals[i] * 1000;
				Thread.sleep(plugin.eliminationIntervals[i] * 1000);
				if (stopped) return;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						plugin.calculateScoresAndDoElimination();
					}
				}, 1);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public long timeToNextElimination() {
		if (nextElimination == 0) {
			return 0;
		} else {
			return (nextElimination - System.currentTimeMillis()) / 1000;
		}
	}
	
	public void stop() {
		stopped = true;
		nextElimination = 0;
	}
	
}