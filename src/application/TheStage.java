package application;

import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TheStage extends Application {
	private final TheMenuBar menuBar = new TheMenuBar();
	private static final TabPane workspaceTabPane = new TabPane();
	private static final TreeView<String> globalSidebarTree = new TreeView<>();
	private final BorderPane mainLayoutRoot = new BorderPane();

	// Instantiated once globally at layout root level
	private static final WorkspaceConsole globalConsole = new WorkspaceConsole();

	public static TabPane getWorkspaceTabPane() {
		return workspaceTabPane;
	}

	public static TreeView<String> getGlobalSidebarTree() {
		return globalSidebarTree;
	}

	// FIXED HELPER: Pulls the text area from whichever tab is currently highlighted
	// on screen
	public static TextArea getCentralCodeEditor() {
		Tab activeTab = workspaceTabPane.getSelectionModel().getSelectedItem();
		if (activeTab instanceof WorkspaceTab) {
			return ((WorkspaceTab) activeTab).getTextArea();
		}
		return null;
	}

	public static void logToConsole(String text) {
		globalConsole.appendLog(text);
	}

	@SuppressWarnings("exports")
	@Override
	public void start(Stage primaryStage) {
		System.out.println("[App Boot] Initializing workspace configuration rules...");

		// DYNAMIC ENVIRONMENT RESOLUTION: Determine safe cross-PC directory paths on
		// initialization.
		File initializedWorkspacePath = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();

		// Initialize the structural parsing indexing dictionaries using the verified
		// directory node context.
		JsonDataEngine.initializeEngineOnBoot(initializedWorkspacePath);

		// Configure TabPane to occupy the workspace region cleanly
		workspaceTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

		globalSidebarTree.setPrefWidth(210);
		globalSidebarTree.setMaxWidth(300);
		globalSidebarTree.setShowRoot(true); // Ensure the folder root displays cleanly

		// POPULATE WORKSPACE ON BOOT: Map directory structure into the far-left pane
		// immediately
		populateSidebarWithWorkspaceFiles(initializedWorkspacePath);

		// Focus Swapping Tracker: Synchronizes caret highlights AND structural trees on
		// active tab selections
		workspaceTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
			if (newTab instanceof WorkspaceTab) {
				WorkspaceTab activeTab = (WorkspaceTab) newTab;
				Platform.runLater(() -> activeTab.getTextArea().requestFocus());

				// SYNCHRONIZATION HOOK: Interacts with the new decoupled UI navigation routing
				// architecture safely
				TreeNavigationEngine.updateGlobalTreeStructure(activeTab.getTextArea().getText(), activeTab.getText());
			} else {
				// Fallback: Re-display standard directory structure if all tabs are closed
				populateSidebarWithWorkspaceFiles(initializedWorkspacePath);
			}
		});

		// Double-click handler with context-aware node execution
		// Double-click handler with context-aware node execution
		globalSidebarTree.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				TreeItem<String> selectedItem = globalSidebarTree.getSelectionModel().getSelectedItem();
				if (selectedItem == null || selectedItem == globalSidebarTree.getRoot())
					return;

				String rawValue = selectedItem.getValue();
				if (rawValue == null)
					return;

				// DETECT FILE TYPE EXPLICITLY: Files end in specific extensions and lack
				// structural colons
				boolean isActualFile = (rawValue.endsWith(".json") || rawValue.endsWith(".txt"))
						&& !rawValue.contains(":");

				if (isActualFile) {
					// --- 1. FILE OPEN LOGIC ---
					String fname = rawValue;
					if (fname.startsWith("▪ ")) {
						fname = fname.replace("▪ ", "").trim();
					}

					String cachedText = AppMenuEventHandler.getTabTextBufferMap().getOrDefault(fname, "");
					File originalFileRef = AppMenuEventHandler.getOriginalSourceFileMap().get(fname);
					String baseText = (originalFileRef != null) ? StorageEngine.readFileToString(originalFileRef)
							: cachedText;

					openNewEditorTabWithContent(fname, cachedText, baseText);
				} else {
					// --- 2. SYNTAX INJECTION LOGIC TARGETING THE ACTIVE RIGHT PANE EDITOR ---
					TextArea activeEditor = getCentralCodeEditor();
					if (activeEditor != null) {
						Platform.runLater(() -> {
							activeEditor.requestFocus();
							int caretPosition = activeEditor.getCaretPosition();

							// FIXED: Removed the leading '\n' line break so data drops exactly onto the
							// caret's row position
							String injectionText = "\"" + rawValue + "\",";

							// Atomic text swap drops the clean string token right on the cursor coordinates
							activeEditor.insertText(caretPosition, injectionText);

							// Relocate focus caret to sit cleanly right after your fresh code token
							// property
							activeEditor.positionCaret(caretPosition + injectionText.length());
						});
					} else {
						System.err.println(
								"[UForge Engine] Refused syntax injection: No active editor tab found in focus.");
					}
				}

			}
		});

		// Vertical split pane dividing the main workspace TabPane from the Terminal
		// Console View below
		SplitPane centerVerticalSplit = new SplitPane();
		centerVerticalSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
		centerVerticalSplit.getItems().addAll(workspaceTabPane, globalConsole);
		centerVerticalSplit.setDividerPositions(0.75);

		// Horizontal split pane dividing the Sidebar Tree View from the Center Work
		// System
		SplitPane mainHorizontalSplit = new SplitPane();
		mainHorizontalSplit.getItems().addAll(globalSidebarTree, centerVerticalSplit);
		mainHorizontalSplit.setDividerPositions(0.20);

		menuBar.initMenbar(primaryStage);

		mainLayoutRoot.setTop(menuBar);
		mainLayoutRoot.setCenter(mainHorizontalSplit);

		// Run layout dark thematic variables overrides programmatically
		ThemeEngine.applyDarkTheme(mainLayoutRoot);
		ThemeEngine.applyDarkTheme(menuBar);
		ThemeEngine.applyDarkTheme(globalSidebarTree);
		ThemeEngine.applyDarkTheme(workspaceTabPane);
		ThemeEngine.applyDarkTheme(centerVerticalSplit);
		ThemeEngine.applyDarkTheme(mainHorizontalSplit);
		ThemeEngine.applyDarkTheme(globalConsole);

		Scene scene = new Scene(mainLayoutRoot, 1280, 720);
		primaryStage.setTitle("UForge - Workspace");
		primaryStage.setScene(scene);
		primaryStage.show();

		logToConsole("[System] UI Layout loaded successfully with multi-view tracking components.");
		logToConsole("[System] Active Workspace Node: " + initializedWorkspacePath.getAbsolutePath());

		openNewEditorTabWithContent("untitled.txt", "", "");
	}

	/**
	 * Scans the active project directory and lists available files inside the
	 * global sidebar on system start.
	 */
	public static void populateSidebarWithWorkspaceFiles(File workspaceDir) {
		if (workspaceDir == null || !workspaceDir.exists())
			return;

		TreeItem<String> rootItem = new TreeItem<>(workspaceDir.getName());
		rootItem.setExpanded(true);

		File[] files = workspaceDir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isFile() && (f.getName().endsWith(".json") || f.getName().endsWith(".txt"))) {
					rootItem.getChildren().add(new TreeItem<>(f.getName()));
				}
			}
		}
		globalSidebarTree.setRoot(rootItem);
	}

	/**
	 * LEGACY COMPATIBILITY OVERLOAD ROUTINE: Safeguards generic text-editor menu
	 * instantiations.
	 */
	public static void openNewEditorTab(String filename) {
		openNewEditorTabWithContent(filename, "", "");
	}

	/**
	 * DIRECT PARAMETER FIX: Spawns a fully configured split compare workspace tab
	 * instantly, bypassing asynchronous file map racing bugs entirely.
	 */
	public static void openNewEditorTabWithContent(String filename, String activeContent, String baseContent) {
		for (Tab tab : workspaceTabPane.getTabs()) {
			if (tab.getText().equals(filename) || tab.getText().equals("*" + filename)) {
				workspaceTabPane.getSelectionModel().select(tab);
				return;
			}
		}

		WorkspaceTab newTab = new WorkspaceTab(filename, activeContent, baseContent);

		workspaceTabPane.getTabs().add(newTab);
		workspaceTabPane.getSelectionModel().select(newTab);

		Platform.runLater(() -> newTab.getTextArea().requestFocus());
	}
}
