package de.elivb.donutTools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ToolExpiryChecker {
   private final Tools plugin;
   private boolean running = false;
   private static final int DEFAULT_CHECK_INTERVAL = 10;
   private Object taskId = null;

   public ToolExpiryChecker(Tools plugin) {
      this.plugin = plugin;
   }

   public void start() {
      if (!this.running) {
         this.running = true;
         int checkInterval = 200;
         if (this.plugin.isFolia()) {
            this.plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(this.plugin, (scheduledTask) -> {
               if (this.running) {
                  this.checkAllOnlinePlayers();
               } else {
                  scheduledTask.cancel();
               }

            }, 100L, (long)checkInterval);
         } else {
            int taskIdInt = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
               if (this.running) {
                  this.checkAllOnlinePlayers();
               }

            }, 100L, (long)checkInterval);
            this.taskId = taskIdInt;
         }

      }
   }

   public void stop() {
      this.running = false;
      if (!this.plugin.isFolia() && this.taskId instanceof Integer) {
         this.plugin.getServer().getScheduler().cancelTask((Integer)this.taskId);
      }

   }

   private void checkAllOnlinePlayers() {
      for(Player player : Bukkit.getOnlinePlayers()) {
         this.checkPlayerInventory(player);
      }

   }

   private void checkPlayerInventory(Player player) {
      for(ItemStack item : player.getInventory().getContents()) {
         if (item != null) {
            this.checkItemExpiry(player, item);
         }
      }

      for(ItemStack item : player.getInventory().getArmorContents()) {
         if (item != null) {
            this.checkItemExpiry(player, item);
         }
      }

      ItemStack offhand = player.getInventory().getItemInOffHand();
      if (offhand != null) {
         this.checkItemExpiry(player, offhand);
      }

   }

   private void checkItemExpiry(Player player, ItemStack item) {
      if (this.plugin.getDrill().isDrill(item)) {
         this.checkToolExpiry(player, item, this.plugin.getDrill());
      } else if (this.plugin.getTreeChopper().isTreeChopper(item)) {
         this.checkToolExpiry(player, item, this.plugin.getTreeChopper());
      } else if (this.plugin.getShovel().isShovel(item)) {
         this.checkToolExpiry(player, item, this.plugin.getShovel());
      } else if (this.plugin.getHoe().isHoe(item)) {
         this.checkToolExpiry(player, item, this.plugin.getHoe());
      } else if (this.plugin.getEnderPearl().isEnderPearl(item)) {
         this.checkToolExpiry(player, item, this.plugin.getEnderPearl());
      }

   }

   private void checkToolExpiry(Player player, ItemStack item, Object tool) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         if (tool instanceof Drill) {
            Drill drill = (Drill)tool;
            if (drill.isExpired(meta)) {
               this.plugin.runEntity(player, () -> drill.handleExpiredTool(player, item));
               return;
            }

            if (drill.getDurabilityMode() == Drill.DurabilityMode.TIME) {
               drill.updateLore(meta);
               item.setItemMeta(meta);
            }
         } else if (tool instanceof TreeChopper) {
            TreeChopper treeChopper = (TreeChopper)tool;
            if (treeChopper.isExpired(meta)) {
               this.plugin.runEntity(player, () -> treeChopper.handleExpiredTool(player, item));
               return;
            }

            if (treeChopper.getDurabilityMode() == TreeChopper.DurabilityMode.TIME) {
               treeChopper.updateLore(meta);
               item.setItemMeta(meta);
            }
         } else if (tool instanceof Shovel) {
            Shovel shovel = (Shovel)tool;
            if (shovel.isExpired(meta)) {
               this.plugin.runEntity(player, () -> shovel.handleExpiredTool(player, item));
               return;
            }

            if (shovel.getDurabilityMode() == Shovel.DurabilityMode.TIME) {
               shovel.updateLore(meta);
               item.setItemMeta(meta);
            }
         } else if (tool instanceof Hoe) {
            Hoe hoe = (Hoe)tool;
            if (hoe.isExpired(meta)) {
               this.plugin.runEntity(player, () -> hoe.handleExpiredTool(player, item));
               return;
            }

            if (hoe.getDurabilityMode() == Hoe.DurabilityMode.TIME) {
               hoe.updateLore(meta);
               item.setItemMeta(meta);
            }
         } else if (tool instanceof EnderPearl) {
            EnderPearl enderPearl = (EnderPearl)tool;
            if (enderPearl.isExpired(meta)) {
               this.plugin.runEntity(player, () -> enderPearl.handleExpiredTool(player, item));
               return;
            }

            if (enderPearl.getDurabilityMode() == EnderPearl.DurabilityMode.TIME) {
               enderPearl.updateLore(meta);
               item.setItemMeta(meta);
            }
         }

      }
   }
}
