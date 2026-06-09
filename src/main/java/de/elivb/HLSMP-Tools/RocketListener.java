package de.elivb.donutTools;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
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
import org.bukkit.util.Vector;

public class RocketListener implements Listener {
   private final Tools plugin;
   private final Rocket rocket;
   private WorldGuardService worldGuardService;

   public RocketListener(Tools plugin) {
      this.plugin = plugin;
      this.rocket = plugin.getRocket();

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
      if (this.rocket.isRocket(mainHand)) {
         item = mainHand;
         usedHand = EquipmentSlot.HAND;
      } else if (this.rocket.isRocket(offHand)) {
         item = offHand;
         usedHand = EquipmentSlot.OFF_HAND;
      }

      if (item != null) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
               if (this.rocket.isExpired(meta)) {
                  this.rocket.handleExpiredTool(player, item);
               } else {
                  PersistentDataContainer container = meta.getPersistentDataContainer();
                  Rocket.DurabilityMode currentMode = null;
                  if (container.has(this.rocket.getModeKey(), PersistentDataType.STRING)) {
                     String modeStr = (String)container.get(this.rocket.getModeKey(), PersistentDataType.STRING);

                     try {
                        currentMode = Rocket.DurabilityMode.valueOf(modeStr);
                     } catch (IllegalArgumentException var12) {
                        currentMode = this.rocket.getDurabilityMode();
                     }
                  } else {
                     currentMode = this.rocket.getDurabilityMode();
                  }

                  if (currentMode == Rocket.DurabilityMode.USES) {
                     int remainingUses = this.rocket.getRemainingUses(meta);
                     if (remainingUses <= 0) {
                        this.rocket.handleExpiredTool(player, item);
                        return;
                     }
                  }

                  if (!this.rocket.hasElytra(player)) {
                     player.sendMessage(this.plugin.getLang("rocket-needs-elytra"));
                     player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                  } else if (!player.isGliding()) {
                     player.sendMessage(this.plugin.getLang("rocket-not-gliding"));
                     player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                  } else if (this.worldGuardService != null && !this.worldGuardService.canBuild(player, player.getLocation())) {
                     player.sendMessage(this.plugin.getLang("region-protected"));
                  } else if (this.plugin.getPlotService().isPlotSquaredEnabled() && !this.plugin.getPlotService().canBuild(player, player.getLocation())) {
                     player.sendMessage(this.plugin.getLang("plot-protected"));
                     player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                  } else {
                     this.useRocketWithMode(player, item, usedHand, currentMode, meta);
                  }
               }
            }
         }
      }
   }

   private void useRocketWithMode(Player player, ItemStack rocketItem, EquipmentSlot hand, Rocket.DurabilityMode currentMode, ItemMeta meta) {
      boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
      if (this.rocket.getConfig().getBoolean("sound.enabled", true)) {
         player.getWorld().playSound(player.getLocation(), this.rocket.getSoundType(), this.rocket.getSoundVolume(), this.rocket.getSoundPitch());
      }

      if (this.rocket.getConfig().getBoolean("particle.enabled", true)) {
         Location loc = player.getLocation().add((double)0.0F, (double)1.0F, (double)0.0F);
         player.getWorld().spawnParticle(this.rocket.getMainParticle(), loc, this.rocket.getMainParticleCount(), (double)0.5F, (double)0.5F, (double)0.5F, 0.1);
         if (this.rocket.getConfig().getBoolean("particle.additional-particle.enabled", true)) {
            player.getWorld().spawnParticle(this.rocket.getAdditionalParticle(), loc, this.rocket.getAdditionalParticleCount(), 0.3, 0.3, 0.3, 0.05);
         }
      }

      Vector direction = player.getLocation().getDirection();
      double boostPower = this.rocket.getConfig().getDouble("rocket.boost-power", (double)1.5F);
      Vector newVelocity = direction.multiply(boostPower);
      player.setVelocity(newVelocity);
      if (!isCreative) {
         if (currentMode == Rocket.DurabilityMode.USES) {
            if (!this.rocket.decreaseUses(meta)) {
               this.rocket.handleExpiredTool(player, rocketItem);
               return;
            }

            rocketItem.setItemMeta(meta);
            if (hand == EquipmentSlot.OFF_HAND) {
               player.getInventory().setItemInOffHand(rocketItem);
            }
         } else if (currentMode == Rocket.DurabilityMode.TIME) {
            this.rocket.updateLore(meta);
            rocketItem.setItemMeta(meta);
            if (hand == EquipmentSlot.OFF_HAND) {
               player.getInventory().setItemInOffHand(rocketItem);
            }
         }
      }

   }
}
