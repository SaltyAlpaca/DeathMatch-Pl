package de.salty;

import org.bukkit.plugin.java.JavaPlugin;

public final class DeathMatch extends JavaPlugin {
    private static DeathMatch instance;


    @Override
    public void onEnable() {
        instance = this;
        GameManager gameManager = new GameManager();
        getCommand("startgame").setExecutor(new StartGameCommand(gameManager));
        getCommand("selectteam").setExecutor(new TeamSelectionCommand(gameManager));
        getServer().getPluginManager().registerEvents(new PlayerListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(gameManager), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static DeathMatch getInstance() {
        return instance;
    }


}
