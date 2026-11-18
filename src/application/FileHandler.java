package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FileHandler {

	@SuppressWarnings("exports")
	public static File openFileInterface(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Text Document");

		// Set standard extension configurations so users filter through valid items
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files (*.txt)", "*.txt"),
				new ExtensionFilter("All Files", "*.*"));

		// This triggers the actual OS native file selector window UI
		return fileChooser.showOpenDialog(stage);
	}

	@SuppressWarnings("exports")
	public static File saveAsFileInterface(Stage stage, File currentFile) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Document As");

		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files (*.txt)", "*.txt"),
				new ExtensionFilter("All Files", "*.*"));

		// Set a smart default filename input target value
		if (currentFile != null) {
			fileChooser.setInitialFileName(currentFile.getName());
		} else {
			fileChooser.setInitialFileName("untitled.txt");
		}

		// This triggers the actual native file location creator window UI
		return fileChooser.showSaveDialog(stage);
	}

	// High performance background stream IO layer (NIO)
	public static void writeToDisk(File file, String content) {
		try {
			Files.writeString(file.toPath(), content);
		} catch (IOException e) {
			System.err.println("Failed writing text payload onto disk storage: " + e.getMessage());
		}
	}
}
