package de.elivb.donutTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Shovel {
   private final Tools plugin;
   private final FileConfiguration config;
   private final ItemStack item;
   private final NamespacedKey usesKey;
   private final NamespacedKey createdKey;
   private final NamespacedKey expiresKey;
   private final NamespacedKey modeKey;
   private final NamespacedKey disabledKey;
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
   private final DurabilityMode durabilityMode;
   private final int maxUses;
   private final String timeDuration;
   private final long durationMillis;
   private final Material convertToMaterial;

   public Shovel(Tools plugin) {
      this.plugin = plugin;
      String var10002 = String.valueOf(plugin.getDataFolder());
      File configFile = new File(var10002 + "/DonutTools", "shovel.yml");
      this.config = YamlConfiguration.loadConfiguration(configFile);
      this.usesKey = new NamespacedKey(plugin, "shovel_uses");
      this.createdKey = new NamespacedKey(plugin, "shovel_created");
      this.expiresKey = new NamespacedKey(plugin, "shovel_expires");
      this.modeKey = new NamespacedKey(plugin, "shovel_mode");
      this.disabledKey = new NamespacedKey(plugin, "shovel_disabled");
      this.ownerKey = new NamespacedKey(plugin, "shovel_owner");
      String materialName = this.config.getString("material", "NETHERITE_SHOVEL");
      Material tempMaterial = Material.matchMaterial(materialName);
      this.material = tempMaterial == null ? Material.NETHERITE_SHOVEL : tempMaterial;
      this.customModelData = this.config.getInt("custom-model-data", 1000);
      String mode = this.config.getString("durability.mode", "time").toLowerCase();
      this.durabilityMode = Shovel.DurabilityMode.fromString(mode);
      this.maxUses = this.config.getInt("durability.uses", 100);
      this.timeDuration = this.config.getString("durability.time", "3d");
      this.durationMillis = this.parseDuration(this.timeDuration);
      String convertTo = this.config.getString("durability.convert-to", "NETHERITE_SHOVEL");
      this.convertToMaterial = Material.matchMaterial(convertTo) != null ? Material.matchMaterial(convertTo) : Material.NETHERITE_SHOVEL;
      String soundName = this.config.getString("sound.type", "BLOCK_GRASS_BREAK");
      this.soundType = this.getSoundByName(soundName);
      this.soundVolume = (float)this.config.getDouble("sound.volume", 0.3);
      this.soundPitch = (float)this.config.getDouble("sound.pitch", 1.1);
      String mainParticleName = this.config.getString("particle.type", "CLOUD");
      this.mainParticle = this.getParticleByName(mainParticleName);
      this.mainParticleCount = this.config.getInt("particle.count", 25);
      String additionalParticleName = this.config.getString("particle.additional-particle.type", "CRIT");
      this.additionalParticle = this.getParticleByName(additionalParticleName);
      this.additionalParticleCount = this.config.getInt("particle.additional-particle.count", 15);
      this.item = this.createItem();
   }

   private long parseDuration(String duration) {
      if (duration != null && !duration.isEmpty()) {
         duration = duration.toLowerCase().trim();

         try {
            if (duration.endsWith("d")) {
               long days = Long.parseLong(duration.substring(0, duration.length() - 1));
               return days * 24L * 60L * 60L * 1000L;
            } else if (duration.endsWith("h")) {
               long hours = Long.parseLong(duration.substring(0, duration.length() - 1));
               return hours * 60L * 60L * 1000L;
            } else if (duration.endsWith("m")) {
               long minutes = Long.parseLong(duration.substring(0, duration.length() - 1));
               return minutes * 60L * 1000L;
            } else if (duration.endsWith("w")) {
               long weeks = Long.parseLong(duration.substring(0, duration.length() - 1));
               return weeks * 7L * 24L * 60L * 60L * 1000L;
            } else {
               return Long.parseLong(duration) * 60L * 1000L;
            }
         } catch (NumberFormatException var4) {
            return 259200000L;
         }
      } else {
         return 259200000L;
      }
   }

   private Sound getSoundByName(String name) {
      switch (name.toUpperCase()) {
         case "BLOCK_GRASS_BREAK" -> {
            return Sound.BLOCK_GRASS_BREAK;
         }
         case "BLOCK_GRAVEL_BREAK" -> {
            return Sound.BLOCK_GRAVEL_BREAK;
         }
         case "BLOCK_SAND_BREAK" -> {
            return Sound.BLOCK_SAND_BREAK;
         }
         case "BLOCK_SNOW_BREAK" -> {
            return Sound.BLOCK_SNOW_BREAK;
         }
         default -> {
            return Sound.BLOCK_GRASS_BREAK;
         }
      }
   }

   private Particle getParticleByName(String name) {
      switch (name.toUpperCase()) {
         case "CLOUD" -> {
            return Particle.CLOUD;
         }
         case "CRIT" -> {
            return Particle.CRIT;
         }
         case "SMOKE" -> {
            return Particle.SMOKE;
         }
         case "FALLING_DUST" -> {
            return Particle.FALLING_DUST;
         }
         default -> {
            return Particle.CLOUD;
         }
      }
   }

   private ItemStack createItem() {
      ItemStack shovel = new ItemStack(this.material);
      ItemMeta meta = shovel.getItemMeta();
      if (meta != null) {
         String displayName = this.config.getString("name", "&#A20AD6ᴀᴍᴇᴛʜʏꜱᴛ ꜱʜᴏᴠᴇʟ");
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
            if (this.config.contains("enchantments.list") && this.config.isConfigurationSection("enchantments.list")) {
               ConfigurationSection enchantSection = this.config.getConfigurationSection("enchantments.list");
               if (enchantSection != null) {
                  for(String enchantName : enchantSection.getKeys(false)) {
                     try {
                        Enchantment enchant = Enchantment.getByName(enchantName);
                        if (enchant != null) {
                           int level = enchantSection.getInt(enchantName, 1);
                           if (enchant.canEnchantItem(shovel)) {
                              meta.addEnchant(enchant, level, true);
                           }
                        }
                     } catch (Exception var15) {
                     }
                  }
               }
            } else if (this.config.contains("enchantments.list") && this.config.isList("enchantments.list")) {
               for(String enchantEntry : this.config.getStringList("enchantments.list")) {
                  try {
                     String[] parts = enchantEntry.split(":");
                     if (parts.length == 2) {
                        String enchantName = parts[0].trim();
                        int level = Integer.parseInt(parts[1].trim());
                        Enchantment enchant = Enchantment.getByName(enchantName);
                        if (enchant != null && enchant.canEnchantItem(shovel)) {
                           meta.addEnchant(enchant, level, true);
                        }
                     }
                  } catch (Exception var14) {
                  }
               }
            }
         }

         meta.setCustomModelData(this.customModelData);
         this.updateLore(meta);
         shovel.setItemMeta(meta);
      }

      return shovel;
   }

   public ItemStack getItemForPlayer(Player player) {
      ItemStack freshShovel = this.item.clone();
      ItemMeta meta = freshShovel.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         if (this.durabilityMode == Shovel.DurabilityMode.TIME) {
            long currentTime = System.currentTimeMillis();
            container.set(this.createdKey, PersistentDataType.LONG, currentTime);
            container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
         }

         this.updateLore(meta, player);
         freshShovel.setItemMeta(meta);
      }

      return freshShovel;
   }

   public ItemStack getItemWithCustomDurability(Player player, String mode, int customUses, long customTime) {
      ItemStack freshShovel = this.item.clone();
      ItemMeta meta = freshShovel.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         DurabilityMode newMode;
         switch (mode.toLowerCase()) {
            case "uses":
               newMode = Shovel.DurabilityMode.USES;
               container.set(this.usesKey, PersistentDataType.INTEGER, customUses);
               break;
            case "time":
               newMode = Shovel.DurabilityMode.TIME;
               long currentTime = System.currentTimeMillis();
               container.set(this.createdKey, PersistentDataType.LONG, currentTime);
               container.set(this.expiresKey, PersistentDataType.LONG, currentTime + customTime);
               break;
            case "infinite":
            default:
               newMode = Shovel.DurabilityMode.INFINITE;
         }

         container.set(this.modeKey, PersistentDataType.STRING, newMode.name());
         this.updateLore(meta, player);
         freshShovel.setItemMeta(meta);
      }

      return freshShovel;
   }

   public void updateLore(ItemMeta meta) {
      this.updateLore(meta, (Player)null);
   }

   public void updateLore(ItemMeta meta, Player player) {
      List<String> loreList = this.config.getStringList("lore");
      if (loreList.isEmpty()) {
         String loreText = this.config.getString("lore", "");
         if (!loreText.isEmpty()) {
            loreList.add(loreText);
         } else {
            loreList.add("&7Breaks 9 Blocks at Once");
            loreList.add("&8Self Destruct:");
            loreList.add("&8%durability%");
         }
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
         String translatedLine = HexColorCode.translateAllColorCodes(line.replace("%durability%", durabilityText).replace("{player}", playerName));
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
      DurabilityMode currentMode = modeStr != null ? Shovel.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
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
               long currentTime = System.currentTimeMillis();
               container.set(this.createdKey, PersistentDataType.LONG, currentTime);
               container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
               expiresAt = currentTime + this.durationMillis;
            }

            long remaining = expiresAt - System.currentTimeMillis();
            if (remaining <= 0L) {
               return "0m";
            }

            return this.formatTime(remaining);
         case 2:
            return "§f∞";
         default:
            return "";
      }
   }

   private String formatTime(long millis) {
      if (millis <= 0L) {
         return "0m";
      } else {
         long days = millis / 86400000L;
         long hours = millis % 86400000L / 3600000L;
         long minutes = millis % 3600000L / 60000L;
         if (days > 0L) {
            return days + "d " + hours + "h " + minutes + "m";
         } else {
            return hours > 0L ? hours + "h " + minutes + "m" : minutes + "m";
         }
      }
   }

   public boolean isExpired(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      String modeStr = (String)container.get(this.modeKey, PersistentDataType.STRING);
      DurabilityMode currentMode = modeStr != null ? Shovel.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
      switch (currentMode.ordinal()) {
         case 0:
            Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
            return uses != null && uses <= 0;
         case 1:
            Long expiresAt = (Long)container.get(this.expiresKey, PersistentDataType.LONG);
            if (expiresAt == null) {
               return false;
            }

            return System.currentTimeMillis() > expiresAt;
         case 2:
            return false;
         default:
            return false;
      }
   }

   public boolean hasUsesLeft(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
      if (uses == null) {
         return true;
      } else {
         return uses > 0;
      }
   }

   public int getRemainingUses(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
      return uses != null ? uses : 0;
   }

   public long getRemainingTime(ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      Long expiresAt = (Long)container.get(this.expiresKey, PersistentDataType.LONG);
      if (expiresAt == null) {
         return 0L;
      } else {
         long remaining = expiresAt - System.currentTimeMillis();
         return Math.max(0L, remaining);
      }
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
      player.sendMessage(this.plugin.getLang("tool-expired", "tool_name", "ᴀᴍᴇᴛʜʏꜱᴛ ꜱʜᴏᴠᴇʟ"));
   }

   public boolean isDisabled(ItemMeta meta) {
      return false;
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

   public NamespacedKey getDisabledKey() {
      return this.disabledKey;
   }

   public NamespacedKey getOwnerKey() {
      return this.ownerKey;
   }

   public long getDurationMillis() {
      return this.durationMillis;
   }

   public int getMaxUses() {
      return this.maxUses;
   }

   public DurabilityMode getDurabilityMode() {
      return this.durabilityMode;
   }

   public String getTimeDuration() {
      return this.timeDuration;
   }

   public Material getConvertToMaterial() {
      return this.convertToMaterial;
   }

   public int getCustomModelData() {
      return this.customModelData;
   }

   public FileConfiguration getConfig() {
      return this.config;
   }

   public Material getMaterial() {
      return this.material;
   }

   public int mine3x3(Player player, Block center) {
      if (center == null) {
         return 0;
      } else {
         int blocksBroken = 0;
         ItemStack shovelItem = player.getInventory().getItemInMainHand();
         WorldGuardService worldGuardService = null;

         try {
            worldGuardService = new WorldGuardService(this.plugin);
         } catch (Throwable var10) {
         }

         for(int x = -1; x <= 1; ++x) {
            for(int y = -1; y <= 1; ++y) {
               for(int z = -1; z <= 1; ++z) {
                  Block block;
                  if ((x != 0 || y != 0 || z != 0) && (block = center.getRelative(x, y, z)) != null && block.getType() != Material.AIR && !(block.getType().getHardness() < 0.0F) && this.isShovelable(block.getType()) && (worldGuardService == null || worldGuardService.canBuild(player, block.getLocation()))) {
                     block.breakNaturally(shovelItem);
                     ++blocksBroken;
                  }
               }
            }
         }

         return blocksBroken;
      }
   }

   public boolean isShovelable(Material material) {
      switch (material) {
         case GRASS_BLOCK:
         case DIRT:
         case COARSE_DIRT:
         case PODZOL:
         case ROOTED_DIRT:
         case MYCELIUM:
         case SAND:
         case RED_SAND:
         case GRAVEL:
         case CLAY:
         case SNOW_BLOCK:
         case SOUL_SAND:
         case SOUL_SOIL:
         case FARMLAND:
         case DIRT_PATH:
         case MOSS_BLOCK:
            return true;
         default:
            return false;
      }
   }

   public void playEffects(Player player, Block block) {
      Location location = block.getLocation().add((double)0.5F, (double)0.5F, (double)0.5F);
      if (this.config.getBoolean("sound.enabled", true)) {
         player.getWorld().playSound(location, this.soundType, this.soundVolume, this.soundPitch);
      }

      if (this.config.getBoolean("particle.enabled", true)) {
         player.getWorld().spawnParticle(this.mainParticle, location, this.mainParticleCount);
         if (this.config.getBoolean("particle.additional-particle.enabled", true)) {
            player.getWorld().spawnParticle(this.additionalParticle, location, this.additionalParticleCount);
         }
      }

   }

   public boolean isShovel(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         return container.has(this.modeKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public ItemStack getItem() {
      ItemStack freshShovel = this.item.clone();
      ItemMeta meta = freshShovel.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (this.durabilityMode == Shovel.DurabilityMode.TIME) {
            long currentTime = System.currentTimeMillis();
            container.set(this.createdKey, PersistentDataType.LONG, currentTime);
            container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
            this.updateLore(meta);
            freshShovel.setItemMeta(meta);
         }
      }

      return freshShovel;
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

      public String getConfigName() {
         return this.configName;
      }

      // $FF: synthetic method
      private static DurabilityMode[] $values() {
         return new DurabilityMode[]{USES, TIME, INFINITE};
      }
   }
}
