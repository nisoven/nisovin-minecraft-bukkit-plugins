package com.nisovin.magicspells.spells.instant;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class PrayerSpell extends InstantSpell {
	
	private int amountHealed;
	private String strAtFullHealth;
	private boolean checkPlugins;

	public PrayerSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		amountHealed = getConfigInt("amount-healed", 10);
		strAtFullHealth = getConfigString("str-at-full-health", "You are already at full health.");
		checkPlugins = getConfigBoolean("check-plugins", true);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (player.getHealth() == 20 && amountHealed > 0) {
				sendMessage(player, strAtFullHealth);
				return PostCastAction.ALREADY_HANDLED;
			} else if (player.isValid()) {
				int health = player.getHealth();
				if (health > 0) {
					int amt = Math.round(amountHealed*power);
					if (checkPlugins && amt > 0) {
						EntityRegainHealthEvent evt = new EntityRegainHealthEvent(player, amt, RegainReason.CUSTOM);
						Bukkit.getPluginManager().callEvent(evt);
						if (evt.isCancelled()) {
							return PostCastAction.ALREADY_HANDLED;
						}
						amt = evt.getAmount();
					}
					health += amt;
					if (health > 20) {
						health = 20;
					} else if (health < 0) {
						health = 0;
					}
					player.setHealth(health);
					playSpellEffects(EffectPosition.CASTER, player);
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
