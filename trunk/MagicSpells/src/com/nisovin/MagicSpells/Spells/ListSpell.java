package com.nisovin.MagicSpells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.CommandSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;
import com.nisovin.MagicSpells.Spellbook;

public class ListSpell extends CommandSpell {
	
	private int lineLength = 60;
	private boolean reloadGrantedSpells;
	private String strNoSpells;
	private String strPrefix;

	public ListSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		reloadGrantedSpells = config.getBoolean("spells." + spellName + ".reload-granted-spells", false);
		strNoSpells = config.getString("spells." + spellName + ".str-no-spells", "You do not know any spells.");
		strPrefix = config.getString("spells." + spellName + ".str-prefix", "Known spells:");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			String extra = "";
			if (args != null && args.length > 0 && spellbook.hasAdvancedPerm()) {
				Player p = Bukkit.getServer().getPlayer(args[0]);
				if (p != null) {
					spellbook = MagicSpells.getSpellbook(p);
					extra = "(" + p.getDisplayName() + ") ";
				}
			}
			if (spellbook != null && reloadGrantedSpells) {
				spellbook.addGrantedSpells();
			}
			if (spellbook == null || spellbook.getSpells().size() == 0) {
				// no spells
				sendMessage(player, strNoSpells);
			} else {
				String s = "";
				for (Spell spell : spellbook.getSpells()) {
					if (s.equals("")) {
						s = spell.getName();
					} else {
						s += ", " + spell.getName();
					}
				}
				s = strPrefix + " " + extra + s;
				while (s.length() > lineLength) {
					int i = s.substring(0, lineLength).lastIndexOf(' ');
					sendMessage(player, s.substring(0, i));
					s = s.substring(i+1);
				}
				if (s.length() > 0) {
					sendMessage(player, s);
				}
			}
			return PostCastAction.ALREADY_HANDLED;
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}

}
