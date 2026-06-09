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

public class TreeChopperListener implements Listener {
   private final Tools plugin;
   private final TreeChopper treeChopper;
   private WorldGuardService worldGuardService;

   public TreeChopperListener(Tools plugin) {
      this.plugin = plugin;
      this.treeChopper = plugin.getTreeChopper();

      try {
         this.worldGuardService = new WorldGuardService(plugin);
      } catch (Throwable var3) {
         this.worldGuardService = null;
      }

   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      ItemStack item = player.getInventory().getItemInMainHand();
      if (this.treeChopper.isTreeChopper(item)) {
         if (this.treeChopper.isLog(block.getType())) {
            if (this.worldGuardService != null && !this.worldGuardService.canBuild(player, block.getLocation())) {
               event.setCancelled(true);
            } else if (this.plugin.getPlotService().isPlotSquaredEnabled() && !this.plugin.getPlotService().canBuild(player, block.getLocation())) {
               event.setCancelled(true);
               player.sendMessage(this.plugin.getLang("plot-protected"));
            } else {
               ItemMeta meta = item.getItemMeta();
               if (meta != null) {
                  if (this.treeChopper.isExpired(meta)) {
                     this.treeChopper.handleExpiredTool(player, item);
                     event.setCancelled(true);
                  } else {
                     TreeChopper.DurabilityMode currentMode = null;
                     PersistentDataContainer container = meta.getPersistentDataContainer();
                     if (container.has(this.treeChopper.getModeKey(), PersistentDataType.STRING)) {
                        String modeStr = (String)container.get(this.treeChopper.getModeKey(), PersistentDataType.STRING);

                        try {
                           currentMode = TreeChopper.DurabilityMode.valueOf(modeStr);
                        } catch (IllegalArgumentException var10) {
                           currentMode = this.treeChopper.getDurabilityMode();
                        }
                     } else {
                        currentMode = this.treeChopper.getDurabilityMode();
                     }

                     if (currentMode == TreeChopper.DurabilityMode.USES) {
                        int remainingUses = this.treeChopper.getRemainingUses(meta);
                        if (remainingUses <= 0) {
                           event.setCancelled(true);
                           this.treeChopper.handleExpiredTool(player, item);
                           return;
                        }
                     }

                     this.plugin.runAtLocationLater(block.getLocation(), () -> {
                        int blocksBroken = this.treeChopper.fellTree(player, block);
                        if (blocksBroken > 0) {
                           this.treeChopper.playEffects(player, block);
                           this.handleDurabilityUpdate(player, item);
                        }

                     }, 2L);
                  }
               }
            }
         }
      }
   }

   private void handleDurabilityUpdate(Player player, ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         PersistentDataContainer container = meta.getPersistentDataContainer();
         TreeChopper.DurabilityMode currentMode = null;
         if (container.has(this.treeChopper.getModeKey(), PersistentDataType.STRING)) {
            String modeStr = (String)container.get(this.treeChopper.getModeKey(), PersistentDataType.STRING);

            try {
               currentMode = TreeChopper.DurabilityMode.valueOf(modeStr);
            } catch (IllegalArgumentException var8) {
               currentMode = this.treeChopper.getDurabilityMode();
            }
         } else {
            currentMode = this.treeChopper.getDurabilityMode();
         }

         if (currentMode == TreeChopper.DurabilityMode.USES) {
            int currentUses = this.treeChopper.getRemainingUses(meta);
            if (currentUses <= 0) {
               this.treeChopper.handleExpiredTool(player, item);
               return;
            }

            boolean success = this.treeChopper.decreaseUses(meta);
            if (!success) {
               this.treeChopper.handleExpiredTool(player, item);
               return;
            }

            item.setItemMeta(meta);
            if (currentUses == 1) {
               this.treeChopper.handleExpiredTool(player, item);
            }
         }

      }
   }
}
