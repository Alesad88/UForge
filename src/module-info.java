module UForge {
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;
	requires java.prefs;

	// FIX: Explicitly export and open your application package to the controls layout layer
	exports application;
	opens application to javafx.graphics, javafx.fxml, javafx.controls;
}
