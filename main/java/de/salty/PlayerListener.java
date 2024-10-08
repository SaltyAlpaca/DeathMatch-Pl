package de.salty;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final GameManager gameManager;
    private final Set<UUID> playersInGame = new HashSet<>();

    public PlayerListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (gameManager.isGameRunning() && gameManager.getTeam(damager) == gameManager.getTeam(victim)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        if (gameManager.isGameRunning() && gameManager.getTeam(victim) != null) {
            victim.setGameMode(GameMode.SPECTATOR);

            Team victimTeam = gameManager.getTeam(victim);

            if (victim.getKiller() != null) {
                Player killer = victim.getKiller();
                Team killerTeam = gameManager.getTeam(killer);

                if (killerTeam != null && gameManager.getTeam(killer) != null) {
                    gameManager.incrementTeamKills(killerTeam);
                    Bukkit.getLogger().info(killer.getName() + " from " + killerTeam.name() + " Team made a kill!");
                }
            } else {
                Team oppositeTeam = (victimTeam == Team.RED) ? Team.BLUE : Team.RED;
                gameManager.incrementTeamKills(oppositeTeam);
                Bukkit.getLogger().info("Player " + victim.getName() + " died from non-player cause, crediting kill to " + oppositeTeam.name() + " Team.");
            }

            gameManager.checkForGameEnd();
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (gameManager.isGameRunning()) {
            ItemStack[] originalInventory = gameManager.getSavedInventory(playerId);
            if (originalInventory != null) {
                player.getInventory().setContents(originalInventory);
                gameManager.removeSavedInventory(playerId);
            }
            gameManager.removePlayerFromGame(playerId);
            gameManager.checkForGameEnd();
            playersInGame.add(playerId);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (playersInGame.contains(playerId)) {
            World world = Bukkit.getWorld("world");
            if (world != null) {
                Location spawnLocation = world.getSpawnLocation();
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(spawnLocation);
            } else {
                Bukkit.getLogger().warning("World not found!");
            }
            playersInGame.remove(playerId);
        }
    }

}
