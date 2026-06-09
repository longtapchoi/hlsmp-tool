package de.elivb.donutTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

public class LavaBucket {
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
   private final DurabilityMode durabilityMode;
   private final int maxUses;
   private final String timeDuration;
   private final long durationMillis;

   public LavaBucket(Tools plugin) {
      this.plugin = plugin;
      String var10002 = String.valueOf(plugin.getDataFolder());
      File configFile = new File(var10002 + "/DonutTools", "lavabucket.yml");
      this.config = YamlConfiguration.loadConfiguration(configFile);
      this.usesKey = new NamespacedKey(plugin, "lavabucket_uses");
      this.createdKey = new NamespacedKey(plugin, "lavabucket_created");
      this.expiresKey = new NamespacedKey(plugin, "lavabucket_expires");
      this.modeKey = new NamespacedKey(plugin, "lavabucket_mode");
      this.ownerKey = new NamespacedKey(plugin, "lavabucket_owner");
      String materialName = this.config.getString("material", "LAVA_BUCKET");
      Material tempMaterial = Material.matchMaterial(materialName);
      this.material = tempMaterial == null ? Material.LAVA_BUCKET : tempMaterial;
      this.customModelData = this.config.getInt("custom-model-data", 1003);
      String mode = this.config.getString("durability.mode", "infinite").toLowerCase();
      this.durabilityMode = LavaBucket.DurabilityMode.fromString(mode);
      this.maxUses = this.config.getInt("durability.uses", 100);
      this.timeDuration = this.config.getString("durability.time", "3d");
      this.durationMillis = this.parseDuration(this.timeDuration);
      String soundName = this.config.getString("sound.type", "ENTITY_GENERIC_EXTINGUISH_FIRE");
      this.soundType = this.getSoundByName(soundName);
      this.soundVolume = (float)this.config.getDouble("sound.volume", (double)1.0F);
      this.soundPitch = (float)this.config.getDouble("sound.pitch", (double)1.0F);
      String mainParticleName = this.config.getString("particle.type", "FLAME");
      this.mainParticle = this.getParticleByName(mainParticleName);
      this.mainParticleCount = this.config.getInt("particle.count", 10);
      String additionalParticleName = this.config.getString("particle.additional-particle.type", "LAVA");
      this.additionalParticle = this.getParticleByName(additionalParticleName);
      this.additionalParticleCount = this.config.getInt("particle.additional-particle.count", 5);
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
         try {
            NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase().replace("_", "."));
            Sound sound = (Sound)Registry.SOUNDS.get(key);
            if (sound != null) {
               return sound;
            }
         } catch (Exception var4) {
         }

         switch (name.toUpperCase()) {
            case "ENTITY_GENERIC_EXTINGUISH_FIRE" -> {
               return Sound.ENTITY_GENERIC_EXTINGUISH_FIRE;
            }
            case "BLOCK_FIRE_AMBIENT" -> {
               return Sound.BLOCK_FIRE_AMBIENT;
            }
            case "BLOCK_LAVA_AMBIENT" -> {
               return Sound.BLOCK_LAVA_AMBIENT;
            }
            default -> {
               return Sound.ENTITY_GENERIC_EXTINGUISH_FIRE;
            }
         }
      } else {
         return Sound.ENTITY_GENERIC_EXTINGUISH_FIRE;
      }
   }

   private Particle getParticleByName(String name) {
      if (name != null && !name.isEmpty()) {
         try {
            NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase().replace("_", "."));
            Particle particle = (Particle)Registry.PARTICLE_TYPE.get(key);
            if (particle != null) {
               return particle;
            }
         } catch (Exception var5) {
         }

         try {
            return Particle.valueOf(name.toUpperCase());
         } catch (IllegalArgumentException var4) {
            return Particle.valueOf("FLAME");
         }
      } else {
         return Particle.valueOf("FLAME");
      }
   }

   private ItemStack createItem() {
      ItemStack bucket = new ItemStack(this.material);
      ItemMeta meta = bucket.getItemMeta();
      if (meta != null) {
         String displayName = this.config.getString("name", "&#A20AD6ᴀᴍᴇᴛʜʏꜱᴛ ʟᴀᴠᴀ ʙᴜᴄᴋᴇᴛ");
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
                     if (enchant != null && enchant.canEnchantItem(bucket)) {
                        meta.addEnchant(enchant, level, true);
                     }
                  }
               } catch (Exception var13) {
               }
            }
         }

         meta.setCustomModelData(this.customModelData);
         this.updateLore(meta);
         bucket.setItemMeta(meta);
      }

      return bucket;
   }

   public ItemStack getItemForPlayer(Player player) {
      ItemStack freshBucket = this.item.clone();
      ItemMeta meta = freshBucket.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         if (this.durabilityMode == LavaBucket.DurabilityMode.TIME) {
            long currentTime = System.currentTimeMillis();
            container.set(this.createdKey, PersistentDataType.LONG, currentTime);
            container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
         }

         this.updateLore(meta, player);
         freshBucket.setItemMeta(meta);
      }

      return freshBucket;
   }

   public ItemStack getItemWithCustomDurability(Player player, String mode, int customUses, long customTime) {
      ItemStack freshBucket = this.item.clone();
      ItemMeta meta = freshBucket.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         DurabilityMode newMode;
         switch (mode.toLowerCase()) {
            case "uses":
               newMode = LavaBucket.DurabilityMode.USES;
               container.set(this.usesKey, PersistentDataType.INTEGER, customUses);
               break;
            case "time":
               newMode = LavaBucket.DurabilityMode.TIME;
               long currentTime = System.currentTimeMillis();
               container.set(this.createdKey, PersistentDataType.LONG, currentTime);
               container.set(this.expiresKey, PersistentDataType.LONG, currentTime + customTime);
               break;
            case "infinite":
            default:
               newMode = LavaBucket.DurabilityMode.INFINITE;
         }

         container.set(this.modeKey, PersistentDataType.STRING, newMode.name());
         this.updateLore(meta, player);
         freshBucket.setItemMeta(meta);
      }

      return freshBucket;
   }

   public void updateLore(ItemMeta meta) {
      this.updateLore(meta, (Player)null);
   }

   public void updateLore(ItemMeta meta, Player player) {
      List<String> loreList = this.config.getStringList("lore");
      if (loreList.isEmpty()) {
         loreList.add("&7Infinite Lava Bucket");
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
      DurabilityMode currentMode = modeStr != null ? LavaBucket.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
      switch (currentMode.ordinal()) {
         case 0:
            Integer uses = (Integer)container.get(this.usesKey, PersistentDataType.INTEGER);
            int var10000 = uses == null ? this.maxUses : uses;
            return "" + var10000;
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
      DurabilityMode currentMode = modeStr != null ? LavaBucket.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
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
      player.sendMessage(this.plugin.getLang("tool-expired", "tool_name", "Lava Bucket"));
   }

   public boolean isLavaBucket(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         return container.has(this.modeKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public void useBucket(Player player, ItemStack bucketItem) {
      ItemMeta meta = bucketItem.getItemMeta();
      if (meta != null) {
         boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
         PersistentDataContainer container = meta.getPersistentDataContainer();
         DurabilityMode currentMode = null;
         if (container.has(this.modeKey, PersistentDataType.STRING)) {
            String modeStr = (String)container.get(this.modeKey, PersistentDataType.STRING);

            try {
               currentMode = LavaBucket.DurabilityMode.valueOf(modeStr);
            } catch (IllegalArgumentException var12) {
               currentMode = this.durabilityMode;
            }
         } else {
            currentMode = this.durabilityMode;
         }

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

         if (currentMode == LavaBucket.DurabilityMode.USES) {
            int remainingUses = this.getRemainingUses(meta);
            if (remainingUses <= 0) {
               this.handleExpiredTool(player, bucketItem);
               return;
            }
         }

         RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), (double)5.0F, FluidCollisionMode.NEVER, true);
         boolean used = false;
         if (rayTrace != null && rayTrace.getHitBlock() != null) {
            Block hitBlock = rayTrace.getHitBlock();
            BlockFace hitFace = rayTrace.getHitBlockFace();
            if (hitBlock.getType() == Material.LAVA) {
               hitBlock.setType(Material.AIR);
               player.getWorld().playSound(hitBlock.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
               used = true;
            } else if (hitFace != null) {
               Block placeBlock = hitBlock.getRelative(hitFace);
               if (placeBlock.getType() == Material.AIR) {
                  placeBlock.setType(Material.LAVA);
                  player.getWorld().playSound(placeBlock.getLocation(), Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F);
                  used = true;
               }
            }
         }

         if (used && !isCreative) {
            if (currentMode == LavaBucket.DurabilityMode.USES) {
               this.getRemainingUses(meta);
               if (!this.decreaseUses(meta)) {
                  this.handleExpiredTool(player, bucketItem);
                  return;
               }

               bucketItem.setItemMeta(meta);
               this.getRemainingUses(meta);
            } else if (currentMode == LavaBucket.DurabilityMode.TIME) {
               this.updateLore(meta);
               bucketItem.setItemMeta(meta);
            }
         }

      }
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
      ItemStack freshBucket = this.item.clone();
      ItemMeta meta = freshBucket.getItemMeta();
      if (meta != null && this.durabilityMode == LavaBucket.DurabilityMode.TIME) {
         long currentTime = System.currentTimeMillis();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.createdKey, PersistentDataType.LONG, currentTime);
         container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
         this.updateLore(meta);
         freshBucket.setItemMeta(meta);
      }

      return freshBucket;
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
