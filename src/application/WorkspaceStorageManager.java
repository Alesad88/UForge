package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;

public class WorkspaceStorageManager {

    // Threshold for emergency relocation (e.g., 200 Megabytes minimum required)
    private static final long EMERGENCY_DISK_THRESHOLD_BYTES = 200 * 1024 * 1024;

    /**
     * Verifies safe runtime workspace allocation constraints. 
     * If the destination machine's default disk tier fills completely, 
     * it auto-routes an alternative safe storage path in the user's home profile directory.
     */
    public static File resolveSafeWorkingWorkspacePath() {
        File primaryDir = new File(System.getProperty("user.dir") + File.separator + "workspace-scratchpad");
        
        // Check if the current drive context is full or lacks safe operational overhead
        if (primaryDir.exists() && primaryDir.getUsableSpace() < EMERGENCY_DISK_THRESHOLD_BYTES) {
            System.err.println("[Storage Guard] Primary runtime partition is critically full! Initializing portable fallback.");
            
            // Build a dynamic profile directory resilient across different host PCs
            File fallbackDir = new File(System.getProperty("user.home") + File.separator + ".uforge_fallback_storage");
            if (!fallbackDir.exists()) {
                fallbackDir.mkdirs();
            }
            return fallbackDir;
        }
        
        if (!primaryDir.exists()) {
            primaryDir.mkdirs();
        }
        return primaryDir;
    }

    /**
     * Spawns a structured window displaying local storage metrics with manual deletion triggers.
     */
    public static void displayMaintenanceDashboard(Stage parentStage) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.initOwner(parentStage);
        modalStage.setTitle("Workspace Storage Management");

        String darkBackground = "-fx-background-color: #1a1a1a;";
        String labelStyle = "-fx-text-fill: #e0e0e0; -fx-font-family: 'Consolas'; -fx-font-size: 14px;";
        String subLabelStyle = "-fx-text-fill: #888888; -fx-font-family: 'Consolas'; -fx-font-size: 11px;";
        String titleStyle = "-fx-text-fill: #ffffff; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-font-weight: bold;";

        VBox layoutRoot = new VBox(15);
        layoutRoot.setPadding(new Insets(25));
        layoutRoot.setStyle(darkBackground);
        layoutRoot.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Local Scratchpad Storage Metrics");
        titleLabel.setStyle(titleStyle);

        long currentBytes = StorageEngine.getWorkspaceDirectorySize();
        double megabytes = currentBytes / (1024.0 * 1024.0);
        
        Label statusLabel = new Label(String.format("Current Cache Footprint: %.2f MB", megabytes));
        statusLabel.setStyle(labelStyle);

        // Display current active location path context for cross-PC visibility
        File activePath = resolveSafeWorkingWorkspacePath();
        Label pathLabel = new Label("Active Storage Node: " + activePath.getAbsolutePath());
        pathLabel.setStyle(subLabelStyle);
        pathLabel.setWrapText(true);

        Button clearButton = new Button("Purge Cache & Prompt History");
        clearButton.setStyle("-fx-background-color: #d90429; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-padding: 8px 16px;");

        Button closeButton = new Button("Close Panel");
        closeButton.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #b3b3b3; -fx-padding: 8px 16px;");

        clearButton.setOnAction(e -> {
            // 1. Wipe local disk cache file pools
            boolean physicalDiskCleared = StorageEngine.clearWorkspaceDirectory();
            
            // 2. Clear volatile memory, auto-completion pipelines, and session context prompts
            AppMenuEventHandler.clearGlobalStateTrackingBuffers();
            
            // 3. Purge AI text autofill generation queues to release hidden memory objects
            if (TextChangeListener.getAutofillEngine() != null) {
                TextChangeListener.getAutofillEngine().clearPromptHistoryContext();
            }

            if (physicalDiskCleared) {
                statusLabel.setText("Current Cache Footprint: 0.00 MB (Storage & Prompt Context Flushed)");
                System.out.println("[Storage Engine] Complete purge complete: physical cache mirrors and inline prompt memory dropped.");
            } else {
                statusLabel.setText("Notice: Volatile memory wiped, but some active asset files are locked.");
            }
        });

        closeButton.setOnAction(e -> modalStage.close());

        HBox controlRow = new HBox(12, clearButton, closeButton);
        controlRow.setAlignment(Pos.CENTER_RIGHT);

        layoutRoot.getChildren().addAll(titleLabel, statusLabel, pathLabel, controlRow);

        Scene scene = new Scene(layoutRoot, 520, 210);
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        modalStage.showAndWait();
    }
}
