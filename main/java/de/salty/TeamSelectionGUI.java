package de.salty;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TeamSelectionGUI {

    private final GameManager gameManager;

    public TeamSelectionGUI(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void openInventory(Player player) {
        Inventory inventory = createTeamSelectionInventory(player);
        player.openInventory(inventory);
    }

    private Inventory createTeamSelectionInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Select Your Team");
        ItemStack redTeamItem = createTeamItem(Team.RED, Material.RED_WOOL, "§cRed Team");
        ItemStack blueTeamItem = createTeamItem(Team.BLUE, Material.BLUE_WOOL, "§bBlue Team");
        if (player.hasPermission("deathmatch.startgame")) {
            ItemStack startGameItem = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta startGameMeta = startGameItem.getItemMeta();
            startGameMeta.setDisplayName("§a§lStart Game");
            startGameItem.setItemMeta(startGameMeta);
            inventory.setItem(4, startGameItem);
        }
        inventory.setItem(2, redTeamItem);
        inventory.setItem(6, blueTeamItem);
        return inventory;
    }

    private ItemStack createTeamItem(Team team, Material material, String displayName) {
        ItemStack teamItem = new ItemStack(material);
        ItemMeta meta = teamItem.getItemMeta();
        meta.setDisplayName(displayName);
        List<String> lore = new ArrayList<>();
        lore.add("§7Players:");
        for (UUID playerId : gameManager.getPlayersInTeam(team)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                lore.add("§7" + player.getName());
            }
        }
        meta.setLore(lore);
        teamItem.setItemMeta(meta);

        return teamItem;
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Select Your Team")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();

        if (itemName.equals("§cRed Team")) {
            gameManager.assignTeam(player, Team.RED);
        } else if (itemName.equals("§bBlue Team")) {
            gameManager.assignTeam(player, Team.BLUE);
        } else if (itemName.equals("§a§lStart Game")) {
            if (gameManager.isGameRunning()) {
                player.sendMessage("§cThe Game is already running!");
                return;
            }
            if (gameManager.getTeamSize(Team.RED) < 1 || gameManager.getTeamSize(Team.BLUE) < 1) {
                player.sendMessage("§cBoth Teams need one Player in each team!");
                return;
            }


            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getOpenInventory().getTitle().equals("Select Your Team")) {
                    onlinePlayer.closeInventory();
                }
            }

            gameManager.startGame();
            player.sendMessage("§aThe game started!");
        }
        refreshOpenInventories();

    }

    public void refreshOpenInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().equals("Select Your Team")) {
                player.openInventory(createTeamSelectionInventory(player));
            }
        }
    }
}
