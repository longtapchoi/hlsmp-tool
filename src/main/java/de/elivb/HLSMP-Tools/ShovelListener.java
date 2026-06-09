package de.elivb.donutTools;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ShovelListener implements Listener {
   private final Tools plugin;
   private final Shovel shovel;
   private WorldGuardService worldGuardService;

   public ShovelListener(Tools plugin) {
      this.plugin = plugin;
      this.shovel = plugin.getShovel();

      try {
         this.worldGuardService = new WorldGuardService(plugin);
      } catch (Throwable var3) {
         this.worldGuardService = null;
      }

   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block centerBlock = event.getBlock();
      ItemStack item = player.getInventory().getItemInMainHand();
      if (this.shovel.isShovel(item)) {
         if (this.shovel.isShovelable(centerBlock.getType())) {
            if (this.worldGuardService != null && !this.worldGuardService.canBuild(player, centerBlock.getLocation())) {
               event.setCancelled(true);
            } else if (this.plugin.getPlotService().isPlotSquaredEnabled() && !this.plugin.getPlotService().canBuild(player, centerBlock.getLocation())) {
               event.setCancelled(true);
               player.sendMessage(this.plugin.getLang("plot-protected"));
            } else {
               ItemMeta meta = item.getItemMeta();
               if (meta != null) {
                  if (this.shovel.isExpired(meta)) {
                     this.shovel.handleExpiredTool(player, item);
                     event.setCancelled(true);
                  } else {
                     if (this.shovel.getDurabilityMode() == Shovel.DurabilityMode.USES) {
                        int remainingUses = this.shovel.getRemainingUses(meta);
                        if (remainingUses <= 0) {
                           event.setCancelled(true);
                           return;
                        }
                     }

                     this.plugin.runAtLocationLater(centerBlock.getLocation(), () -> {
                        int blocksBroken = this.shovel.mine3x3(player, centerBlock);
                        if (blocksBroken > 0) {
                           this.shovel.playEffects(player, centerBlock);
                           this.handleDurabilityUpdate(player, item, meta);
                        }

                     }, 1L);
                  }
               }
            }
         }
      }
   }

   private void handleDurabilityUpdate(Player player, ItemStack item, ItemMeta meta) {
      PersistentDataContainer container = meta.getPersistentDataContainer();
      String modeStr = (String)container.get(this.shovel.getModeKey(), PersistentDataType.STRING);
      if (modeStr != null) {
         Shovel.DurabilityMode mode = Shovel.DurabilityMode.valueOf(modeStr);
         if (mode == Shovel.DurabilityMode.USES) {
            int currentUses = this.shovel.getRemainingUses(meta);
            if (!this.shovel.decreaseUses(meta)) {
               this.shovel.handleExpiredTool(player, item);
               return;
            }

            item.setItemMeta(meta);
            if (currentUses == 1) {
               this.shovel.handleExpiredTool(player, item);
            }
         }
      }

   }
}
