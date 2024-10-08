package de.salty;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final Map<UUID, Team> playerTeams = new HashMap<>();
    private final Map<Team, Integer> teamSizes = new HashMap<>();
    private final Map<Team, Integer> teamKills = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private boolean gameRunning = false;
    private final int gameTime = 300;
    private static final int MAX_PLAYERS_PER_TEAM = 2;
    private final ScoreboardManager scoreboardManager;

    public GameManager() {
        for (Team team : Team.values()) {
            teamSizes.put(team, 0);
            teamKills.put(team, 0);
        }
        this.scoreboardManager = new ScoreboardManager(this);
        Bukkit.getLogger().info("GameManager initialized.");
    }

    public void startGame() {
        if (gameRunning) return;
        for (Team team : Team.values()) {
            teamKills.put(team, 0);
        }
        Bukkit.getLogger().info("Starting game with players: " + playerTeams.keySet());

        if (playerTeams.isEmpty()) {
            Bukkit.getLogger().warning("No players assigned to teams!");
            return;
        }

        gameRunning = true;
        equipPlayers();

        teleportPlayersToArena();

        new BukkitRunnable() {
            int timeLeft = gameTime;

            @Override
            public void run() {
                if (timeLeft <= 0 || !gameRunning) {
                    endGame();
                    cancel();
                    return;
                }
                scoreboardManager.updateScoreboard(timeLeft);
                timeLeft--;
            }
        }.runTaskTimer(DeathMatch.getInstance(), 0, 20);

        for (UUID playerId : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                scoreboardManager.showScoreboard(player);
                Bukkit.getLogger().info("Showing scoreboard to player: " + player.getName());
            } else {
                Bukkit.getLogger().warning("Player not found for UUID: " + playerId);
            }
        }
    }

    public void endGame() {
        if (!gameRunning) return;
        gameRunning = false;

        Team winningTeam = determineWinningTeam();
        if (winningTeam != null) {
            String winMessage = "§a§l" + winningTeam.name() + " Team wins!";
            Bukkit.broadcastMessage(winMessage);
        } else {
            String drawMessage = "§c§lThe game ended in a draw! :(";
            Bukkit.broadcastMessage(drawMessage);
        }

        for (UUID playerId : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                player.teleport(player.getWorld().getSpawnLocation());

                scoreboardManager.hideScoreboard(player);

                ItemStack[] items = getSavedInventory(playerId);
                if (items != null) {
                    player.getInventory().setContents(items);
                    removeSavedInventory(playerId);
                } else {
                    Bukkit.getLogger().warning("No inventory data found for player: " + player.getName());
                }
            }
        }
    }

    public void assignTeam(Player player, Team newTeam) {
        UUID playerId = player.getUniqueId();
        Team currentTeam = playerTeams.get(playerId);

        if (currentTeam != null && currentTeam == newTeam) {
            player.sendMessage("§cYou are already on the " + newTeam.name() + " Team!");
            return;
        }

        if (teamSizes.get(newTeam) >= MAX_PLAYERS_PER_TEAM) {
            player.sendMessage("§cThe " + newTeam.name() + " Team is full!");
            return;
        }

        if (currentTeam != null) {
            teamSizes.put(currentTeam, teamSizes.get(currentTeam) - 1);
            player.sendMessage("§aYou have switched from the " + currentTeam.name() + " Team to the " + newTeam.name() + " Team.");
        } else {
            player.sendMessage("§aYou have joined the " + newTeam.name() + " Team!");
        }

        playerTeams.put(playerId, newTeam);
        teamSizes.put(newTeam, teamSizes.get(newTeam) + 1);

        Bukkit.getLogger().info("Player " + player.getName() + " assigned to " + newTeam.name() + " Team.");
    }

    public void checkForGameEnd() {
        int redAlive = 0;
        int blueAlive = 0;

        for (UUID playerId : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.getGameMode() != GameMode.SPECTATOR) {
                if (playerTeams.get(playerId) == Team.RED) {
                    redAlive++;
                } else if (playerTeams.get(playerId) == Team.BLUE) {
                    blueAlive++;
                }
            }
        }

        Bukkit.getLogger().info("Red Team Alive: " + redAlive);
        Bukkit.getLogger().info("Blue Team Alive: " + blueAlive);

        if (redAlive == 0) {
            endGame();
        } else if (blueAlive == 0) {
            endGame();
        }
    }

    private void equipPlayers() {
        Bukkit.getLogger().info("Equipping players...");
        for (UUID playerId : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                savedInventories.put(playerId, player.getInventory().getContents());

                PlayerInventory inventory = player.getInventory();
                inventory.clear();
                inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
                inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
                inventory.addItem(new ItemStack(Material.DIAMOND_SWORD));
                inventory.addItem(new ItemStack(Material.GOLDEN_APPLE, 5));
            }
        }
        Bukkit.getLogger().info("Players equipped.");
    }

    private void teleportPlayersToArena() {
        Bukkit.getLogger().info("Teleporting players to arena...");
        World overworld = Bukkit.getWorld("world");
        if (overworld == null) {
            Bukkit.getLogger().warning("Overworld not found!");
            return;
        }

        Location redTeamLocation = new Location(overworld, 20, 65, 20);
        Location blueTeamLocation = new Location(overworld, -20, 65, -20);

        for (UUID playerId : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                Location targetLocation = (playerTeams.get(playerId) == Team.RED) ? redTeamLocation : blueTeamLocation;

                Location safeLocation = findSafeLocation(targetLocation);
                if (safeLocation != null) {
                    player.teleport(safeLocation);
                    Bukkit.getLogger().info("Player " + player.getName() + " teleported to a safe location.");
                } else {
                    player.teleport(targetLocation);
                    Bukkit.getLogger().warning("Couldn't find a safe location, teleporting " + player.getName() + " to default.");
                }
            }
        }
        Bukkit.getLogger().info("Players teleported to arena.");
    }


    public boolean isGameRunning() {
        return gameRunning;
    }

    public ItemStack[] getSavedInventory(UUID playerId) {
        ItemStack[] inventory = savedInventories.get(playerId);
        return Objects.requireNonNullElseGet(inventory, () -> new ItemStack[0]);
    }

    public void removeSavedInventory(UUID playerId) {
        savedInventories.remove(playerId);
    }

    public void removePlayerFromGame(UUID playerId) {
        Team team = playerTeams.remove(playerId);
        if (team != null) {
            teamSizes.put(team, teamSizes.get(team) - 1);
        }
    }

    public int getTeamSize(Team team) {
        return teamSizes.getOrDefault(team, 0);
    }

    public List<UUID> getPlayersInTeam(Team team) {
        List<UUID> playersInTeam = new ArrayList<>();
        for (Map.Entry<UUID, Team> entry : playerTeams.entrySet()) {
            if (entry.getValue() == team) {
                playersInTeam.add(entry.getKey());
            }
        }
        return playersInTeam;
    }

    public int getTeamKills(Team team) {
        return teamKills.getOrDefault(team, 0);
    }
    public Team getTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }
    public void incrementTeamKills(Team team) {
        int currentKills = teamKills.getOrDefault(team, 0);
        teamKills.put(team, currentKills + 1);
    }
    private Team determineWinningTeam() {
        int redKills = teamKills.get(Team.RED);
        int blueKills = teamKills.get(Team.BLUE);

        int redTeamSize = teamSizes.get(Team.RED);
        int blueTeamSize = teamSizes.get(Team.BLUE);

        double redKillRatio = redTeamSize > 0 ? (double) redKills / redTeamSize : 0;
        double blueKillRatio = blueTeamSize > 0 ? (double) blueKills / blueTeamSize : 0;

        Bukkit.getLogger().info("Red Team Kill Ratio: " + redKillRatio);
        Bukkit.getLogger().info("Blue Team Kill Ratio: " + blueKillRatio);

        if (redKillRatio > blueKillRatio) {
            return Team.RED;
        } else if (blueKillRatio > redKillRatio) {
            return Team.BLUE;
        } else {
            return null;
        }
    }
    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return false;

        Location feet = location.clone();
        Location head = feet.clone().add(0, 1, 0);
        Location ground = feet.clone().add(0, -1, 0);

        Material feetMaterial = world.getBlockAt(feet).getType();
        Material headMaterial = world.getBlockAt(head).getType();
        Material groundMaterial = world.getBlockAt(ground).getType();

        return groundMaterial.isSolid() &&
                isNonSolidBlock(feetMaterial) &&
                isNonSolidBlock(headMaterial);
    }
    private Location findSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        for (int y = 0; y < 10; y++) {
            Location checkLocation = location.clone().add(0, y, 0);
            if (isSafeLocation(checkLocation)) {
                return checkLocation;
            }
        }

        for (int y = 0; y > -10; y--) {
            Location checkLocation = location.clone().add(0, y, 0);
            if (isSafeLocation(checkLocation)) {
                return checkLocation;
            }
        }

        return null;
    }

    private boolean isNonSolidBlock(Material material) {
        return material == Material.AIR ||
                material == Material.LADDER ||
                material == Material.VINE ||
                material == Material.SNOW ||
                material == Material.TALL_GRASS ||
                material == Material.FERN ||
                material == Material.SEAGRASS;
    }


}
