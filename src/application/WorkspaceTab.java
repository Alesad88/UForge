package application;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class WorkspaceTab extends Tab {

	private final TextArea textArea = new TextArea();
	private final TextArea referenceTextArea = new TextArea();
	private final TextFlow editableCodedFlow = new TextFlow();

	private final VBox rightLineGutterIndexTrack = new VBox();
	private final VBox leftLineGutterIndexTrack = new VBox();

	private final Text lineCountText = new Text("Lines: 0");
	private final Text charCountText = new Text("Chars: 0");
	private final Text statusLight = new Text("●");
	private final Text statusMessage = new Text("Schema Valid");

	public WorkspaceTab(String filename, String rawDataContent, String baseFileContent) {
		super(filename);
		applyTelemetryStyles();

		// --- RIGHT EDITABLE PANE SETUP ---
		textArea.setText(rawDataContent);
		styleEditorTextArea(textArea, true);
		textArea.addEventFilter(KeyEvent.KEY_PRESSED, new TextChangeListener());
		editableCodedFlow.setMouseTransparent(true);
		configureLineGutterTrack(rightLineGutterIndexTrack);

		StackPane rightStack = new StackPane(editableCodedFlow, textArea);
		HBox rightLayout = new HBox(0, rightLineGutterIndexTrack, rightStack);
		HBox.setHgrow(rightStack, Priority.ALWAYS);
		
		ScrollPane rightSP = new ScrollPane(rightLayout);
		configureScrollPane(rightSP, "#1e1e1e");

		// --- LEFT REFERENCE PANE SETUP ---
		referenceTextArea.setText(baseFileContent != null ? baseFileContent : "");
		styleEditorTextArea(referenceTextArea, false);
		configureLineGutterTrack(leftLineGutterIndexTrack);

		HBox leftLayout = new HBox(0, leftLineGutterIndexTrack, referenceTextArea);
		HBox.setHgrow(referenceTextArea, Priority.ALWAYS);
		
		// Wrap the left side layout in an identical ScrollPane to match the background color footprint
		ScrollPane leftSP = new ScrollPane(leftLayout);
		configureScrollPane(leftSP, "#1e1e1e");

		// --- MAIN LAYOUT ASSEMBLY ---
		SplitPane innerCompareSplitPane = new SplitPane(leftSP, rightSP);
		innerCompareSplitPane.setDividerPositions(0.45);
		innerCompareSplitPane.setStyle("-fx-box-border: transparent; -fx-background-color: #2d2d2d;");
		VBox.setVgrow(innerCompareSplitPane, Priority.ALWAYS);

		HBox telemetryFooterBar = new HBox(18, lineCountText, charCountText, new HBox(6, statusLight, statusMessage));
		telemetryFooterBar.setStyle("-fx-background-color: #141414; -fx-padding: 5px 12px; -fx-border-color: #2d2d2d transparent transparent transparent;");

		setContent(new VBox(innerCompareSplitPane, telemetryFooterBar));

		// Metrics and baseline numbering initialization
		DocumentTelemetry.updateMetrics(rawDataContent, lineCountText, charCountText, statusLight, statusMessage);
		setupTextChangeListener();
		refreshLineGutterIndex(rightLineGutterIndexTrack, rawDataContent);
		refreshLineGutterIndex(leftLineGutterIndexTrack, baseFileContent);

		Platform.runLater(() -> {
			// Lock global sidebar tree explicitly to permanent left-pane baseline text
			TreeNavigationEngine.updateGlobalTreeStructure(referenceTextArea.getText(), getText());
			TreeNavigationEngine.initializeNavigation(this, referenceTextArea, textArea);

			javafx.scene.Node rContent = textArea.lookup(".content");
			if (rContent != null) {
				editableCodedFlow.translateYProperty().bind(rContent.layoutYProperty());
				rightLineGutterIndexTrack.translateYProperty().bind(rContent.layoutYProperty());
			}
			javafx.scene.Node lContent = referenceTextArea.lookup(".content");
			if (lContent != null) {
				leftLineGutterIndexTrack.translateYProperty().bind(lContent.layoutYProperty());
			}
		});
	}

	private void styleEditorTextArea(TextArea area, boolean isInteractive) {
		area.setWrapText(true);
		if (isInteractive) {
			area.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px; -fx-display-caret: true; "
				+ "-fx-control-inner-background: transparent; -fx-background-color: transparent; -fx-text-fill: #a9b7c6; "
				+ "-fx-highlight-fill: #34495e22; -fx-highlight-text-fill: #ffffff; -fx-text-box-border: transparent; "
				+ "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		} else {
			area.setEditable(false);
			area.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px; "
				+ "-fx-control-inner-background: transparent; -fx-background-color: transparent; -fx-text-fill: #858585; "
				+ "-fx-highlight-fill: #34495e44; -fx-highlight-text-fill: #ffffff; -fx-text-box-border: transparent; "
				+ "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		}
	}

	private void configureScrollPane(ScrollPane scrollPane, String hexColor) {
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		scrollPane.setStyle("-fx-background: " + hexColor + "; -fx-background-color: " + hexColor 
			+ "; -fx-viewport-background: " + hexColor + "; -fx-box-border: transparent;");
	}

	private void configureLineGutterTrack(VBox gutter) {
		gutter.setAlignment(Pos.TOP_RIGHT);
		gutter.setPadding(new Insets(5, 8, 5, 4));
		gutter.setStyle("-fx-background-color: #161616; -fx-border-color: transparent #2d2d2d transparent transparent; -fx-border-width: 1px;");
		gutter.setMouseTransparent(true);
	}

	private void refreshLineGutterIndex(VBox gutter, String content) {
		gutter.getChildren().clear();
		if (content == null) return;
		int totalLines = Math.max(1, content.split("\r\n|\r|\n", -1).length);
		String style = "-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px; -fx-fill: #5c6370;";

		for (int i = 1; i <= totalLines; i++) {
			Text txt = new Text(String.valueOf(i));
			txt.setStyle(style);
			gutter.getChildren().add(txt);
		}
	}

	private void applyTelemetryStyles() {
		String style = "-fx-fill: #858585; -fx-font-family: 'Consolas';";
		lineCountText.setStyle(style);
		charCountText.setStyle(style);
		statusMessage.setStyle("-fx-fill: #b3b3b3; -fx-font-family: 'Consolas';");
		statusLight.setStyle("-fx-fill: #52b788;");
	}

	private void setupTextChangeListener() {
		textArea.textProperty().addListener((obs, oldText, newText) -> {
			if (newText == null) return;
			DocumentTelemetry.updateMetrics(textArea.getText(), lineCountText, charCountText, statusLight, statusMessage);
			refreshLineGutterIndex(rightLineGutterIndexTrack, textArea.getText());
			editableCodedFlow.getChildren().clear();
			editableCodedFlow.requestLayout();
		});
	}

	public TextArea getTextArea() { return this.textArea; }
	public void refreshTelemetryMetrics() { DocumentTelemetry.updateMetrics(textArea.getText(), lineCountText, charCountText, statusLight, statusMessage); }
}
