package application;

import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

// Extending MenuBar makes this object a usable Node in BorderPane right away
public class TheMenuBar extends MenuBar {

	public TheMenuBar() {
		// Default constructor matches your instantiation statement
	}

	@SuppressWarnings("exports")
	public void initMenbar(Stage stage) {
		TheMenu menuProvider = new TheMenu();
		
		// Clear existing items if called multiple times, then build
		this.getMenus().clear();
		this.getMenus().addAll(menuProvider.buildMenuBar(stage));
	}
}
