package de.salty;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {

    private final GameManager gameManager;
    private Scoreboard scoreboard;
    private Objective objective;

    public ScoreboardManager(GameManager gameManager) {
        this.gameManager = gameManager;
        setupScoreboard();
    }

    private void setupScoreboard() {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            scoreboard = manager.getNewScoreboard();

            objective = scoreboard.registerNewObjective("game", "dummy", ChatColor.BOLD + "Death Match");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            updateScoreboard(0);
        } else {
            Bukkit.getLogger().warning("Failed to get ScoreboardManager!");
        }
    }

    public void updateScoreboard(int timeLeft) {
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        Score timeScore = objective.getScore(ChatColor.GREEN + "Time Left: " + ChatColor.WHITE + timeLeft);
        timeScore.setScore(3);

        int redTeamKills = gameManager.getTeamKills(Team.RED);
        int blueTeamKills = gameManager.getTeamKills(Team.BLUE);

        Score redTeamScore = objective.getScore(ChatColor.RED + "Red Team Kills: " + ChatColor.WHITE + redTeamKills);
        redTeamScore.setScore(2);

        Score blueTeamScore = objective.getScore(ChatColor.BLUE + "Blue Team Kills: " + ChatColor.WHITE + blueTeamKills);
        blueTeamScore.setScore(1);
    }

    public void showScoreboard(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void hideScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}