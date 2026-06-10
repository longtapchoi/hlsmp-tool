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
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Drill {
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

   public Drill(Tools plugin) {
      this.plugin = plugin;
      File configFile = new File(String.valueOf(plugin.getDataFolder()) + "/DonutTools", "drill.yml");
      this.config = YamlConfiguration.loadConfiguration(configFile);
      this.usesKey = new NamespacedKey(plugin, "drill_uses");
      this.createdKey = new NamespacedKey(plugin, "drill_created");
      this.expiresKey = new NamespacedKey(plugin, "drill_expires");
      this.modeKey = new NamespacedKey(plugin, "drill_mode");
      this.disabledKey = new NamespacedKey(plugin, "drill_disabled");
      this.ownerKey = new NamespacedKey(plugin, "drill_owner");
      String materialName = this.config.getString("material", "NETHERITE_PICKAXE");
      Material tempMaterial = Material.matchMaterial(materialName);
      if (tempMaterial == null) {
         this.material = Material.NETHERITE_PICKAXE;
      } else {
         this.material = tempMaterial;
      }

      this.customModelData = this.config.getInt("custom-model-data", 1000);
      String mode = this.config.getString("durability.mode", "time").toLowerCase();
      this.durabilityMode = Drill.DurabilityMode.fromString(mode);
      this.maxUses = this.config.getInt("durability.uses", 100);
      this.timeDuration = this.config.getString("durability.time", "7d");
      this.durationMillis = this.parseDuration(this.timeDuration);
      String convertTo = this.config.getString("durability.convert-to", "NETHERITE_PICKAXE");
      this.convertToMaterial = Material.matchMaterial(convertTo) != null ? Material.matchMaterial(convertTo) : Material.NETHERITE_PICKAXE;
      String soundName = this.config.getString("sound.type", "BLOCK_AMETHYST_BLOCK_STEP");
      this.soundType = this.getSoundByName(soundName);
      this.soundVolume = (float)this.config.getDouble("sound.volume", 0.1);
      this.soundPitch = (float)this.config.getDouble("sound.pitch", (double)1.0F);
      String mainParticleName = this.config.getString("particle.type", "PORTAL");
      this.mainParticle = this.getParticleByName(mainParticleName);
      this.mainParticleCount = this.config.getInt("particle.count", 3);
      String additionalParticleName = this.config.getString("particle.additional-particle.type", "DRAGON_BREATH");
      this.additionalParticle = this.getParticleByName(additionalParticleName);
      this.additionalParticleCount = this.config.getInt("particle.additional-particle.count", 0);
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
      try {
         switch (name.toUpperCase()) {
            case "BLOCK_AMETHYST_BLOCK_STEP":
               return Sound.BLOCK_AMETHYST_BLOCK_STEP;
            case "BLOCK_STONE_STEP":
            case "BLOCK_STONE_FALL":
               return Sound.BLOCK_STONE_FALL;
            case "ENTITY_PLAYER_LEVELUP":
               return Sound.ENTITY_PLAYER_LEVELUP;
            case "BLOCK_ANVIL_USE":
               return Sound.BLOCK_ANVIL_USE;
            case "ENTITY_EXPERIENCE_ORB_PICKUP":
               return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            default:
               return Sound.BLOCK_STONE_FALL;
         }
      } catch (Exception var4) {
         return Sound.BLOCK_STONE_FALL;
      }
   }

   private Particle getParticleByName(String name) {
      try {
         switch (name.toUpperCase()) {
            case "PORTAL" -> {
               return Particle.PORTAL;
            }
            case "DRAGON_BREATH" -> {
               return Particle.DRAGON_BREATH;
            }
            case "CLOUD" -> {
               return Particle.CLOUD;
            }
            case "FLAME" -> {
               return Particle.FLAME;
            }
            case "CRIT" -> {
               return Particle.CRIT;
            }
            case "SOUL_FIRE_FLAME" -> {
               return Particle.SOUL_FIRE_FLAME;
            }
            case "ELECTRIC_SPARK" -> {
               return Particle.ELECTRIC_SPARK;
            }
            case "GLOW" -> {
               return Particle.GLOW;
            }
            default -> {
               return Particle.PORTAL;
            }
         }
      } catch (Exception var4) {
         return Particle.PORTAL;
      }
   }

   private ItemStack createItem() {
      ItemStack drill = new ItemStack(this.material);
      ItemMeta meta = drill.getItemMeta();
      if (meta != null) {
         String displayName = this.config.getString("name", "&#A20AD6ᴀᴍᴇᴛʜʏꜱᴛ ᴘɪᴄᴋᴀxᴇ");
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
                     String enchantName = parts[0].trim();
                     int level = Integer.parseInt(parts[1].trim());
                     Enchantment enchant = Enchantment.getByName(enchantName);
                     if (enchant != null && enchant.canEnchantItem(drill)) {
                        meta.addEnchant(enchant, level, true);
                     }
                  }
               } catch (Exception var14) {
               }
            }
         }

         meta.setCustomModelData(this.customModelData);
         this.updateLore(meta);
         drill.setItemMeta(meta);
      }

      return drill;
   }

   public ItemStack getItemForPlayer(Player player) {
      ItemStack freshDrill = this.item.clone();
      ItemMeta meta = freshDrill.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         if (this.durabilityMode == Drill.DurabilityMode.TIME) {
            long currentTime = System.currentTimeMillis();
            container.set(this.createdKey, PersistentDataType.LONG, currentTime);
            container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
         }

         this.updateLore(meta, player);
         freshDrill.setItemMeta(meta);
      }

      return freshDrill;
   }

   public ItemStack getItemWithCustomDurability(Player player, String mode, int customUses, long customTime) {
      ItemStack freshDrill = this.item.clone();
      ItemMeta meta = freshDrill.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (player != null) {
            container.set(this.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
         }

         DurabilityMode newMode;
         switch (mode.toLowerCase()) {
            case "uses":
               newMode = Drill.DurabilityMode.USES;
               container.set(this.usesKey, PersistentDataType.INTEGER, customUses);
               break;
            case "time":
               newMode = Drill.DurabilityMode.TIME;
               long currentTime = System.currentTimeMillis();
               container.set(this.createdKey, PersistentDataType.LONG, currentTime);
               container.set(this.expiresKey, PersistentDataType.LONG, currentTime + customTime);
               break;
            case "infinite":
            default:
               newMode = Drill.DurabilityMode.INFINITE;
         }

         container.set(this.modeKey, PersistentDataType.STRING, newMode.name());
         this.updateLore(meta, player);
         freshDrill.setItemMeta(meta);
      }

      return freshDrill;
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
            loreList.add("&7Phá 3x3 mỗi lần đào");
            loreList.add("&8Hạn sử dụng:");
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
      DurabilityMode currentMode = modeStr != null ? Drill.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
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
      DurabilityMode currentMode = modeStr != null ? Drill.DurabilityMode.valueOf(modeStr) : this.durabilityMode;
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
      player.sendMessage(this.plugin.getLang("tool-expired", "tool_name", "ᴀᴍᴇᴛʜʏꜱᴛ ᴘɪᴄᴋᴀxᴇ"));
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

   public int getCustomModelData() {
      return this.customModelData;
   }

   public FileConfiguration getConfig() {
      return this.config;
   }

   public Material getMaterial() {
      return this.material;
   }

   public String getTimeDuration() {
      return this.timeDuration;
   }

   public Material getConvertToMaterial() {
      return this.convertToMaterial;
   }

   public ItemStack getItem() {
      ItemStack freshDrill = this.item.clone();
      ItemMeta meta = freshDrill.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (this.durabilityMode == Drill.DurabilityMode.TIME) {
            long currentTime = System.currentTimeMillis();
            container.set(this.createdKey, PersistentDataType.LONG, currentTime);
            container.set(this.expiresKey, PersistentDataType.LONG, currentTime + this.durationMillis);
            this.updateLore(meta);
            freshDrill.setItemMeta(meta);
         }
      }

      return freshDrill;
   }

   public int mine3x3(Player player, Block center) {
      if (center == null) {
         return 0;
      } else {
         int blocksBroken = 0;
         ItemStack drillItem = player.getInventory().getItemInMainHand();
         BlockFace face = this.getTargetBlockFace(player);

         for(int x = -1; x <= 1; ++x) {
            for(int y = -1; y <= 1; ++y) {
               for(int z = -1; z <= 1; ++z) {
                  if (x != 0 || y != 0 || z != 0) {
                     Block block = null;

                     try {
                        switch (face) {
                           case NORTH:
                           case SOUTH:
                              block = center.getRelative(x, y, 0);
                              break;
                           case EAST:
                           case WEST:
                              block = center.getRelative(0, y, z);
                              break;
                           case UP:
                           case DOWN:
                              block = center.getRelative(x, 0, z);
                              break;
                           default:
                              block = center.getRelative(x, y, z);
                        }
                     } catch (Exception var12) {
                        continue;
                     }

                     if (block != null && block.getType() != Material.AIR && block.getType() != Material.BEDROCK && block.getType() != Material.BARRIER && !(block.getType().getHardness() < 0.0F)) {
                        try {
                           block.breakNaturally(drillItem);
                           this.playBlockEffects(player, block);
                           ++blocksBroken;
                        } catch (Exception var11) {
                        }
                     }
                  }
               }
            }
         }

         return blocksBroken;
      }
   }

   private BlockFace getTargetBlockFace(Player player) {
      float yaw = player.getLocation().getYaw();
      if (yaw < 0.0F) {
         yaw += 360.0F;
      }

      if (!(yaw >= 315.0F) && !(yaw < 45.0F)) {
         if (yaw >= 45.0F && yaw < 135.0F) {
            return BlockFace.WEST;
         } else {
            return yaw >= 135.0F && yaw < 225.0F ? BlockFace.NORTH : BlockFace.EAST;
         }
      } else {
         return BlockFace.SOUTH;
      }
   }

   public void playBlockEffects(Player player, Block block) {
      Location location = block.getLocation().add((double)0.5F, (double)0.5F, (double)0.5F);
      if (this.config.getBoolean("sound.enabled", true)) {
         player.getWorld().playSound(location, this.soundType, this.soundVolume, this.soundPitch);
      }

      if (this.config.getBoolean("particle.enabled", true)) {
         player.getWorld().spawnParticle(this.mainParticle, location, 3, 0.1, 0.1, 0.1, 0.01);
         if (this.config.getBoolean("particle.additional-particle.enabled", false) && this.additionalParticleCount > 0) {
            player.getWorld().spawnParticle(this.additionalParticle, location, 1, 0.05, 0.05, 0.05, 0.005);
         }
      }

   }

   public void playEffects(Player player, Block block) {
      this.playBlockEffects(player, block);
   }

   public float calculateBreakSpeed(Player player, ItemStack item, Block block) {
      float breakSpeed = 1.0F;
      breakSpeed = this.getToolEfficiency(this.material);
      if (!this.isToolSuitableForBlock(this.material, block.getType())) {
         return breakSpeed / 5.0F;
      } else {
         if (item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.EFFICIENCY)) {
            int efficiencyLevel = item.getItemMeta().getEnchantLevel(Enchantment.EFFICIENCY);
            float efficiencyBonus = efficiencyLevel > 0 ? (float)(efficiencyLevel * efficiencyLevel + 1) : 0.0F;
            breakSpeed += efficiencyBonus;
         }

         PotionEffectType hasteType = this.getHasteEffectType();
         if (hasteType != null && player.hasPotionEffect(hasteType)) {
            PotionEffect hasteEffect = player.getPotionEffect(hasteType);
            int hasteLevel = hasteEffect.getAmplifier() + 1;
            float hasteMultiplier = 1.0F + 0.2F * (float)hasteLevel;
            breakSpeed *= hasteMultiplier;
         }

         PotionEffectType fatigueType = this.getMiningFatigueEffectType();
         if (fatigueType != null && player.hasPotionEffect(fatigueType)) {
            PotionEffect fatigueEffect = player.getPotionEffect(fatigueType);
            int fatigueLevel = fatigueEffect.getAmplifier() + 1;
            float fatigueMultiplier = (float)Math.pow(0.3, (double)fatigueLevel);
            breakSpeed *= fatigueMultiplier;
         }

         if (player.isInWater() && !this.hasAquaAffinity(player)) {
            breakSpeed /= 5.0F;
         }

         if (!player.isOnGround()) {
            breakSpeed /= 5.0F;
         }

         return Math.max(breakSpeed, 0.1F);
      }
   }

   public boolean isDrill(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         return container.has(this.modeKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   private PotionEffectType getHasteEffectType() {
      try {
         return PotionEffectType.getByName("FAST_DIGGING");
      } catch (Exception var4) {
         try {
            return PotionEffectType.getByName("HASTE");
         } catch (Exception var3) {
            return null;
         }
      }
   }

   private PotionEffectType getMiningFatigueEffectType() {
      try {
         return PotionEffectType.getByName("SLOW_DIGGING");
      } catch (Exception var4) {
         try {
            return PotionEffectType.getByName("MINING_FATIGUE");
         } catch (Exception var3) {
            return null;
         }
      }
   }

   private float getToolEfficiency(Material tool) {
      switch (tool) {
         case NETHERITE_PICKAXE -> {
            return 9.0F;
         }
         case DIAMOND_PICKAXE -> {
            return 8.0F;
         }
         case IRON_PICKAXE -> {
            return 6.0F;
         }
         case STONE_PICKAXE -> {
            return 4.0F;
         }
         case GOLDEN_PICKAXE -> {
            return 12.0F;
         }
         case WOODEN_PICKAXE -> {
            return 2.0F;
         }
         default -> {
            return 1.0F;
         }
      }
   }

   public boolean isToolSuitableForBlock(Material tool, Material block) {
      if (!tool.toString().contains("PICKAXE")) {
         return false;
      } else {
         switch (block) {
            case BEDROCK:
            case BARRIER:
            case COMMAND_BLOCK:
            case CHAIN_COMMAND_BLOCK:
            case REPEATING_COMMAND_BLOCK:
            case STRUCTURE_BLOCK:
            case JIGSAW:
            case END_PORTAL:
            case END_PORTAL_FRAME:
            case NETHER_PORTAL:
               return false;
            default:
               return true;
         }
      }
   }

   private boolean hasAquaAffinity(Player player) {
      ItemStack helmet = player.getInventory().getHelmet();
      return helmet != null && helmet.containsEnchantment(Enchantment.AQUA_AFFINITY);
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
