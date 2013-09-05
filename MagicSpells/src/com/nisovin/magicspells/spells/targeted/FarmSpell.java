package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class FarmSpell extends TargetedSpell implements TargetedLocationSpell {

	private int radius;
	private int growth;
	private int newCropType;
	private boolean targeted;
	
	public FarmSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		radius = getConfigInt("radius", 3);
		growth = getConfigInt("growth", 1);
		newCropType = getConfigInt("new-crop-type", Material.CROPS.getId());
		targeted = getConfigBoolean("targeted", false);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block block = null;
			if (targeted) {
				block = player.getTargetBlock(MagicSpells.getTransparentBlocks(), range);
			} else {
				block = player.getLocation().subtract(0, 1, 0).getBlock();
			}
			if (block != null) {
				SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, player, block.getLocation());
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					block = null;
				} else {
					block = event.getTargetLocation().getBlock();
				}
			}
			if (block != null) {
				boolean farmed = farm(block, Math.round(radius * power));
				if (!farmed) return noTarget(player);
				playSpellEffects(EffectPosition.CASTER, player);
				if (targeted) {
					playSpellEffects(EffectPosition.TARGET, block.getLocation());
				}
			} else {
				return noTarget(player);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	private boolean farm(Block center, int radius) {
		int cx = center.getX();
		int y = center.getY();
		int cz = center.getZ();
		
		int count = 0;
		for (int x = cx - radius; x <= cx + radius; x++) {
			for (int z = cz - radius; z <= cz + radius; z++) {
				Block b = center.getWorld().getBlockAt(x, y, z);
				if (b.getType() != Material.SOIL) {
					b = b.getRelative(BlockFace.DOWN);
					if (b.getType() != Material.SOIL) {
						continue;
					}
				}
				b = b.getRelative(BlockFace.UP);
				if (b.getType() == Material.AIR) {
					if (newCropType > 0) {
						b.setTypeId(newCropType);
						if (growth > 1) b.setData((byte) (growth-1));
						count++;
					}
				} else if ((b.getType() == Material.CROPS || b.getType() == Material.CARROT || b.getType() == Material.POTATO) && b.getData() < 7) {
					byte newData = (byte) (b.getData() + growth);
					if (newData > 7) newData = 7;
					b.setData(newData);
					count++;
				}
			}
		}
		
		return count > 0;
	}
	
	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		return farm(target.subtract(0, 1, 0).getBlock(), Math.round(radius * power));
	}

}
