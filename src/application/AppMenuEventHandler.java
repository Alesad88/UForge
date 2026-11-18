package application;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AppMenuEventHandler implements EventHandler<ActionEvent> {

	private final String actionName;
	private final Stage stage;

	// CORE DATA LAYER: Tracks file mapping and unsaved text state arrays per individual tab row element
	private static final Map<String, File> workspaceFileMap = new HashMap<>();
	private static final Map<String, File> originalSourceFileMap = new HashMap<>();
	private static final Map<String, String> tabTextBufferMap = new HashMap<>();

	public AppMenuEventHandler(String actionName, Stage stage) {
		this.actionName = actionName;
		this.stage = stage;
	}

	@Override
	public void handle(ActionEvent event) {
		boolean needsActiveTab = "Save".equals(actionName) || "Save As".equals(actionName) || "Cut".equals(actionName)
				|| "Copy".equals(actionName) || "Paste".equals(actionName);

		Tab activeTab = TheStage.getWorkspaceTabPane().getSelectionModel().getSelectedItem();
		if (needsActiveTab && activeTab == null) {
			TheStage.logToConsole("[Menu Warning] Command blocked. No active workspace tabs open.");
			return;
		}

		// MULTI-TAB BUGFIX: Dynamically resolve the text area component from the selected custom WorkspaceTab
		TextArea textArea = null;
		if (activeTab instanceof WorkspaceTab) {
			textArea = ((WorkspaceTab) activeTab).getTextArea();
		}

		String currentTabTitle = (activeTab != null) ? activeTab.getText() : "";

		// Before running actions, preserve current keystrokes into our virtual active memory buffer
		if (activeTab != null && textArea != null) {
			tabTextBufferMap.put(currentTabTitle, textArea.getText());
		}

		switch (actionName) {
		case "New":
			String defaultName = "Untitled.json";
			tabTextBufferMap.put(defaultName, "");
			
			// Open with clean explicit parameters
			TheStage.openNewEditorTabWithContent(defaultName, "", "");
			TheStage.logToConsole("[Menu Action] Opened fresh empty workspace tab environment.");
			break;

		case "Open":
			FileChooser openChooser = new FileChooser();
			openChooser.setTitle("Open FModel Properties/JSON Data");
			openChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("UIAsset / JSON Properties Data (*.json, *.uiasset)", "*.json", "*.uiasset")
			);
			
			File fileToOpen = openChooser.showOpenDialog(stage);

			if (fileToOpen != null) {
				String content = StorageEngine.readFileToString(fileToOpen);
				File workspaceFile = StorageEngine.mirrorToWorkspaceFolder(fileToOpen);
				File activeTrackingFile = (workspaceFile != null) ? workspaceFile : fileToOpen;

				// 1. POPULATE THE DICTIONARIES FIRST BEFORE INITIALIZING THE UI TAB
				workspaceFileMap.put(fileToOpen.getName(), activeTrackingFile);
				originalSourceFileMap.put(fileToOpen.getName(), fileToOpen);
				tabTextBufferMap.put(fileToOpen.getName(), content);

				// 2. NOW PASS CONTENT SAFELY TO AVOID RACING PIPELINES
				TheStage.openNewEditorTabWithContent(fileToOpen.getName(), content, content);

				// FIXED: Explicitly force the global sidebar to parse and display the opened file's JSON node tree hierarchy
				javafx.scene.control.TreeView<String> globalTree = TheStage.getGlobalSidebarTree();
				if (globalTree != null) {
					javafx.scene.control.TreeItem<String> structuralRoot = JsonTreeParser.buildStructuralTree(content, fileToOpen.getName());
					globalTree.setRoot(structuralRoot);
				}

				// Pass the resolved workspace folder context dynamically into the dictionary index engine
				File targetDirectory = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();
				JsonDataEngine.initializeEngineOnBoot(targetDirectory);
			}
			break;

		case "Save":
			if (textArea == null) return;
			File activeWorkspaceFile = workspaceFileMap.get(currentTabTitle);
			File originalSourceFile = originalSourceFileMap.get(currentTabTitle);

			if (activeWorkspaceFile != null && originalSourceFile != null) {
				// 1. Write the central editor text content back out to your local storage path copy
				StorageEngine.writeStringToFile(activeWorkspaceFile, textArea.getText());
				
				// 2. Commit the staging modifications back into your main external source files
				StorageEngine.saveWorkspaceToOriginal(activeWorkspaceFile, originalSourceFile);
				
				// Clear any modification prefix trackers safely
				if (currentTabTitle.startsWith("*")) {
					String cleanName = currentTabTitle.substring(1);
					updateTabMetaTracking(currentTabTitle, cleanName, activeWorkspaceFile, originalSourceFile);
				}
				TheStage.logToConsole("[System] Changes committed to source file: " + originalSourceFile.getName());
			} else {
				executeSaveAsDialog(textArea, currentTabTitle);
			}
			break;

		case "Save As":
			if (textArea == null) return;
			executeSaveAsDialog(textArea, currentTabTitle);
			break;

		case "Cut":   if (textArea != null) textArea.cut(); break;
		case "Copy":  if (textArea != null) textArea.copy(); break;
		case "Paste": if (textArea != null) textArea.paste(); break;

		case "Manage Storage":
			WorkspaceStorageManager.displayMaintenanceDashboard(stage);
			break;

		default:
			System.out.println("[Menu Warning] Unhandled event profile layout: " + actionName);
			break;
		}
	}

	private void executeSaveAsDialog(TextArea textArea, String currentTabTitle) {
		FileChooser saveChooser = new FileChooser();
		saveChooser.setTitle("Export FModel Properties Data");
		saveChooser.getExtensionFilters().addAll(
			new FileChooser.ExtensionFilter("UIAsset / JSON Properties Data (*.json, *.uiasset)", "*.json", "*.uiasset"),
			new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
		);

		File activeFile = workspaceFileMap.get(currentTabTitle);
		if (activeFile != null) {
			saveChooser.setInitialFileName(activeFile.getName());
		} else {
			saveChooser.setInitialFileName(currentTabTitle.replace("*", ""));
		}

		File fileToSave = saveChooser.showSaveDialog(stage);
		if (fileToSave != null) {
			// Write the data payload using standard utility blocks
			StorageEngine.writeStringToFile(fileToSave, textArea.getText());
			
			// Map out reference structures for the newly exported workspace target
			updateTabMetaTracking(currentTabTitle, fileToSave.getName(), fileToSave, fileToSave);
			TheStage.logToConsole("[System] Exported data layout configuration down to: " + fileToSave.getName());
		}
	}

	private void updateTabMetaTracking(String oldTitle, String newTitle, File workspaceFile, File sourceFile) {
		String textBuffer = tabTextBufferMap.remove(oldTitle);
		workspaceFileMap.remove(oldTitle);
		originalSourceFileMap.remove(oldTitle);

		workspaceFileMap.put(newTitle, workspaceFile);
		originalSourceFileMap.put(newTitle, sourceFile);
		tabTextBufferMap.put(newTitle, textBuffer);

		Tab selectedTab = TheStage.getWorkspaceTabPane().getSelectionModel().getSelectedItem();
		if (selectedTab != null) {
			selectedTab.setText(newTitle);
		}
	}
	
	/**
	 * Synchronously flushes all virtual memory mappings when the local mirror directory is purged.
	 */
	public static void clearGlobalStateTrackingBuffers() {
	    workspaceFileMap.clear();
	    originalSourceFileMap.clear();
	    tabTextBufferMap.clear();
	    TheStage.logToConsole("[Memory Track] Virtual active memory tracking buffers wiped cleanly.");
	}

	// Dynamic Accessors allowing your central TabPane selection listeners inside TheStage.java 
	// to swap files smoothly when clicking across tabs
	public static Map<String, String> getTabTextBufferMap() { return tabTextBufferMap; }
	public static Map<String, File> getWorkspaceFileMap() { return workspaceFileMap; }
	public static Map<String, File> getOriginalSourceFileMap() { return originalSourceFileMap; }
}
