package de.salty;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final TeamSelectionGUI teamSelectionGUI;

    public InventoryClickListener(GameManager gameManager) {
        this.teamSelectionGUI = new TeamSelectionGUI(gameManager);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        teamSelectionGUI.handleInventoryClick(event);
    }
}
