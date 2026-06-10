package de.elivb.donutTools;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
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

public class EnderPearlListener implements Listener {
   private final Tools plugin;
   private WorldGuardService worldGuardService;

   public EnderPearlListener(Tools plugin) {
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
      if (this.plugin.getEnderPearl().isEnderPearl(mainHand)) {
         item = mainHand;
         usedHand = EquipmentSlot.HAND;
      } else if (this.plugin.getEnderPearl().isEnderPearl(offHand)) {
         item = offHand;
         usedHand = EquipmentSlot.OFF_HAND;
      }

      if (item != null) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            ItemMeta meta = item.getItemMeta();
            if (meta != null && this.plugin.getEnderPearl().isExpired(meta)) {
               this.plugin.getEnderPearl().handleExpiredTool(player, item);
            } else {
               Location targetLocation = player.getLocation().add(player.getLocation().getDirection().multiply(10));
               if (this.worldGuardService != null && !this.worldGuardService.canBuild(player, targetLocation)) {
                  player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
               } else if (this.plugin.getPlotService().isPlotSquaredEnabled() && !this.plugin.getPlotService().canBuild(player, targetLocation)) {
                  player.sendMessage(this.plugin.getLang("plot-protected"));
                  player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
               } else if (player.hasCooldown(Material.ENDER_PEARL)) {
                  player.sendActionBar(this.plugin.getLang("enderpearl-cooldown"));
                  player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
               } else {
                  if (meta != null) {
                     PersistentDataContainer container = meta.getPersistentDataContainer();
                     String modeStr = (String)container.get(this.plugin.getEnderPearl().getModeKey(), PersistentDataType.STRING);
                     if (modeStr != null) {
                        EnderPearl.DurabilityMode mode = EnderPearl.DurabilityMode.valueOf(modeStr);
                        if (mode == EnderPearl.DurabilityMode.USES) {
                           if (!this.plugin.getEnderPearl().decreaseUses(meta)) {
                              this.plugin.getEnderPearl().handleExpiredTool(player, item);
                              return;
                           }

                           item.setItemMeta(meta);
                           if (usedHand == EquipmentSlot.OFF_HAND) {
                              player.getInventory().setItemInOffHand(item);
                           }
                        }
                     }
                  }

                  if (this.isSoundEnabled()) {
                     Sound sound = this.getSound("sound.type", "ENTITY_ENDERMAN_TELEPORT");
                     float volume = (float)this.getSoundVolume();
                     float pitch = (float)this.getSoundPitch();
                     player.playSound(player.getLocation(), sound, volume, pitch);
                  }

                  if (this.isParticleEnabled()) {
                     Location loc = player.getLocation().add((double)0.0F, (double)1.0F, (double)0.0F);
                     Particle particle = this.getParticle("particle.type", "PORTAL");
                     int count = this.getParticleCount();
                     this.spawnParticleSafely(player.getWorld(), particle, loc, count, (double)0.5F, (double)0.5F, (double)0.5F, 0.1);
                  }

                  final org.bukkit.entity.EnderPearl pearl = (org.bukkit.entity.EnderPearl)player.launchProjectile(org.bukkit.entity.EnderPearl.class);
                  pearl.setShooter(player);
                  pearl.setVelocity(player.getLocation().getDirection().multiply((double)1.5F));
                  player.setCooldown(Material.ENDER_PEARL, 60);
                  if (this.isAdditionalParticleEnabled()) {
                     this.plugin.runAtLocationTimer(pearl.getLocation(), new Runnable() {
                        public void run() {
                           if (pearl.isValid() && !pearl.isDead()) {
                              Particle additionalParticle = EnderPearlListener.this.getParticle("particle.additional-particle.type", "DRAGON_BREATH");
                              int additionalCount = EnderPearlListener.this.getAdditionalParticleCount();
                              EnderPearlListener.this.spawnParticleSafely(pearl.getWorld(), additionalParticle, pearl.getLocation(), additionalCount, 0.2, 0.2, 0.2, 0.05);
                           }

                        }
                     }, 1L, 5L);
                  }

               }
            }
         }
      }
   }

   private void spawnParticleSafely(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
      try {
         switch (particle.name()) {
            case "DUST":
            case "REDSTONE":
               Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
               world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, dustOptions);
               break;
            case "DUST_COLOR_TRANSITION":
               Particle.DustTransition dustTransition = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(0, 0, 255), 1.0F);
               world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, dustTransition);
               break;
            default:
               world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
         }
      } catch (IllegalArgumentException var17) {
         world.spawnParticle(Particle.PORTAL, location, count, offsetX, offsetY, offsetZ, extra);
      }

   }

   private boolean isSoundEnabled() {
      return this.plugin.getEnderPearl().getConfig().getBoolean("sound.enabled", true);
   }

   private boolean isParticleEnabled() {
      return this.plugin.getEnderPearl().getConfig().getBoolean("particle.enabled", true);
   }

   private boolean isAdditionalParticleEnabled() {
      return this.plugin.getEnderPearl().getConfig().getBoolean("particle.additional-particle.enabled", true);
   }

   private Sound getSound(String path, String defaultSound) {
      String soundName = this.plugin.getEnderPearl().getConfig().getString(path, defaultSound);
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
      String particleName = this.plugin.getEnderPearl().getConfig().getString(path, defaultParticle);
      Registry<Particle> particleRegistry = Bukkit.getRegistry(Particle.class);
      Particle particle = (Particle)particleRegistry.get(NamespacedKey.minecraft(particleName.toLowerCase().replace("_", ".")));
      if (particle == null) {
         particle = (Particle)particleRegistry.get(NamespacedKey.minecraft(defaultParticle.toLowerCase().replace("_", ".")));
      }

      if (particle == null) {
         try {
            particle = Particle.valueOf(defaultParticle);
         } catch (IllegalArgumentException var7) {
            return Particle.PORTAL;
         }
      }

      return this.requiresData(particle) ? Particle.PORTAL : particle;
   }

   private boolean requiresData(Particle particle) {
      String name = particle.name();
      return name.equals("DUST") || name.equals("REDSTONE") || name.equals("DUST_COLOR_TRANSITION") || name.equals("VIBRATION") || name.equals("BLOCK_MARKER") || name.equals("FALLING_DUST") || name.equals("ITEM") || name.equals("BLOCK") || name.equals("SCULK_CHARGE");
   }

   private double getSoundVolume() {
      return this.plugin.getEnderPearl().getConfig().getDouble("sound.volume", (double)1.0F);
   }

   private double getSoundPitch() {
      return this.plugin.getEnderPearl().getConfig().getDouble("sound.pitch", (double)1.0F);
   }

   private int getParticleCount() {
      return this.plugin.getEnderPearl().getConfig().getInt("particle.count", 50);
   }

   private int getAdditionalParticleCount() {
      return this.plugin.getEnderPearl().getConfig().getInt("particle.additional-particle.count", 30);
   }
}
