package com.nisovin.magicspells.volatilecode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_7_R1.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftTNTPrimed;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.BoundingBox;
import com.nisovin.magicspells.util.DisguiseManager;
import com.nisovin.magicspells.util.MagicConfig;

public class VolatileCodeEnabled_1_7_R1 implements VolatileCodeHandle {

	
	private static NBTTagCompound getTag(ItemStack item) {
		if (item instanceof CraftItemStack) {
			try {
				Field field = CraftItemStack.class.getDeclaredField("handle");
				field.setAccessible(true);
				return ((net.minecraft.server.v1_7_R1.ItemStack)field.get(item)).tag;
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	private static ItemStack setTag(ItemStack item, NBTTagCompound tag) {
		CraftItemStack craftItem = null;
		if (item instanceof CraftItemStack) {
			craftItem = (CraftItemStack)item;
		} else {
			craftItem = CraftItemStack.asCraftCopy(item);
		}
		
		net.minecraft.server.v1_7_R1.ItemStack nmsItem = null;
		try {
			Field field = CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
			nmsItem = ((net.minecraft.server.v1_7_R1.ItemStack)field.get(item));
		} catch (Exception e) {
		}
		if (nmsItem == null) {
			nmsItem = CraftItemStack.asNMSCopy(craftItem);
		}
		
		nmsItem.tag = tag;
		try {
			Field field = CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
			field.set(craftItem, nmsItem);
		} catch (Exception e) {
		}
		
		return craftItem;
	}
	
	public VolatileCodeEnabled_1_7_R1() {
		try {
			packet63Fields[0] = PacketPlayOutWorldParticles.class.getDeclaredField("a");
			packet63Fields[1] = PacketPlayOutWorldParticles.class.getDeclaredField("b");
			packet63Fields[2] = PacketPlayOutWorldParticles.class.getDeclaredField("c");
			packet63Fields[3] = PacketPlayOutWorldParticles.class.getDeclaredField("d");
			packet63Fields[4] = PacketPlayOutWorldParticles.class.getDeclaredField("e");
			packet63Fields[5] = PacketPlayOutWorldParticles.class.getDeclaredField("f");
			packet63Fields[6] = PacketPlayOutWorldParticles.class.getDeclaredField("g");
			packet63Fields[7] = PacketPlayOutWorldParticles.class.getDeclaredField("h");
			packet63Fields[8] = PacketPlayOutWorldParticles.class.getDeclaredField("i");
			for (int i = 0; i <= 8; i++) {
				packet63Fields[i].setAccessible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addPotionGraphicalEffect(LivingEntity entity, int color, int duration) {
		final EntityLiving el = ((CraftLivingEntity)entity).getHandle();
		final DataWatcher dw = el.getDataWatcher();
		dw.watch(7, Integer.valueOf(color));
		
		if (duration > 0) {
			MagicSpells.scheduleDelayedTask(new Runnable() {
				public void run() {
					int c = 0;
					if (!el.effects.isEmpty()) {
						c = net.minecraft.server.v1_7_R1.PotionBrewer.a(el.effects.values());
					}
					dw.watch(7, Integer.valueOf(c));
				}
			}, duration);
		}
	}

	@Override
	public void entityPathTo(LivingEntity creature, LivingEntity target) {
		EntityCreature entity = ((CraftCreature)creature).getHandle();
		entity.pathEntity = entity.world.findPath(entity, ((CraftLivingEntity)target).getHandle(), 16.0F, true, false, false, false);
	}

	@Override
	public void sendFakeSlotUpdate(Player player, int slot, ItemStack item) {
		net.minecraft.server.v1_7_R1.ItemStack nmsItem;
		if (item != null) {
			nmsItem = CraftItemStack.asNMSCopy(item);
		} else {
			nmsItem = null;
		}
		PacketPlayOutSetSlot packet = new PacketPlayOutSetSlot(0, (short)slot+36, nmsItem);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void toggleLeverOrButton(Block block) {
		net.minecraft.server.v1_7_R1.Block.e(block.getType().getId()).interact(((CraftWorld)block.getWorld()).getHandle(), block.getX(), block.getY(), block.getZ(), null, 0, 0, 0, 0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void pressPressurePlate(Block block) {
		block.setData((byte) (block.getData() ^ 0x1));
		net.minecraft.server.v1_7_R1.World w = ((CraftWorld)block.getWorld()).getHandle();
		w.applyPhysics(block.getX(), block.getY(), block.getZ(), net.minecraft.server.v1_7_R1.Block.e(block.getType().getId()));
		w.applyPhysics(block.getX(), block.getY()-1, block.getZ(), net.minecraft.server.v1_7_R1.Block.e(block.getType().getId()));
	}

	@Override
	public boolean simulateTnt(Location target, LivingEntity source, float explosionSize, boolean fire) {
        EntityTNTPrimed e = new EntityTNTPrimed(((CraftWorld)target.getWorld()).getHandle(), target.getX(), target.getY(), target.getZ(), ((CraftLivingEntity)source).getHandle());
        CraftTNTPrimed c = new CraftTNTPrimed((CraftServer)Bukkit.getServer(), e);
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(c, explosionSize, fire);
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event.isCancelled();
	}

	@Override
	public boolean createExplosionByPlayer(Player player, Location location, float size, boolean fire, boolean breakBlocks) {
		return !((CraftWorld)location.getWorld()).getHandle().createExplosion(((CraftPlayer)player).getHandle(), location.getX(), location.getY(), location.getZ(), size, fire, breakBlocks).wasCanceled;
	}

	@Override
	public void playExplosionEffect(Location location, float size) {
		@SuppressWarnings("rawtypes")
		PacketPlayOutExplosion packet = new PacketPlayOutExplosion(location.getX(), location.getY(), location.getZ(), size, new ArrayList(), null);
		for (Player player : location.getWorld().getPlayers()) {
			if (player.getLocation().distanceSquared(location) < 50 * 50) {
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@Override
	public void setExperienceBar(Player player, int level, float percent) {
		PacketPlayOutExperience packet = new PacketPlayOutExperience(percent, player.getTotalExperience(), level);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public Fireball shootSmallFireball(Player player) {
		net.minecraft.server.v1_7_R1.World w = ((CraftWorld)player.getWorld()).getHandle();
		Location playerLoc = player.getLocation();
		Vector loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(10));
		
		double d0 = loc.getX() - playerLoc.getX();
        double d1 = loc.getY() - (playerLoc.getY() + 1.5);
        double d2 = loc.getZ() - playerLoc.getZ();
		EntitySmallFireball entitysmallfireball = new EntitySmallFireball(w, ((CraftPlayer)player).getHandle(), d0, d1, d2);

        entitysmallfireball.locY = playerLoc.getY() + 1.5;
        w.addEntity(entitysmallfireball);
        
        return (Fireball)entitysmallfireball.getBukkitEntity();
	}

	@Override
	public void setTarget(LivingEntity entity, LivingEntity target) {
		if (entity instanceof Creature) {
			((Creature)entity).setTarget(target);
		}
		((EntityInsentient)((CraftLivingEntity)entity).getHandle()).setGoalTarget(((CraftLivingEntity)target).getHandle());
	}

	@Override
	public void playSound(Location location, String sound, float volume, float pitch) {
		((CraftWorld)location.getWorld()).getHandle().makeSound(location.getX(), location.getY(), location.getZ(), sound, volume, pitch);
	}

	@Override
	public void playSound(Player player, String sound, float volume, float pitch) {
		Location loc = player.getLocation();
		PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(sound, loc.getX(), loc.getY(), loc.getZ(), volume, pitch);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public ItemStack addFakeEnchantment(ItemStack item) {
		if (!(item instanceof CraftItemStack)) {
			item = CraftItemStack.asCraftCopy(item);
		}
		NBTTagCompound tag = getTag(item);		
		if (tag == null) {
			tag = new NBTTagCompound();
		}
		if (!tag.hasKey("ench")) {
			tag.set("ench", new NBTTagList());
		}		
		return setTag(item, tag);
	}

	@Override
	public void setFallingBlockHurtEntities(FallingBlock block, float damage, int max) {
		EntityFallingBlock efb = ((CraftFallingSand)block).getHandle();
		try {
			Field field = EntityFallingBlock.class.getDeclaredField("hurtEntities");
			field.setAccessible(true);
			field.setBoolean(efb, true);
			
			field = EntityFallingBlock.class.getDeclaredField("fallHurtAmount");
			field.setAccessible(true);
			field.setFloat(efb, damage);
			
			field = EntityFallingBlock.class.getDeclaredField("fallHurtMax");
			field.setAccessible(true);
			field.setInt(efb, max);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void playEntityAnimation(final Location location, final EntityType entityType, final int animationId, boolean instant) {
		final EntityLiving entity;
		if (entityType == EntityType.VILLAGER) {
			entity = new EntityVillager(((CraftWorld)location.getWorld()).getHandle());
		} else if (entityType == EntityType.WITCH) {
			entity = new EntityWitch(((CraftWorld)location.getWorld()).getHandle());
		} else if (entityType == EntityType.OCELOT) {
			entity = new EntityOcelot(((CraftWorld)location.getWorld()).getHandle());
		} else {
			entity = null;
		}
		if (entity == null) return;
		
		entity.setPosition(location.getX(), instant ? location.getY() : -5, location.getZ());
		((CraftWorld)location.getWorld()).getHandle().addEntity(entity);
		entity.addEffect(new MobEffect(14, 40));
		if (instant) {
			((CraftWorld)location.getWorld()).getHandle().broadcastEntityEffect(entity, (byte)animationId);
			entity.getBukkitEntity().remove();
		} else {
			entity.setPosition(location.getX(), location.getY(), location.getZ());
			MagicSpells.scheduleDelayedTask(new Runnable() {
				public void run() {
					((CraftWorld)location.getWorld()).getHandle().broadcastEntityEffect(entity, (byte)animationId);
					entity.getBukkitEntity().remove();
				}
			}, 8);
		}
	}

	@Override
	public void createFireworksExplosion(Location location, boolean flicker, boolean trail, int type, int[] colors, int[] fadeColors, int flightDuration) {
		// create item
		net.minecraft.server.v1_7_R1.ItemStack item = new net.minecraft.server.v1_7_R1.ItemStack(Item.d(401), 1, 0);
		
		// get tag
		NBTTagCompound tag = item.tag;
		if (tag == null) {
			tag = new NBTTagCompound();
		}
		
		// create explosion tag
		NBTTagCompound explTag = new NBTTagCompound();
		explTag.setByte("Flicker", flicker ? (byte)1 : (byte)0);
		explTag.setByte("Trail", trail ? (byte)1 : (byte)0);
		explTag.setByte("Type", (byte)type);
		explTag.setIntArray("Colors", colors);
		explTag.setIntArray("FadeColors", fadeColors);
		
		// create fireworks tag
		NBTTagCompound fwTag = new NBTTagCompound();
		fwTag.setByte("Flight", (byte)flightDuration);
		NBTTagList explList = new NBTTagList();
		explList.add(explTag);
		fwTag.set("Explosions", explList);
		tag.set("Fireworks", fwTag);
		
		// set tag
		item.tag = tag;
		
		// create fireworks entity
		EntityFireworks fireworks = new EntityFireworks(((CraftWorld)location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(), item);
		((CraftWorld)location.getWorld()).getHandle().addEntity(fireworks);
		
		// cause explosion
		if (flightDuration == 0) {
			((CraftWorld)location.getWorld()).getHandle().broadcastEntityEffect(fireworks, (byte)17);
			fireworks.die();
		}
	}
	
	Field[] packet63Fields = new Field[9];
	@Override
	public void playParticleEffect(Location location, String name, float spreadHoriz, float spreadVert, float speed, int count, int radius, float yOffset) {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles();
		try {
			packet63Fields[0].set(packet, name);
			packet63Fields[1].setFloat(packet, (float)location.getX());
			packet63Fields[2].setFloat(packet, (float)location.getY() + yOffset);
			packet63Fields[3].setFloat(packet, (float)location.getZ());
			packet63Fields[4].setFloat(packet, spreadHoriz);
			packet63Fields[5].setFloat(packet, spreadVert);
			packet63Fields[6].setFloat(packet, spreadHoriz);
			packet63Fields[7].setFloat(packet, speed);
			packet63Fields[8].setInt(packet, count);
			
			int rSq = radius * radius;
			
			for (Player player : location.getWorld().getPlayers()) {
				if (player.getLocation().distanceSquared(location) <= rSq) {
					((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
				} else {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void playDragonDeathEffect(Location location) {
		EntityEnderDragon dragon = new EntityEnderDragon(((CraftWorld)location.getWorld()).getHandle());
		dragon.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), 0F);
		
		PacketPlayOutSpawnEntityLiving packet24 = new PacketPlayOutSpawnEntityLiving(dragon);
		PacketPlayOutEntityStatus packet38 = new PacketPlayOutEntityStatus(dragon, (byte)3);
		final PacketPlayOutEntityDestroy packet29 = new PacketPlayOutEntityDestroy(dragon.getBukkitEntity().getEntityId());
		
		BoundingBox box = new BoundingBox(location, 64);
		final List<Player> players = new ArrayList<Player>();
		for (Player player : location.getWorld().getPlayers()) {
			if (box.contains(player)) {
				players.add(player);
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet24);
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet38);
			}
		}
		
		MagicSpells.scheduleDelayedTask(new Runnable() {
			public void run() {
				for (Player player : players) {
					if (player.isValid()) {
						((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet29);
					}
				}
			}
		}, 250);
	}
	
	@Override
	public void setKiller(LivingEntity entity, Player killer) {
		((CraftLivingEntity)entity).getHandle().killer = ((CraftPlayer)killer).getHandle();
	}
	
	@Override
	public DisguiseManager getDisguiseManager(MagicConfig config) {
		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
			return new DisguiseManager_1_7_R1_ProtocolLib(config);
		} else {
			return new DisguiseManager_1_7_R1_Injected(config);
		}
	}

	@Override
	public ItemStack addAttributes(ItemStack item, String[] names, String[] types, double[] amounts, int[] operations) {
		if (!(item instanceof CraftItemStack)) {
			item = CraftItemStack.asCraftCopy(item);
		}
		NBTTagCompound tag = getTag(item);
		
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < names.length; i++) {
			if (names[i] != null) {
				NBTTagCompound attr = new NBTTagCompound();
				attr.setString("Name", names[i]);
				attr.setString("AttributeName", types[i]);
				attr.setDouble("Amount", amounts[i]);
				attr.setInt("Operation", operations[i]);
				UUID uuid = UUID.randomUUID();
				attr.setLong("UUIDLeast", uuid.getLeastSignificantBits());
				attr.setLong("UUIDMost", uuid.getMostSignificantBits());
				list.add(attr);
			}
		}
		
		tag.set("AttributeModifiers", list);
		
		setTag(item, tag);
		return item;
	}

	@Override
	public void addEntityAttribute(LivingEntity entity, String attribute, double amount, int operation) {
		EntityInsentient nmsEnt = (EntityInsentient) ((CraftLivingEntity)entity).getHandle();
		IAttribute attr = null;
		if (attribute.equals("generic.maxHealth")) {
			attr = GenericAttributes.a;
		} else if (attribute.equals("generic.followRange")) {
			attr = GenericAttributes.b;
		} else if (attribute.equals("generic.knockbackResistance")) {
			attr = GenericAttributes.c;
		} else if (attribute.equals("generic.movementSpeed")) {
			attr = GenericAttributes.e;
		} else if (attribute.equals("generic.attackDamage")) {
			attr = GenericAttributes.e;
		}
		if (attr != null) {
			AttributeInstance attributes = nmsEnt.getAttributeInstance(attr);
			attributes.a(new AttributeModifier("MagicSpells " + attribute, amount, operation));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void removeAI(LivingEntity entity) {
        try {
        	EntityInsentient ev = (EntityInsentient)((CraftLivingEntity)entity).getHandle();
               
            Field goalsField = EntityInsentient.class.getDeclaredField("goalSelector");
            goalsField.setAccessible(true);
            PathfinderGoalSelector goals = (PathfinderGoalSelector) goalsField.get(ev);
           
            Field listField = PathfinderGoalSelector.class.getDeclaredField("b");
            listField.setAccessible(true);
            List list = (List)listField.get(goals);
            list.clear();
            listField = PathfinderGoalSelector.class.getDeclaredField("c");
            listField.setAccessible(true);
            list = (List)listField.get(goals);
            list.clear();

            goals.a(0, new PathfinderGoalFloat(ev));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void addAILookAtPlayer(LivingEntity entity, int range) {
        try {
        	EntityInsentient ev = (EntityInsentient)((CraftLivingEntity)entity).getHandle();
               
            Field goalsField = EntityInsentient.class.getDeclaredField("goalSelector");
            goalsField.setAccessible(true);
            PathfinderGoalSelector goals = (PathfinderGoalSelector) goalsField.get(ev);

            goals.a(1, new PathfinderGoalLookAtPlayer(ev, EntityHuman.class, range, 1.0F));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
