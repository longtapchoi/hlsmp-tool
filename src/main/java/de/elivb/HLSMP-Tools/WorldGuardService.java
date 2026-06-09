package de.elivb.donutTools;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardService {
   private final Tools plugin;
   private Boolean worldGuardEnabled = null;

   public WorldGuardService(Tools plugin) {
      this.plugin = plugin;
   }

   public boolean canBuild(Player player, Location location) {
      if (this.worldGuardEnabled == null) {
         this.checkWorldGuard();
      }

      if (!this.worldGuardEnabled) {
         return true;
      } else {
         try {
            return this.checkWorldGuardBuild(player, location);
         } catch (Throwable var4) {
            this.worldGuardEnabled = false;
            return true;
         }
      }
   }

   private void checkWorldGuard() {
      try {
         this.worldGuardEnabled = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
         if (this.worldGuardEnabled) {
         }
      } catch (Throwable var2) {
         this.worldGuardEnabled = false;
      }

   }

   private boolean checkWorldGuardBuild(Player player, Location location) {
      try {
         World world = BukkitAdapter.adapt(location.getWorld());
         RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
         ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
         return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(WorldGuardPlugin.inst().wrapPlayer(player), world) ? true : set.testState(WorldGuardPlugin.inst().wrapPlayer(player), new StateFlag[]{Flags.BUILD});
      } catch (Throwable t) {
         throw t;
      }
   }

   public boolean isWorldGuardEnabled() {
      if (this.worldGuardEnabled == null) {
         this.checkWorldGuard();
      }

      return this.worldGuardEnabled;
   }
}
