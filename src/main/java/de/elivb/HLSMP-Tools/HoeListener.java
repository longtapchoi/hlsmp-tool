package de.elivb.donutTools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class HoeListener implements Listener {
   private final Tools plugin;
   private WorldGuardService worldGuardService;

   public HoeListener(Tools plugin) {
      this.plugin = plugin;

      try {
         this.worldGuardService = new WorldGuardService(plugin);
      } catch (Throwable var3) {
         this.worldGuardService = null;
      }

   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      ItemStack item = null;
      EquipmentSlot usedHand = null;
      if (this.plugin.getHoe().isHoe(mainHand)) {
         item = mainHand;
         usedHand = EquipmentSlot.HAND;
      } else if (this.plugin.getHoe().isHoe(offHand)) {
         item = offHand;
         usedHand = EquipmentSlot.OFF_HAND;
      }

      if (item != null) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block centerBlock = event.getClickedBlock();
            if (centerBlock != null) {
               if (this.worldGuardService != null && !this.worldGuardService.canBuild(player, centerBlock.getLocation())) {
                  event.setCancelled(true);
               } else if (this.plugin.getPlotService().isPlotSquaredEnabled() && !this.plugin.getPlotService().canBuild(player, centerBlock.getLocation())) {
                  event.setCancelled(true);
                  player.sendMessage(this.plugin.getLang("plot-protected"));
               } else {
                  event.setCancelled(true);
                  ItemMeta meta = item.getItemMeta();
                  if (meta != null && this.plugin.getHoe().isExpired(meta)) {
                     this.plugin.getHoe().handleExpiredTool(player, item);
                  } else {
                     boolean canHarvest = this.isCrop(centerBlock.getType());
                     boolean canCreateFarmland = this.canConvertToFarmland(centerBlock);
                     if (canHarvest || canCreateFarmland) {
                        if (meta != null) {
                           PersistentDataContainer container = meta.getPersistentDataContainer();
                           String modeStr = (String)container.get(this.plugin.getHoe().getModeKey(), PersistentDataType.STRING);
                           if (modeStr != null) {
                              Hoe.DurabilityMode mode = Hoe.DurabilityMode.valueOf(modeStr);
                              if (mode == Hoe.DurabilityMode.USES) {
                                 if (!this.plugin.getHoe().decreaseUses(meta)) {
                                    this.plugin.getHoe().handleExpiredTool(player, item);
                                    return;
                                 }

                                 item.setItemMeta(meta);
                                 if (usedHand == EquipmentSlot.OFF_HAND) {
                                    player.getInventory().setItemInOffHand(item);
                                 }
                              }
                           }
                        }

                        if (canHarvest) {
                           this.harvest3x3Area(player, centerBlock);
                        } else if (canCreateFarmland) {
                           this.createFarmland3x3(player, centerBlock);
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private void breakTool(Player player, ItemStack item) {
      player.getInventory().setItemInMainHand((ItemStack)null);
      String toolName = "Hoe";
      player.sendMessage(this.plugin.getLang("tool-broken", "tool_name", toolName));
      player.sendActionBar(this.plugin.getLang("tool-broken", "tool_name", toolName));
      player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
   }

   private void harvest3x3Area(final Player player, Block centerBlock) {
      final Location center = centerBlock.getLocation();
      final World world = centerBlock.getWorld();
      if (this.isSoundEnabled() || this.isParticleEnabled()) {
         this.showHarvestAreaEffect(player, center);
      }

      this.plugin.runAtLocationLater(center, new Runnable() {
         public void run() {
            int harvestedBlocks = 0;

            for(int x = -1; x <= 1; ++x) {
               for(int z = -1; z <= 1; ++z) {
                  Block targetBlock = world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z);
                  if ((HoeListener.this.worldGuardService == null || HoeListener.this.worldGuardService.canBuild(player, targetBlock.getLocation())) && (!HoeListener.this.plugin.getPlotService().isPlotSquaredEnabled() || HoeListener.this.plugin.getPlotService().canBuild(player, targetBlock.getLocation())) && HoeListener.this.harvestBlock(player, targetBlock)) {
                     ++harvestedBlocks;
                  }
               }
            }

            if (harvestedBlocks > 0 && (HoeListener.this.isSoundEnabled() || HoeListener.this.isParticleEnabled())) {
               HoeListener.this.playHarvestEffects(player, center);
            }

         }
      }, 5L);
   }

   private void showHarvestAreaEffect(Player player, Location center) {
      World world = center.getWorld();
      if (this.isSoundEnabled()) {
         Sound sound = this.getSound("sound.type", "BLOCK_CROP_BREAK");
         float volume = (float)this.getSoundVolume();
         float pitch = (float)this.getSoundPitch();
         player.playSound(center, sound, volume, pitch);
      }

      if (this.isParticleEnabled()) {
         Particle mainParticle = this.getParticle("particle.type", "VILLAGER_HAPPY");
         int mainCount = this.getParticleCount();

         for(int x = -1; x <= 1; ++x) {
            for(int z = -1; z <= 1; ++z) {
               Location particleLoc = center.clone().add((double)x + (double)0.5F, 1.2, (double)z + (double)0.5F);
               world.spawnParticle(mainParticle, particleLoc, mainCount, 0.3, 0.3, 0.3, 0.1);
               if (this.isAdditionalParticleEnabled()) {
                  Particle additionalParticle = this.getParticle("particle.additional-particle.type", "CLOUD");
                  int additionalCount = this.getAdditionalParticleCount();
                  world.spawnParticle(additionalParticle, particleLoc, additionalCount, 0.2, 0.2, 0.2, 0.05);
               }
            }
         }
      }

   }

   private boolean harvestBlock(Player player, Block block) {
      Material blockType = block.getType();
      if (!this.isCrop(blockType)) {
         return false;
      } else {
         boolean wasHarvested = false;
         if (blockType == Material.WHEAT) {
            if (this.isFullyGrown(block)) {
               block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT));
               block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT_SEEDS, 1));
               block.setType(Material.AIR);
               wasHarvested = true;
            }
         } else if (blockType == Material.CARROTS) {
            if (this.isFullyGrown(block)) {
               block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.CARROT, 2));
               block.setType(Material.AIR);
               wasHarvested = true;
            }
         } else if (blockType == Material.POTATOES) {
            if (this.isFullyGrown(block)) {
               block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.POTATO, 2));
               block.setType(Material.AIR);
               wasHarvested = true;
            }
         } else if (blockType == Material.BEETROOTS) {
            if (this.isFullyGrown(block)) {
               block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.BEETROOT));
               block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.BEETROOT_SEEDS, 1));
               block.setType(Material.AIR);
               wasHarvested = true;
            }
         } else if (blockType == Material.NETHER_WART) {
            if (this.isFullyGrown(block)) {
               block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.NETHER_WART, 2));
               block.setBlockData(Bukkit.createBlockData(Material.NETHER_WART));
               wasHarvested = true;
            }
         } else if (blockType != Material.MELON_STEM && blockType != Material.PUMPKIN_STEM) {
            if (blockType == Material.COCOA) {
               if (this.isFullyGrown(block)) {
                  block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COCOA_BEANS, 3));
                  block.setBlockData(Bukkit.createBlockData(Material.COCOA));
                  wasHarvested = true;
               }
            } else if (blockType == Material.SWEET_BERRY_BUSH) {
               if (this.isFullyGrown(block)) {
                  block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SWEET_BERRIES, 2));
                  block.setBlockData(Bukkit.createBlockData(Material.SWEET_BERRY_BUSH));
                  wasHarvested = true;
               }
            } else if (blockType != Material.CACTUS && blockType != Material.SUGAR_CANE && blockType != Material.BAMBOO) {
               if (blockType != Material.MELON && blockType != Material.PUMPKIN) {
                  for(ItemStack drop : block.getDrops()) {
                     block.getWorld().dropItemNaturally(block.getLocation(), drop);
                  }

                  block.setType(Material.AIR);
                  wasHarvested = true;
               } else {
                  for(ItemStack drop : block.getDrops()) {
                     block.getWorld().dropItemNaturally(block.getLocation(), drop);
                  }

                  block.setType(Material.AIR);
                  wasHarvested = true;
               }
            } else {
               this.harvestTallPlant(block);
               wasHarvested = true;
            }
         }

         if (wasHarvested && (this.isSoundEnabled() || this.isParticleEnabled())) {
            this.playBlockBreakEffects(player, block);
         }

         return wasHarvested;
      }
   }

   private void playBlockBreakEffects(Player player, Block block) {
      Location location = block.getLocation().add((double)0.5F, (double)0.5F, (double)0.5F);
      if (this.isSoundEnabled()) {
         Sound sound = this.getSound("sound.type", "BLOCK_CROP_BREAK");
         float volume = (float)this.getSoundVolume();
         float pitch = (float)this.getSoundPitch();
         player.playSound(location, sound, volume * 0.6F, pitch);
      }

      if (this.isParticleEnabled()) {
         Particle particle = this.getParticle("particle.type", "VILLAGER_HAPPY");
         player.spawnParticle(particle, location, 3, 0.2, 0.2, 0.2, 0.05);
      }

   }

   private void playHarvestEffects(Player player, Location center) {
      if (this.isSoundEnabled()) {
         player.playSound(center, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.2F);
      }

      if (this.isParticleEnabled()) {
         World world = center.getWorld();
         Location effectCenter = center.clone().add((double)0.5F, (double)1.0F, (double)0.5F);
         Particle particle = this.getParticle("particle.type", "VILLAGER_HAPPY");
         world.spawnParticle(particle, effectCenter, 15, 0.7, 0.7, 0.7, 0.1);
      }

   }

   private void createFarmland3x3(final Player player, Block centerBlock) {
      final Location center = centerBlock.getLocation();
      final World world = centerBlock.getWorld();
      if (this.isSoundEnabled() || this.isParticleEnabled()) {
         this.showFarmlandAreaEffect(player, center);
      }

      this.plugin.runAtLocationLater(center, new Runnable() {
         public void run() {
            int blocksPlaced = 0;

            for(int x = -1; x <= 1; ++x) {
               for(int z = -1; z <= 1; ++z) {
                  Block targetBlock = world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z);
                  if ((HoeListener.this.worldGuardService == null || HoeListener.this.worldGuardService.canBuild(player, targetBlock.getLocation())) && (!HoeListener.this.plugin.getPlotService().isPlotSquaredEnabled() || HoeListener.this.plugin.getPlotService().canBuild(player, targetBlock.getLocation())) && HoeListener.this.canConvertToFarmland(targetBlock)) {
                     HoeListener.this.convertToFarmland(targetBlock);
                     ++blocksPlaced;
                  }
               }
            }

            if (blocksPlaced > 0 && (HoeListener.this.isSoundEnabled() || HoeListener.this.isParticleEnabled())) {
               HoeListener.this.playFarmlandEffects(player, center);
            }

         }
      }, 5L);
   }

   private void showFarmlandAreaEffect(Player player, Location center) {
      World world = center.getWorld();
      if (this.isSoundEnabled()) {
         Sound sound = this.getSound("sound.type", "ITEM_HOE_TILL");
         float volume = (float)this.getSoundVolume();
         float pitch = (float)this.getSoundPitch();
         player.playSound(center, sound, volume, pitch);
      }

      if (this.isParticleEnabled()) {
         Particle particle = this.getParticle("particle.type", "VILLAGER_HAPPY");
         int count = this.getParticleCount();

         for(int x = -1; x <= 1; ++x) {
            for(int z = -1; z <= 1; ++z) {
               Location particleLoc = center.clone().add((double)x + (double)0.5F, 1.2, (double)z + (double)0.5F);
               world.spawnParticle(particle, particleLoc, count / 2, 0.2, 0.2, 0.2, 0.05);
            }
         }
      }

   }

   private void playFarmlandEffects(Player player, Location center) {
      if (this.isSoundEnabled()) {
         player.playSound(center, Sound.BLOCK_GRAVEL_PLACE, 1.0F, 1.0F);
         Sound sound = this.getSound("sound.type", "ITEM_HOE_TILL");
         float volume = (float)this.getSoundVolume();
         float pitch = (float)this.getSoundPitch();
         player.playSound(center, sound, volume * 0.8F, pitch * 1.2F);
      }

      if (this.isParticleEnabled()) {
         World world = center.getWorld();
         Location effectCenter = center.clone().add((double)0.5F, (double)1.0F, (double)0.5F);
         Particle particle = this.getParticle("particle.type", "VILLAGER_HAPPY");
         world.spawnParticle(particle, effectCenter, 10, 0.7, 0.7, 0.7, 0.1);
      }

   }

   private boolean isSoundEnabled() {
      return this.plugin.getHoe().getConfig().getBoolean("sound.enabled", true);
   }

   private boolean isParticleEnabled() {
      return this.plugin.getHoe().getConfig().getBoolean("particle.enabled", true);
   }

   private boolean isAdditionalParticleEnabled() {
      return this.plugin.getHoe().getConfig().getBoolean("particle.additional-particle.enabled", true);
   }

   private Sound getSound(String path, String defaultSound) {
      String soundName = this.plugin.getHoe().getConfig().getString(path, defaultSound);
      Registry<Sound> soundRegistry = Bukkit.getRegistry(Sound.class);
      Sound sound = (Sound)soundRegistry.get(NamespacedKey.minecraft(soundName.toLowerCase().replace("_", ".")));
      if (sound == null) {
         sound = (Sound)soundRegistry.get(NamespacedKey.minecraft(defaultSound.toLowerCase().replace("_", ".")));
      }

      if (sound == null) {
      }

      return sound;
   }

   private Particle getParticle(String path, String defaultParticle) {
      String particleName = this.plugin.getHoe().getConfig().getString(path, defaultParticle);
      Registry<Particle> particleRegistry = Bukkit.getRegistry(Particle.class);
      Particle particle = (Particle)particleRegistry.get(NamespacedKey.minecraft(particleName.toLowerCase().replace("_", ".")));
      if (particle == null) {
         particle = (Particle)particleRegistry.get(NamespacedKey.minecraft(defaultParticle.toLowerCase().replace("_", ".")));
      }

      if (particle == null) {
         try {
            particle = Particle.valueOf(defaultParticle);
         } catch (IllegalArgumentException var7) {
         }
      }

      return particle;
   }

   private double getSoundVolume() {
      return this.plugin.getHoe().getConfig().getDouble("sound.volume", 0.4);
   }

   private double getSoundPitch() {
      return this.plugin.getHoe().getConfig().getDouble("sound.pitch", (double)1.0F);
   }

   private int getParticleCount() {
      return this.plugin.getHoe().getConfig().getInt("particle.count", 15);
   }

   private int getAdditionalParticleCount() {
      return this.plugin.getHoe().getConfig().getInt("particle.additional-particle.count", 10);
   }

   private boolean isCrop(Material material) {
      return material == Material.WHEAT || material == Material.CARROTS || material == Material.POTATOES || material == Material.BEETROOTS || material == Material.MELON_STEM || material == Material.PUMPKIN_STEM || material == Material.NETHER_WART || material == Material.COCOA || material == Material.SWEET_BERRY_BUSH || material == Material.CACTUS || material == Material.SUGAR_CANE || material == Material.BAMBOO || material == Material.MELON || material == Material.PUMPKIN;
   }

   private boolean isFullyGrown(Block block) {
      Material type = block.getType();
      BlockData blockData = block.getBlockData();
      if (blockData instanceof Ageable ageable) {
         if (type != Material.WHEAT && type != Material.CARROTS && type != Material.POTATOES) {
            if (type != Material.BEETROOTS && type != Material.NETHER_WART) {
               if (type != Material.COCOA && type != Material.SWEET_BERRY_BUSH) {
                  return true;
               } else {
                  return ageable.getAge() >= 2;
               }
            } else {
               return ageable.getAge() >= 3;
            }
         } else {
            return ageable.getAge() == ageable.getMaximumAge();
         }
      } else {
         return true;
      }
   }

   private void harvestTallPlant(Block startBlock) {
      Material plantType = startBlock.getType();

      for(Block currentBlock = startBlock; currentBlock.getType() == plantType; currentBlock = currentBlock.getRelative(0, 1, 0)) {
         for(ItemStack drop : currentBlock.getDrops()) {
            currentBlock.getWorld().dropItemNaturally(currentBlock.getLocation(), drop);
         }

         currentBlock.setType(Material.AIR);
      }

   }

   private boolean canConvertToFarmland(Block block) {
      Material blockType = block.getType();
      return blockType == Material.DIRT || blockType == Material.GRASS_BLOCK || blockType == Material.COARSE_DIRT || blockType == Material.ROOTED_DIRT;
   }

   private void convertToFarmland(Block block) {
      block.setType(Material.FARMLAND);
      BlockData blockData = block.getBlockData();
      if (blockData instanceof Farmland farmland) {
         farmland.setMoisture(farmland.getMaximumMoisture());
         block.setBlockData(farmland);
      }

   }
}
