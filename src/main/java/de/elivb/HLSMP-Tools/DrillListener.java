package de.elivb.donutTools;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DrillListener implements Listener {
   private final Tools plugin;
   private final Drill drill;
   private WorldGuardService worldGuardService;

   public DrillListener(Tools plugin) {
      this.plugin = plugin;
      this.drill = plugin.getDrill();

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
      if (this.drill.isDrill(item)) {
         if (this.drill.isToolSuitableForBlock(this.drill.getMaterial(), centerBlock.getType())) {
            if (this.worldGuardService == null || this.worldGuardService.canBuild(player, centerBlock.getLocation())) {
               if (this.plugin.getPlotService().isPlotSquaredEnabled() && !this.plugin.getPlotService().canBuild(player, centerBlock.getLocation())) {
                  player.sendMessage(this.plugin.getLang("plot-protected"));
               } else {
                  this.plugin.runAtLocationLater(centerBlock.getLocation(), () -> {
                     int blocksBroken = this.drill.mine3x3(player, centerBlock);
                     if (blocksBroken > 0) {
                        this.drill.playBlockEffects(player, centerBlock);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                           PersistentDataContainer container = meta.getPersistentDataContainer();
                           String modeStr = (String)container.get(this.drill.getModeKey(), PersistentDataType.STRING);
                           if (modeStr != null) {
                              Drill.DurabilityMode mode = Drill.DurabilityMode.valueOf(modeStr);
                              if (mode == Drill.DurabilityMode.USES) {
                                 this.decreaseUses(player, item);
                              }
                           }
                        }
                     }

                  }, 1L);
               }
            }
         }
      }
   }

   private void decreaseUses(Player player, ItemStack item) {
      try {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return;
         }

         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (!container.has(this.drill.getUsesKey(), PersistentDataType.INTEGER)) {
            return;
         }

         Integer uses = (Integer)container.get(this.drill.getUsesKey(), PersistentDataType.INTEGER);
         if (uses == null) {
            uses = this.drill.getMaxUses();
         }

         if (uses <= 1) {
            player.getInventory().remove(item);
            String toolName = "Drill";
            player.sendMessage(this.plugin.getLang("tool-broken", "tool_name", toolName));
            player.sendActionBar(this.plugin.getLang("tool-broken", "tool_name", toolName));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            return;
         }

         --uses;
         this.updateDrillLore(meta, uses);
         container.set(this.drill.getUsesKey(), PersistentDataType.INTEGER, uses);
         item.setItemMeta(meta);
         this.drill.updateLore(meta);
         item.setItemMeta(meta);
      } catch (Exception var7) {
      }

   }

   private void updateDrillLore(ItemMeta meta, int uses) {
      List<String> loreList = this.drill.getConfig().getStringList("lore");
      if (loreList.isEmpty()) {
         String loreText = this.drill.getConfig().getString("lore", "&#FF5555&lBuild &#FFAA00&l3x3 &#FFFF55&lBlocks");
         if (!loreText.isEmpty()) {
            loreList.add(loreText);
         } else {
            loreList.add("§6Build 3x3 Blocks");
            loreList.add("§eUses: " + uses);
         }
      }

      ArrayList<String> translatedLore = new ArrayList();

      for(String line : loreList) {
         String translatedLine = HexColorCode.translateAllColorCodes(line.replace("%uses%", "§fUses: " + uses));
         translatedLore.add(translatedLine);
      }

      meta.setLore(translatedLore);
   }
}
