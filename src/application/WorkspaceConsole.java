package application;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class WorkspaceConsole extends VBox {

    private final TextArea consoleTextArea = new TextArea();
    private final Button toggleConsoleBtn;
    private boolean isConsoleExpanded = false;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public WorkspaceConsole() {
        consoleTextArea.setEditable(false);
        
        // FIX 1: Remove rigid height boundary. Let Vgrow and the SplitPane negotiate dimensions seamlessly.
        VBox.setVgrow(consoleTextArea, Priority.ALWAYS);
        
        // FIX 2: Explicitly force text wrapping. This drops lines cleanly and completely eliminates the second (horizontal) scroll track slider.
        consoleTextArea.setWrapText(true);

        consoleTextArea.setStyle("-fx-control-inner-background: #0f0f0f; -fx-text-fill: #a7c957; "
                + "-fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-text-box-border: transparent;");
        
        toggleConsoleBtn = new Button("Terminal Console [^]");
        toggleConsoleBtn.setMaxWidth(Double.MAX_VALUE);
        toggleConsoleBtn.setStyle("-fx-background-color: #141414; -fx-text-fill: #b3b3b3; -fx-font-family: 'Consolas'; "
                + "-fx-font-size: 11px; -fx-padding: 6px; -fx-cursor: hand; -fx-background-radius: 0; "
                + "-fx-border-color: #2d2d2d transparent transparent transparent;");
        
        toggleConsoleBtn.setOnAction(e -> toggleConsoleVisibility());

        this.getChildren().add(toggleConsoleBtn);
        appendLog("Global UForge workspace console pipeline active.");
    }

    private void toggleConsoleVisibility() {
        isConsoleExpanded = !isConsoleExpanded;
        if (isConsoleExpanded) {
            this.getChildren().add(0, consoleTextArea);
            toggleConsoleBtn.setText("Terminal Console [v]");
        } else {
            this.getChildren().remove(consoleTextArea);
            toggleConsoleBtn.setText("Terminal Console [^]");
        }
    }

    public void appendLog(String message) {
        String timestamp = LocalTime.now().format(timeFormatter);
        // FX THREAD INTEGRATION FIX: Wrap the append call to ensure multi-threaded log submissions
        // don't freeze or drop from the layout thread hierarchy natively.
        Platform.runLater(() -> {
            consoleTextArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }
}
