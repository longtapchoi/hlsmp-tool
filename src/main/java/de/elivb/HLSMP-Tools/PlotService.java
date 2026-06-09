package de.elivb.donutTools;

import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlotService {
   private final Tools plugin;
   private Boolean plotSquaredEnabled = null;
   private Object plotSquaredInstance = null;
   private Method getPlotAreaManagerMethod = null;
   private Method getPlotAreaMethod = null;
   private Method getPlotAtMethod = null;
   private Method isOwnerMethod = null;
   private Method isAddedMethod = null;
   private Method getOwnerMethod = null;
   private Method wrapPlayerMethod = null;
   private Method getUUIDMethod = null;

   public PlotService(Tools plugin) {
      this.plugin = plugin;
   }

   public boolean canBuild(Player player, Location location) {
      if (this.plotSquaredEnabled == null) {
         this.checkPlotSquared();
      }

      if (!this.plotSquaredEnabled) {
         return true;
      } else {
         try {
            return this.checkPlotBuild(player, location);
         } catch (Throwable var4) {
            this.plotSquaredEnabled = false;
            return true;
         }
      }
   }

   private void checkPlotSquared() {
      try {
         Plugin plotPlugin = this.plugin.getServer().getPluginManager().getPlugin("PlotSquared");
         this.plotSquaredEnabled = plotPlugin != null && plotPlugin.isEnabled();
         if (this.plotSquaredEnabled) {
            this.initReflectionMethods();
         }
      } catch (Throwable var2) {
         this.plotSquaredEnabled = false;
      }

   }

   private void initReflectionMethods() {
      try {
         Class<?> plotSquaredClass = Class.forName("com.plotsquared.core.PlotSquared");
         Method platformMethod = plotSquaredClass.getMethod("platform");
         Object platform = platformMethod.invoke((Object)null);
         this.getPlotAreaManagerMethod = platform.getClass().getMethod("getPlotAreaManager");
         Object plotAreaManager = this.getPlotAreaManagerMethod.invoke(platform);
         Class<?> plotAreaManagerClass = plotAreaManager.getClass();

         try {
            this.getPlotAreaMethod = plotAreaManagerClass.getMethod("getPlotArea", Location.class);
         } catch (NoSuchMethodException var16) {
            try {
               this.getPlotAreaMethod = plotAreaManagerClass.getMethod("getPlotArea", String.class, Integer.TYPE, Integer.TYPE);
            } catch (NoSuchMethodException var15) {
            }
         }

         Class<?> plotClass = Class.forName("com.plotsquared.core.plot.Plot");
         Class<?> plotAreaClass = Class.forName("com.plotsquared.core.plot.PlotArea");

         try {
            this.getPlotAtMethod = plotAreaClass.getMethod("getPlotAt", Location.class);
         } catch (NoSuchMethodException var14) {
            try {
               this.getPlotAtMethod = plotAreaClass.getMethod("getPlot", Location.class);
            } catch (NoSuchMethodException var13) {
            }
         }

         this.isOwnerMethod = plotClass.getMethod("isOwner", UUID.class);
         this.isAddedMethod = plotClass.getMethod("isAdded", UUID.class);
         this.getOwnerMethod = plotClass.getMethod("getOwner");
         Class<?> plotPlayerClass = Class.forName("com.plotsquared.core.player.PlotPlayer");

         try {
            this.wrapPlayerMethod = plotPlayerClass.getMethod("wrap", Player.class);
         } catch (NoSuchMethodException var12) {
            try {
               this.wrapPlayerMethod = plotPlayerClass.getMethod("from", Player.class);
            } catch (NoSuchMethodException var11) {
            }
         }

         this.getUUIDMethod = plotPlayerClass.getMethod("getUUID");
      } catch (Throwable var17) {
         this.plotSquaredEnabled = false;
      }

   }

   private boolean checkPlotBuild(Player player, Location location) {
      try {
         Object plotPlayer = this.wrapPlayerMethod.invoke((Object)null, player);
         UUID playerUUID = (UUID)this.getUUIDMethod.invoke(plotPlayer);
         Object plotSquared = Class.forName("com.plotsquared.core.PlotSquared").getMethod("platform").invoke((Object)null);
         Object plotAreaManager = this.getPlotAreaManagerMethod.invoke(plotSquared);
         Object plotArea = null;
         if (this.getPlotAreaMethod.getParameterCount() == 1) {
            plotArea = this.getPlotAreaMethod.invoke(plotAreaManager, location);
         } else {
            plotArea = this.getPlotAreaMethod.invoke(plotAreaManager, location.getWorld().getName(), location.getBlockX(), location.getBlockZ());
         }

         if (plotArea == null) {
            return true;
         } else {
            Object plot = this.getPlotAtMethod.invoke(plotArea, location);
            if (plot == null) {
               return true;
            } else {
               boolean isOwner = (Boolean)this.isOwnerMethod.invoke(plot, playerUUID);
               if (isOwner) {
                  return true;
               } else {
                  boolean isAdded = (Boolean)this.isAddedMethod.invoke(plot, playerUUID);
                  if (isAdded) {
                     return true;
                  } else {
                     return player.hasPermission("plots.admin.build");
                  }
               }
            }
         }
      } catch (Throwable var11) {
         return true;
      }
   }

   public boolean isInPlot(Location location) {
      if (this.plotSquaredEnabled == null) {
         this.checkPlotSquared();
      }

      if (!this.plotSquaredEnabled) {
         return false;
      } else {
         try {
            Object plotSquared = Class.forName("com.plotsquared.core.PlotSquared").getMethod("platform").invoke((Object)null);
            Object plotAreaManager = this.getPlotAreaManagerMethod.invoke(plotSquared);
            Object plotArea = null;
            if (this.getPlotAreaMethod.getParameterCount() == 1) {
               plotArea = this.getPlotAreaMethod.invoke(plotAreaManager, location);
            } else {
               plotArea = this.getPlotAreaMethod.invoke(plotAreaManager, location.getWorld().getName(), location.getBlockX(), location.getBlockZ());
            }

            if (plotArea == null) {
               return false;
            } else {
               Object plot = this.getPlotAtMethod.invoke(plotArea, location);
               return plot != null;
            }
         } catch (Throwable var6) {
            return false;
         }
      }
   }

   public UUID getPlotOwner(Location location) {
      if (this.plotSquaredEnabled == null) {
         this.checkPlotSquared();
      }

      if (!this.plotSquaredEnabled) {
         return null;
      } else {
         try {
            Object plotSquared = Class.forName("com.plotsquared.core.PlotSquared").getMethod("platform").invoke((Object)null);
            Object plotAreaManager = this.getPlotAreaManagerMethod.invoke(plotSquared);
            Object plotArea = null;
            if (this.getPlotAreaMethod.getParameterCount() == 1) {
               plotArea = this.getPlotAreaMethod.invoke(plotAreaManager, location);
            } else {
               plotArea = this.getPlotAreaMethod.invoke(plotAreaManager, location.getWorld().getName(), location.getBlockX(), location.getBlockZ());
            }

            if (plotArea == null) {
               return null;
            } else {
               Object plot = this.getPlotAtMethod.invoke(plotArea, location);
               if (plot == null) {
                  return null;
               } else {
                  Object ownerObj = this.getOwnerMethod.invoke(plot);
                  if (ownerObj == null) {
                     return null;
                  } else {
                     return ownerObj instanceof UUID ? (UUID)ownerObj : null;
                  }
               }
            }
         } catch (Throwable var7) {
            return null;
         }
      }
   }

   public boolean isPlotSquaredEnabled() {
      if (this.plotSquaredEnabled == null) {
         this.checkPlotSquared();
      }

      return this.plotSquaredEnabled;
   }
}
