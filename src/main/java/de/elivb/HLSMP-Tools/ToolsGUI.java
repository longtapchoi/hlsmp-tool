package de.elivb.donutTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ToolsGUI implements Listener {
   private final Tools plugin;
   private String guiTitle;
   private int rows;
   private final Map<Integer, String> slotToToolMap = new HashMap();
   private final Map<UUID, Long> clickCooldown = new HashMap();
   private static final long COOLDOWN_TIME = 250L;
   private static final GUIHolder GUI_HOLDER = new GUIHolder();

   public ToolsGUI(Tools plugin) {
      this.plugin = plugin;
      this.rows = plugin.getGuiConfig().getInt("rows", 3);
      this.guiTitle = HexColorCode.translateAllColorCodes(plugin.getGuiConfig().getString("title", "&8ᴛᴏᴏʟꜱ"));
   }

   public void reloadGUI() {
      this.rows = this.plugin.getGuiConfig().getInt("rows", 3);
      this.guiTitle = HexColorCode.translateAllColorCodes(this.plugin.getGuiConfig().getString("title", "&8ᴛᴏᴏʟꜱ"));
   }

   public void openGUI(Player player) {
      int inventorySize = this.rows * 9;
      Inventory gui = Bukkit.createInventory(GUI_HOLDER, inventorySize, this.guiTitle);
      this.slotToToolMap.clear();
      if (this.plugin.getGuiConfig().getBoolean("fill.enabled", false)) {
         this.fillBackground(gui);
      }

      this.addToolsToGUI(gui);
      this.addCloseButton(gui);
      this.plugin.runGlobal(() -> player.openInventory(gui));
   }

   private void fillBackground(Inventory gui) {
      Material fillMaterial = Material.valueOf(this.plugin.getGuiConfig().getString("fill.material", "BLACK_STAINED_GLASS_PANE"));
      String fillDisplayName = HexColorCode.translateAllColorCodes(this.plugin.getGuiConfig().getString("fill.display-name", " "));
      ItemStack background = this.createItem(fillMaterial, fillDisplayName, new ArrayList());
      String slotsConfig = this.plugin.getGuiConfig().getString("fill.slots", "0-8,18-26");

      for(int slot : this.parseSlots(slotsConfig, gui.getSize())) {
         if (slot < gui.getSize()) {
            gui.setItem(slot, background);
         }
      }

   }

   private void addToolsToGUI(Inventory gui) {
      int drillSlot = this.plugin.getGuiConfig().getInt("Tools.Drill-slot", 11);
      if (drillSlot < gui.getSize()) {
         gui.setItem(drillSlot, this.plugin.getDrill().getItem());
         this.slotToToolMap.put(drillSlot, "drill");
      }

      int treeChopperSlot;
      if ((treeChopperSlot = this.plugin.getGuiConfig().getInt("Tools.treechopper-slot", 13)) < gui.getSize()) {
         gui.setItem(treeChopperSlot, this.plugin.getTreeChopper().getItem());
         this.slotToToolMap.put(treeChopperSlot, "treechopper");
      }

      int shovelSlot;
      if ((shovelSlot = this.plugin.getGuiConfig().getInt("Tools.shovel-slot", 15)) < gui.getSize()) {
         gui.setItem(shovelSlot, this.plugin.getShovel().getItem());
         this.slotToToolMap.put(shovelSlot, "shovel");
      }

      int hoeSlot;
      if ((hoeSlot = this.plugin.getGuiConfig().getInt("Tools.hoe-slot", 12)) < gui.getSize()) {
         gui.setItem(hoeSlot, this.plugin.getHoe().getItem());
         this.slotToToolMap.put(hoeSlot, "hoe");
      }

      int enderPearlSlot;
      if ((enderPearlSlot = this.plugin.getGuiConfig().getInt("Tools.enderpearl-slot", 14)) < gui.getSize()) {
         gui.setItem(enderPearlSlot, this.plugin.getEnderPearl().getItem());
         this.slotToToolMap.put(enderPearlSlot, "enderpearl");
      }

      int rocketSlot;
      if ((rocketSlot = this.plugin.getGuiConfig().getInt("Tools.rocket-slot", 16)) < gui.getSize()) {
         gui.setItem(rocketSlot, this.plugin.getRocket().getItem());
         this.slotToToolMap.put(rocketSlot, "rocket");
      }

      int waterBucketSlot;
      if ((waterBucketSlot = this.plugin.getGuiConfig().getInt("Tools.waterbucket-slot", 20)) < gui.getSize()) {
         gui.setItem(waterBucketSlot, this.plugin.getWaterBucket().getItem());
         this.slotToToolMap.put(waterBucketSlot, "waterbucket");
      }

      int lavaBucketSlot;
      if ((lavaBucketSlot = this.plugin.getGuiConfig().getInt("Tools.lavabucket-slot", 22)) < gui.getSize()) {
         gui.setItem(lavaBucketSlot, this.plugin.getLavaBucket().getItem());
         this.slotToToolMap.put(lavaBucketSlot, "lavabucket");
      }

   }

   private void addCloseButton(Inventory gui) {
      int closeSlot = this.plugin.getGuiConfig().getInt("Close-button.slot", 18);
      if (closeSlot < gui.getSize()) {
         Material closeMaterial = Material.valueOf(this.plugin.getGuiConfig().getString("Close-button.material", "RED_STAINED_GLASS_PANE"));
         String closeDisplayName = HexColorCode.translateAllColorCodes(this.plugin.getGuiConfig().getString("Close-button.display-name", "&#FF0000ᴄʟᴏꜱᴇ"));
         ArrayList<String> closeLore = new ArrayList();

         for(String loreLine : this.plugin.getGuiConfig().getStringList("Close-button.lore")) {
            closeLore.add(HexColorCode.translateAllColorCodes(loreLine));
         }

         ItemStack closeButton = this.createItem(closeMaterial, closeDisplayName, closeLore);
         gui.setItem(closeSlot, closeButton);
      }
   }

   private ItemStack createItem(Material material, String displayName, List<String> lore) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(displayName);
         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }

   private List<Integer> parseSlots(String slotsConfig, int maxSlots) {
      ArrayList<Integer> slots = new ArrayList();

      for(String part : slotsConfig.split(",")) {
         if (part.contains("-")) {
            String[] range = part.split("-");

            try {
               int start = Integer.parseInt(range[0].trim());
               int end = Integer.parseInt(range[1].trim());

               for(int i = start; i <= end; ++i) {
                  if (i < maxSlots) {
                     slots.add(i);
                  }
               }
            } catch (NumberFormatException var14) {
            }
         } else {
            try {
               int slot = Integer.parseInt(part.trim());
               if (slot < maxSlots) {
                  slots.add(slot);
               }
            } catch (NumberFormatException var13) {
            }
         }
      }

      return slots;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      HumanEntity humanEntity = event.getWhoClicked();
      if (humanEntity instanceof Player player) {
         Inventory inventory = event.getInventory();
         if (inventory.getHolder() instanceof GUIHolder) {
            event.setCancelled(true);
            int clickedSlot = event.getRawSlot();
            if (clickedSlot >= 0 && clickedSlot < inventory.getSize()) {
               ItemStack clickedItem = event.getCurrentItem();
               if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                  UUID playerId = player.getUniqueId();
                  long currentTime = System.currentTimeMillis();
                  if (!this.clickCooldown.containsKey(playerId) || currentTime - (Long)this.clickCooldown.get(playerId) >= 250L) {
                     this.clickCooldown.put(playerId, currentTime);
                     int closeSlot = this.plugin.getGuiConfig().getInt("Close-button.slot", 18);
                     if (clickedSlot == closeSlot) {
                        this.playSound(player, "sounds.close-sound", "ui.button.click");
                        this.plugin.runEntity(player, () -> player.closeInventory());
                     } else {
                        if (this.slotToToolMap.containsKey(clickedSlot)) {
                           String toolType = (String)this.slotToToolMap.get(clickedSlot);
                           this.giveTool(player, toolType);
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private void giveTool(Player player, String toolType) {
      String displayName;
      ItemStack toolItem;
      switch (toolType.toLowerCase()) {
         case "drill":
            toolItem = this.plugin.getDrill().getItemForPlayer(player);
            displayName = this.plugin.getDrill().getConfig().getString("name", "&#A20AD6ᴀᴍᴇᴛʜʏꜱᴛ ᴘɪᴄᴋᴀxᴇ");
            break;
         case "treechopper":
            toolItem = this.plugin.getTreeChopper().getItemForPlayer(player);
            displayName = this.plugin.getTreeChopper().getConfig().getString("name", "&#A20AD6ᴀᴍᴇᴛʏꜱᴛ ᴀxᴇ");
            break;
         case "shovel":
            toolItem = this.plugin.getShovel().getItemForPlayer(player);
            displayName = this.plugin.getShovel().getConfig().getString("name", "&#A20AD6ᴀᴍᴇᴛʏꜱᴛ ꜱʜᴏᴠᴇʟ");
            break;
         case "hoe":
            toolItem = this.plugin.getHoe().getItemForPlayer(player);
            displayName = this.plugin.getHoe().getConfig().getString("name", "&#A20AD6ᴀᴍᴇᴛʏꜱᴛ ʜᴏᴇ");
            break;
         case "enderpearl":
            toolItem = this.plugin.getEnderPearl().getItemForPlayer(player);
            displayName = this.plugin.getEnderPearl().getConfig().getString("name", "&#A20AD6ɪɴꜰɪɴɪᴛᴇ ᴇɴᴅᴇʀ ᴘᴇᴀʀʟ");
            break;
         case "rocket":
            toolItem = this.plugin.getRocket().getItemForPlayer(player);
            displayName = this.plugin.getRocket().getConfig().getString("name", "&#A20AD6ɪɴꜰɪɴɪᴛʏ ʀᴏᴄᴋᴇᴛ");
            break;
         case "waterbucket":
            toolItem = this.plugin.getWaterBucket().getItemForPlayer(player);
            displayName = this.plugin.getWaterBucket().getConfig().getString("name", "&#3B9EFFᴀᴍᴇᴛʜʏꜱᴛ ᴡᴀᴛᴇʀ ʙᴜᴄᴋᴇᴛ");
            break;
         case "lavabucket":
            toolItem = this.plugin.getLavaBucket().getItemForPlayer(player);
            displayName = this.plugin.getLavaBucket().getConfig().getString("name", "&#FF6600ᴀᴍᴇᴛʜʏꜱᴛ ʟᴀᴠᴀ ʙᴜᴄᴋᴇᴛ");
            break;
         default:
            return;
      }

      if (toolItem != null) {
         if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(this.plugin.getLang("inventory-full"));
            this.playSound(player, "sounds.inventory-full-sound", "entity.villager.no");
            return;
         }

         this.plugin.runEntity(player, () -> {
            player.getInventory().addItem(new ItemStack[]{toolItem.clone()});
            String translatedName = HexColorCode.translateAllColorCodes(displayName);
            player.sendMessage(this.plugin.getLang("tool-received", "tool_name", translatedName));
            this.playSound(player, "sounds.tool-given-sound", "entity.experience_orb.pickup");
         });
      }

   }

   private void playSound(Player player, String configPath, String defaultSound) {
      try {
         String soundName = this.plugin.getGuiConfig().getString(configPath, defaultSound);
         if (soundName != null && !soundName.isEmpty()) {
            String formattedSound = this.convertSoundName(soundName);
            this.plugin.runEntity(player, () -> player.playSound(player.getLocation(), formattedSound, 1.0F, 1.0F));
         } else {
            String formattedDefault = this.convertSoundName(defaultSound);
            this.plugin.runEntity(player, () -> player.playSound(player.getLocation(), formattedDefault, 1.0F, 1.0F));
         }
      } catch (Exception var7) {
         try {
            String formattedDefault = this.convertSoundName(defaultSound);
            this.plugin.runEntity(player, () -> player.playSound(player.getLocation(), formattedDefault, 1.0F, 1.0F));
         } catch (Exception var6) {
         }
      }

   }

   private String convertSoundName(String soundName) {
      if (soundName != null && !soundName.isEmpty()) {
         if (soundName.contains(".")) {
            return soundName.toLowerCase();
         } else {
            String converted = soundName.toLowerCase().replace("_", ".");
            if (!converted.startsWith("block.") && !converted.startsWith("entity.") && !converted.startsWith("ui.") && !converted.contains(".")) {
               converted = "entity." + converted;
            }

            return converted;
         }
      } else {
         return soundName;
      }
   }

   private static class GUIHolder implements InventoryHolder {
      public @NotNull Inventory getInventory() {
         return Bukkit.createInventory((InventoryHolder)null, 9);
      }
   }
}
