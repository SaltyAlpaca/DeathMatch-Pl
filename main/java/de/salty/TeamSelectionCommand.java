package de.salty;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamSelectionCommand implements CommandExecutor {

    private final TeamSelectionGUI teamSelectionGUI;
    private final GameManager gameManager;

    public TeamSelectionCommand(GameManager gameManager) {
        this.teamSelectionGUI = new TeamSelectionGUI(gameManager);
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (gameManager.isGameRunning()) {
                player.sendMessage("§cYou cannot change teams after the game has started!");
                return true;
            }
            teamSelectionGUI.openInventory(player);
            return true;
        }
        return false;
    }
}
