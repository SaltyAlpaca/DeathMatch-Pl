package de.salty;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartGameCommand implements CommandExecutor {

    private final GameManager gameManager;

    public StartGameCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (gameManager.isGameRunning()) {
                player.sendMessage("§cThe Game is already running!");
                return true;
            }

            if (gameManager.getTeamSize(Team.RED) < 1 || gameManager.getTeamSize(Team.BLUE) < 1) {
                player.sendMessage("§cBoth Teams need one Player in each team!");
                return true;
            }

            gameManager.startGame();
            player.sendMessage("§aThe game started!");
            return true;
        }
        return false;
    }
}
