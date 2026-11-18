package application;

import javafx.scene.control.Menu;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TheMenu {
	// Cleaned array keeping your native structural navigation groups intact
	private final String[] menuNames = { "File", "Edit", "Help" };

	public TheMenu() {
	}

	@SuppressWarnings("exports")
	public List<Menu> buildMenuBar(Stage stage) {
		List<Menu> menuList = new ArrayList<>();
		TheMenuItem menuItemProvider = new TheMenuItem();

		// Universal CSS theme rules for the top-level menu buttons and their dropdown popups
		String menuStyle = 
			"-fx-text-fill: #e0e0e0; " +
			"-fx-font-family: 'Segoe UI', Helvetica, sans-serif; " +
			"-fx-font-size: 13px; " +
			"-fx-padding: 5px 10px;";

		for (String name : menuNames) {
			Menu menu = new Menu(name);
			
			// Apply text color and layout rules
			menu.setStyle(menuStyle);

			if (name.equals("File")) {
				menu.getItems().addAll(menuItemProvider.buildMenuFileItem(stage));
			} else if (name.equals("Edit")) {
				menu.getItems().addAll(menuItemProvider.buildMenuEditItem(stage));
			} else if (name.equals("Help")) {
				// INTERFACE ALIGNMENT FIX: Wire the help item generator to the menuItemProvider
				menu.getItems().addAll(menuItemProvider.buildMenuHelpItem(stage));
			}

			menuList.add(menu);
		}

		return menuList;
	}
}
