package de.elivb.donutTools;

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

public class WaterBucketListener implements Listener {
   private final Tools plugin;
   private final WaterBucket waterBucket;
   private WorldGuardService worldGuardService;

   public WaterBucketListener(Tools plugin) {
      this.plugin = plugin;
      this.waterBucket = plugin.getWaterBucket();

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
      if (this.waterBucket.isWaterBucket(mainHand)) {
         item = mainHand;
         usedHand = EquipmentSlot.HAND;
      } else if (this.waterBucket.isWaterBucket(offHand)) {
         item = offHand;
         usedHand = EquipmentSlot.OFF_HAND;
      }

      if (item != null) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
               if (this.waterBucket.isExpired(meta)) {
                  this.waterBucket.handleExpiredTool(player, item);
               } else {
                  PersistentDataContainer container = meta.getPersistentDataContainer();
                  WaterBucket.DurabilityMode currentMode = null;
                  if (container.has(this.waterBucket.getModeKey(), PersistentDataType.STRING)) {
                     String modeStr = (String)container.get(this.waterBucket.getModeKey(), PersistentDataType.STRING);

                     try {
                        currentMode = WaterBucket.DurabilityMode.valueOf(modeStr);
                     } catch (IllegalArgumentException var12) {
                        currentMode = this.waterBucket.getDurabilityMode();
                     }
                  } else {
                     currentMode = this.waterBucket.getDurabilityMode();
                  }

                  if (currentMode == WaterBucket.DurabilityMode.USES) {
                     int remainingUses = this.waterBucket.getRemainingUses(meta);
                     if (remainingUses <= 0) {
                        this.waterBucket.handleExpiredTool(player, item);
                        return;
                     }
                  }

                  if (this.worldGuardService != null && !this.worldGuardService.canBuild(player, player.getLocation())) {
                     player.sendMessage(this.plugin.getLang("region-protected"));
                  } else if (this.plugin.getPlotService().isPlotSquaredEnabled() && !this.plugin.getPlotService().canBuild(player, player.getLocation())) {
                     player.sendMessage(this.plugin.getLang("plot-protected"));
                     player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                  } else {
                     this.waterBucket.useBucket(player, item);
                     if (usedHand == EquipmentSlot.OFF_HAND) {
                        player.getInventory().setItemInOffHand(item);
                     }

                  }
               }
            }
         }
      }
   }
}
