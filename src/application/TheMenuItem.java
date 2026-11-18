package application;

import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TheMenuItem {

	// Unified File item list maintaining one clean single entry point for Manage Storage
	private String[] fileMenuItemNames = { "New", "Open", "Save", "Save As", "Manage Storage" };
	private String[] editMenuItemNames = { "Cut", "Copy", "Paste" };

	public TheMenuItem() {
	}

	@SuppressWarnings("exports")
	public List<MenuItem> buildMenuFileItem(Stage stage) {
		List<MenuItem> itemsList = new ArrayList<>();
		
		// Dark-theme styling for the text within menu drops
		String itemStyle = "-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px;";

		for (String itemName : fileMenuItemNames) {
			MenuItem item = new MenuItem(itemName);
			item.setStyle(itemStyle);
			
			// Map standard OS-level hotkeys for workspace ergonomics
			switch (itemName) {
				case "New":             item.setAccelerator(KeyCombination.keyCombination("Shortcut+N")); break;
				case "Open":            item.setAccelerator(KeyCombination.keyCombination("Shortcut+O")); break;
				case "Save":            item.setAccelerator(KeyCombination.keyCombination("Shortcut+S")); break;
				case "Save As":         item.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S")); break;
				case "Manage Storage":  item.setAccelerator(KeyCombination.keyCombination("Shortcut+Alt+M")); break;
			}
			
			item.setOnAction(new AppMenuEventHandler(itemName, stage));
			itemsList.add(item);
		}
		return itemsList;
	}

	@SuppressWarnings("exports")
	public List<MenuItem> buildMenuEditItem(Stage stage) {
		List<MenuItem> itemsList = new ArrayList<>();
		String itemStyle = "-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px;";

		for (String itemName : editMenuItemNames) {
			MenuItem item = new MenuItem(itemName);
			item.setStyle(itemStyle);
			
			// Map traditional text-manipulation clipboard bindings
			switch (itemName) {
				case "Cut":   item.setAccelerator(KeyCombination.keyCombination("Shortcut+X")); break;
				case "Copy":  item.setAccelerator(KeyCombination.keyCombination("Shortcut+C")); break;
				case "Paste": item.setAccelerator(KeyCombination.keyCombination("Shortcut+V")); break;
			}
			
			item.setOnAction(new AppMenuEventHandler(itemName, stage));
			itemsList.add(item);
		}
		return itemsList;
	}

	/**
	 * FIXED LINK: Constructs clean dropdown actions for the structural Help menu navigation header.
	 * Duplicate Workspace Storage Status items have been completely scrubbed from this layout tier.
	 */
	@SuppressWarnings("exports")
	public List<MenuItem> buildMenuHelpItem(Stage stage) {
		List<MenuItem> itemsList = new ArrayList<>();
		String itemStyle = "-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px;";

		// Structural App Info item mapping
		MenuItem aboutItem = new MenuItem("About UForge");
		aboutItem.setStyle(itemStyle);
		aboutItem.setOnAction(e -> {
			TheStage.logToConsole("[System Help] UForge Modding Tool Workspace - Platform Runtime Engine Version 1.0.0");
		});
		itemsList.add(aboutItem);

		return itemsList;
	}
}
