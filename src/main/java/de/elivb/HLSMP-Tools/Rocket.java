package de.elivb.donutTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class Rocket {
   private final Tools plugin;
   private final FileConfiguration config;
   private final ItemStack item;
   private final NamespacedKey usesKey;
   private final NamespacedKey createdKey;
   private final NamespacedKey expiresKey;
   private final NamespacedKey modeKey;
   private final NamespacedKey ownerKey;
   private final int customModelData;
   private final Material material;
   private final Sound soundType;
   private final float soundVolume;
   private final float soundPitch;
   private final Particle mainParticle;
   private final int mainParticleCount;
   private final Particle additionalParticle;
   private final int additionalParticleCount;
   private final int flightDuration;
   private final DurabilityMode durabilityMode;
   private final int maxUses;
   private final String timeDuration;
   private final long durationMillis;

   public Rocket(Tools plugin) {
      this.plugin = plugin;
      String var10002 = String.valueOf(plugin.getDataFolder());
      File configFile = new File(var10002 + "/DonutTools", "rocket.yml");
      this.config = YamlConfiguration.loadConfiguration(configFile);
      this.usesKey = new NamespacedKey(plugin, "rocket_uses");
      this.createdKey = new NamespacedKey(plugin, "rocket_created");
      this.expiresKey = new NamespacedKey(plugin, "rocket_expires");
      this.modeKey = new NamespacedKey(plugin, "rocket_mode");
      this.ownerKey = new NamespacedKey(plugin, "rocket_owner");
      String materialName = this.config.getString("material", "FIREWORK_ROCKET");
      Material tempMaterial = Material.matchMaterial(materialName);
      this.material = tempMaterial == null ? Material.FIREWORK_ROCKET : tempMaterial;
      this.customModelData = this.config.getInt("custom-model-data", 1001);
      String mode = this.config.getString("durability.mode", "infinite").toLowerCase();
      this.durabilityMode = Rocket.DurabilityMode.fromString(mode);
      this.maxUses = this.config.getInt("durability.uses", 100);
      this.timeDuration = this.config.getString("durability.time", "3d");
      this.durationMillis = this.parseDuration(this.timeDuration);
      this.flightDuration = this.config.getInt("rocket.flight-duration", 1);
      String soundName = this.config.getString("sound.type", "ENTITY_FIREWORK_ROCKET_LAUNCH");
      this.soundType = this.getSoundByName(soundName);
      this.soundVolume = (float)this.config.getDouble("sound.volume", (double)1.0F);
      this.soundPitch = (float)this.config.getDouble("sound.pitch", (double)1.0F);
      String mainParticleName = this.config.getString("particle.type", "FIREWORK");
      this.mainParticle = this.getParticleByName(mainParticleName);
      this.mainParticleCount = this.config.getInt("particle.count", 30);
      String additionalParticleName = this.config.getString("particle.additional-particle.type", "FLAME");
      this.additionalParticle = this.getParticleByName(additionalParticleName);
      this.additionalParticleCount = this.config.getInt("particle.additional-particle.count", 20);
      this.item = this.createItem();
   }

   private long parseDuration(String duration) {
      if (duration != null && !duration.isEmpty()) {
         duration = duration.toLowerCase().trim();

         try {
            if (duration.endsWith("d")) {
               return Long.parseLong(duration.substring(0, duration.length() - 1)) * 24L * 60L * 60L * 1000L;
            } else if (duration.endsWith("h")) {
               return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60L * 60L * 1000L;
            } else if (duration.endsWith("m")) {
               return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60L * 1000L;
            } else {
               return duration.endsWith("w") ? Long.parseLong(duration.substring(0, duration.length() - 1)) * 7L * 24L * 60L * 60L * 1000L : Long.parseLong(duration) * 60L * 1000L;
            }
         } catch (NumberFormatException var3) {
            return 259200000L;
         }
      } else {
         return 259200000L;
      }
   }

   private Sound getSoundByName(String name) {
      if (name != null && !name.isEmpty()) {
         switch (name.toUpperCase()) {
            case "ENTITY_FIREWORK_ROCKET_LAUNCH" -> {
               return Sound.ENTITY_FIREWORK_ROCKET_LAUNCH;
            }
            case "ENTITY_FIREWORK_ROCKET_BLAST" -> {
               return Sound.ENTITY_FIREWORK_ROCKET_BLAST;
            }
            case "ENTITY_FIREWORK_ROCKET_TWINKLE" -> {
               return Sound.ENTITY_FIREWORK_ROCKET_TWINKLE;
            }
            case "ENTITY_FIREWORK_ROCKET_LARGE_BLAST" -> {
               return Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;
            }
            default -> {
               return Sound.ENTITY_FIREWORK_ROCKET_LAUNCH;
            }
         }
      } else {
         return Sound.ENTITY_FIREWORK_ROCKET_LAUNCH;
      }
   }

   private Particle getParticleByName(String name) {
      if (name != null && !name.isEmpty()) {
         switch (name.toUpperCase()) {
            case "FIREWORK" -> {
               return Particle.FIREWORK;
            }
            case "FLAME" -> {
               return Particle.FLAME;
            }
            case "LAVA" -> {
               return Particle.LAVA;
            }
            case "SMOKE" -> {
               return Particle.SMOKE;
            }
            case "CAMPFIRE_COSY_SMOKE" -> {
               return Particle.CAMPFIRE_COSY_SMOKE;
            }
            case "SOUL_FIRE_FLAME" -> {
               return Particle.SOUL_FIRE_FLAME;
            }
            case "ELECTRIC_SPARK" -> {
               return Particle.ELECTRIC_SPARK;
            }
            case "CRIT" -> {
               return Particle.CRIT;
            }
            case "DRAGON_BREATH" -> {
               return Particle.DRAGON_BREATH;
            }
            case "PORTAL" -> {
               return Particle.PORTAL;
            }
            default -> {
               return Particle.FIREWORK;
            }
         }
      } else {
         return Particle.FIREWORK;
      }
   }

   private ItemStack createItem() {
      ItemStack rocket = new ItemStack(this.material);
      ItemMeta meta = rocket.getItemMeta();
      if (meta != null) {
         String displayName = this.config.getString("name", "&#A20AD6ɪɴꜰɪɴɪᴛʏ ʀᴏᴄᴋᴇᴛ");
         meta.setDisplayName(HexColorCode.translateAllColorCodes(displayName));
         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.modeKey, PersistentDataType.STRING, this.durabilityMode.name());
         long currentTime = System.currentTimeMillis();
         switch (this.durabilityMode.ordinal()) {
            case 0:
               container.set(this.usesKey, PersistentDataType.INTEGER, this.maxUses);
               break;
            case 1:
               container.set(this.createdKey, PersistentDataType.LONG, currentTime);
               container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
            case 2:
         }

         if (this.config.getBoolean("enchantments.enabled", true)) {
            for(String enchantEntry : this.config.getStringList("enchantments.list")) {
               try {
                  String[] parts = enchantEntry.split(":");
                  if (parts.length == 2) {
                     Enchantment enchant = Enchantment.getByName(parts[0].trim());
                     int level = Integer.parseInt(parts[1].trim());
                     if (enchant != null && enchant.canEnchantItem(rocket)) {
                        meta.addEnchant(enchant, level, true);
                     }
                  }
               } catch (Exception var13) {
               }
            }
         }

         meta.setCustomModelData(this.customModelData);
         this.updateLore(meta);
         rocket.setItemMeta(meta);
      }

      return rocket;
   }

   public ItemStack getItemForPlayer(Player player) {
      ItemStack freshRocket = this.item.clone();
      ItemMeta meta = freshRocket.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         if (this.durabilityMode == Rocket.DurabilityMode.TIME) {
            long currentTime = System.currentTimeMillis();
            container.set(this.createdKey, PersistentDataType.LONG, currentTime);
            container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
         }

         this.updateLore(meta, player);
         freshRocket.setItemMeta(meta);
      }

      return freshRocket;
   }

   public ItemStack getItemWithCustomDurability(Player player, String mode, int customUses, long customTime) {
      ItemStack freshRocket = this.item.clone();
      ItemMeta meta = freshRocket.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         DurabilityMode newMode;
         switch (mode.toLowerCase()) {
            case "uses":
               newMode = Rocket.DurabilityMode.USES;
               container.set(this.usesKey, PersistentDataType.INTEGER, customUses);
               break;
            case "time":
               newMode = Rocket.DurabilityMode.TIME;
               long currentTime = System.currentTimeMillis();
               container.set(this.createdKey, PersistentDataType.LONG, currentTime);
               container.set(this.expiresKey, PersistentDataType.LONG, currentTime + customTime);
               break;
            case "infinite":
            default:
               newMode = Rocket.DurabilityMode.INFINITE;
         }

         container.set(this.modeKey, PersistentDataType.STRING, newMode.name());
         this.updateLore(meta, player);
         freshRocket.setItemMeta(meta);
      }

      return freshRocket;
   }

   public void updateLore(ItemMeta meta) {
      this.updateLore(meta, (Player)null);
   }

   public void updateLore(ItemMeta meta, Player player) {
      List<String> loreList = this.config.getStringList("lore");
      if (loreList.isEmpty()) {
         loreList.add("&7Boosts your Elytra flight!");
         loreList.add("&8Durability: &f%durability%");
      }

      ArrayList<String> translatedLore = new ArrayList();
      String durabilityText = this.getDurabilityText(meta);
      String playerName = this.getPlayerNameFromMeta(meta);
      if (playerName == null && player != null) {
         playerName = player.getName();
      }

      if (playerName == null) {
         playerName = "Console";
      }

      for(String line : loreList) {
         String translatedLine = HexColorCode.translateAllColorCodes(line.replace("%durability%", durabilityText).replace("%duration%", String.valueOf(this.flightDuration)).replace("{player}", playerName));
         translatedLore.add(translatedLine);
      }

      meta.setLore(translatedLore);
   }

   private String getPlayerNameFromMeta(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      String uuidStr = (String)container.get(this.ownerKey, PersistentDataType.STRING);
      if (uuidStr != null) {
         try {
            UUID uuid = UUID.fromString(uuidStr);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
               return player.getName();
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getName() != null) {
               return offlinePlayer.getName();
            }
         } catch (IllegalArgumentException var7) {
            return null;
         }
      }

      return null;
   }

   private String getDurabilityText(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      String modeStr = (String)container.get(this.modeKey, PersistentDataType.STRING);
      DurabilityMode currentMode = modeStr != null ? Rocket.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
      switch (currentMode.ordinal()) {
         case 0:
            Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
            if (uses == null) {
               uses = this.maxUses;
            }

            return "" + uses;
         case 1:
            Long expiresAt = (Long)container.get(this.expiresKey, PersistentDataType.LONG);
            if (expiresAt == null) {
               return "0m";
            } else {
               long remaining = expiresAt - System.currentTimeMillis();
               if (remaining <= 0L) {
                  return "0m";
               }

               return this.formatTime(remaining);
            }
         case 2:
            return "∞";
         default:
            return "";
      }
   }

   private String formatTime(long millis) {
      long days = millis / 86400000L;
      long hours = millis % 86400000L / 3600000L;
      long minutes = millis % 3600000L / 60000L;
      if (days > 0L) {
         return days + "d " + hours + "h";
      } else {
         return hours > 0L ? hours + "h " + minutes + "m" : minutes + "m";
      }
   }

   public boolean isExpired(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      String modeStr = (String)container.get(this.modeKey, PersistentDataType.STRING);
      DurabilityMode currentMode = modeStr != null ? Rocket.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
      switch (currentMode.ordinal()) {
         case 0:
            Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
            return uses != null && uses <= 0;
         case 1:
            Long expiresAt = (Long)container.get(this.expiresKey, PersistentDataType.LONG);
            return expiresAt != null && System.currentTimeMillis() > expiresAt;
         default:
            return false;
      }
   }

   public int getRemainingUses(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
      return uses != null ? uses : 0;
   }

   public boolean decreaseUses(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
      if (uses != null && uses > 0) {
         --uses;
         container.set(this.usesKey, PersistentDataType.INTEGER, uses);
         this.updateLore(meta);
         return true;
      } else {
         return false;
      }
   }

   public void handleExpiredTool(Player player, ItemStack item) {
      player.getInventory().remove(item);
      player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
      player.sendMessage(this.plugin.getLang("tool-expired", "tool_name", "Rocket"));
   }

   public boolean isRocket(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         return container.has(this.modeKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public boolean hasElytra(Player player) {
      ItemStack chestplate = player.getInventory().getChestplate();
      return chestplate != null && chestplate.getType() == Material.ELYTRA;
   }

   public void useRocket(Player player, ItemStack rocketItem, EquipmentSlot hand) {
      ItemMeta meta = rocketItem.getItemMeta();
      if (meta != null) {
         boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
         if (this.config.getBoolean("sound.enabled", true)) {
            player.getWorld().playSound(player.getLocation(), this.soundType, this.soundVolume, this.soundPitch);
         }

         if (this.config.getBoolean("particle.enabled", true)) {
            Location loc = player.getLocation().add((double)0.0F, (double)1.0F, (double)0.0F);
            player.getWorld().spawnParticle(this.mainParticle, loc, this.mainParticleCount, (double)0.5F, (double)0.5F, (double)0.5F, 0.1);
            if (this.config.getBoolean("particle.additional-particle.enabled", true)) {
               player.getWorld().spawnParticle(this.additionalParticle, loc, this.additionalParticleCount, 0.3, 0.3, 0.3, 0.05);
            }
         }

         Vector direction = player.getLocation().getDirection();
         double boostPower = this.config.getDouble("rocket.boost-power", (double)1.5F);
         Vector newVelocity = direction.multiply(boostPower);
         player.setVelocity(newVelocity);
         if (!isCreative) {
            if (this.durabilityMode == Rocket.DurabilityMode.USES) {
               if (!this.decreaseUses(meta)) {
                  this.handleExpiredTool(player, rocketItem);
                  return;
               }

               rocketItem.setItemMeta(meta);
               if (hand == EquipmentSlot.OFF_HAND) {
                  player.getInventory().setItemInOffHand(rocketItem);
               }
            } else if (this.durabilityMode == Rocket.DurabilityMode.TIME) {
               this.updateLore(meta);
               rocketItem.setItemMeta(meta);
               if (hand == EquipmentSlot.OFF_HAND) {
                  player.getInventory().setItemInOffHand(rocketItem);
               }
            }
         }

      }
   }

   public void useRocket(Player player, ItemStack rocketItem) {
      this.useRocket(player, rocketItem, EquipmentSlot.HAND);
   }

   public NamespacedKey getUsesKey() {
      return this.usesKey;
   }

   public NamespacedKey getCreatedKey() {
      return this.createdKey;
   }

   public NamespacedKey getExpiresKey() {
      return this.expiresKey;
   }

   public NamespacedKey getModeKey() {
      return this.modeKey;
   }

   public NamespacedKey getOwnerKey() {
      return this.ownerKey;
   }

   public int getMaxUses() {
      return this.maxUses;
   }

   public DurabilityMode getDurabilityMode() {
      return this.durabilityMode;
   }

   public int getFlightDuration() {
      return this.flightDuration;
   }

   public FileConfiguration getConfig() {
      return this.config;
   }

   public Sound getSoundType() {
      return this.soundType;
   }

   public float getSoundVolume() {
      return this.soundVolume;
   }

   public float getSoundPitch() {
      return this.soundPitch;
   }

   public Particle getMainParticle() {
      return this.mainParticle;
   }

   public int getMainParticleCount() {
      return this.mainParticleCount;
   }

   public Particle getAdditionalParticle() {
      return this.additionalParticle;
   }

   public int getAdditionalParticleCount() {
      return this.additionalParticleCount;
   }

   public ItemStack getItem() {
      ItemStack freshRocket = this.item.clone();
      ItemMeta meta = freshRocket.getItemMeta();
      if (meta != null && this.durabilityMode == Rocket.DurabilityMode.TIME) {
         long currentTime = System.currentTimeMillis();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.createdKey, PersistentDataType.LONG, currentTime);
         container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
         this.updateLore(meta);
         freshRocket.setItemMeta(meta);
      }

      return freshRocket;
   }

   public static enum DurabilityMode {
      USES("uses"),
      TIME("time"),
      INFINITE("infinite");

      private final String configName;

      private DurabilityMode(String configName) {
         this.configName = configName;
      }

      public static DurabilityMode fromString(String name) {
         for(DurabilityMode mode : values()) {
            if (mode.configName.equalsIgnoreCase(name)) {
               return mode;
            }
         }

         return USES;
      }

      // $FF: synthetic method
      private static DurabilityMode[] $values() {
         return new DurabilityMode[]{USES, TIME, INFINITE};
      }
   }
}
