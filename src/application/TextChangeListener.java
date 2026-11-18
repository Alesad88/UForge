package application;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;

public class TextChangeListener implements EventHandler<KeyEvent> {

	private static TextChangeListener activeAutofillEngineInstance = null;

	private String activeSuggestion = null;
	private String typedWordBuffer = null;

	private final Popup suggestionPopup = new Popup();
	private final TextFlow suggestionContainer = new TextFlow();
	private final Text prefixText = new Text("Tab to insert: ");
	private final Text dataText = new Text();

	public TextChangeListener() {
		activeAutofillEngineInstance = this;

		suggestionContainer.setStyle(
			"-fx-background-color: #2d2d2d; " + 
			"-fx-border-color: #3f3f46; " + 
			"-fx-border-width: 1px; " + 
			"-fx-padding: 4px 8px; " + 
			"-fx-background-radius: 3px; " + 
			"-fx-border-radius: 3px;"
		);

		prefixText.setStyle(
			"-fx-fill: #71717a; " + 
			"-fx-font-family: 'Consolas', 'Courier New', monospace; " + 
			"-fx-font-size: 13px;"
		);

		dataText.setStyle(
			"-fx-fill: #4fc1ff; " + 
			"-fx-font-weight: bold; " + 
			"-fx-font-family: 'Consolas', 'Courier New', monospace; " + 
			"-fx-font-size: 13px;"
		);

		suggestionContainer.getChildren().addAll(prefixText, dataText);
		suggestionPopup.getContent().add(suggestionContainer);
		suggestionPopup.setAutoHide(true);
	}

	public static TextChangeListener getAutofillEngine() {
		return activeAutofillEngineInstance;
	}

	public void clearPromptHistoryContext() {
		Platform.runLater(() -> {
			activeSuggestion = null;
			typedWordBuffer = null;
			if (suggestionPopup.isShowing()) {
				suggestionPopup.hide();
			}
			JsonDataEngine.purgeCachedParsingDictionaries();
		});
	}

	@Override
	public void handle(KeyEvent event) {
		TextArea textArea = (event.getSource() instanceof TextArea) ? (TextArea) event.getSource() : null;
		if (textArea == null) return;

		// --- TAB KEY HIJACKING ENGINE ---
		if (event.getCode() == KeyCode.TAB) {
			if (suggestionPopup.isShowing() && activeSuggestion != null && typedWordBuffer != null) {
				int caretPosition = textArea.getCaretPosition();
				int wordStart = caretPosition - typedWordBuffer.length();
				if (wordStart < 0) wordStart = 0;

				textArea.replaceText(wordStart, caretPosition, activeSuggestion);

				activeSuggestion = null;
				typedWordBuffer = null;
				suggestionPopup.hide();
				
				String cleanTitleKey = TheStage.getWorkspaceTabPane().getSelectionModel().getSelectedItem().getText().replace("*", "");
				AppMenuEventHandler.getTabTextBufferMap().put(cleanTitleKey, textArea.getText());
			} else {
				textArea.insertText(textArea.getCaretPosition(), "    ");
			}
			event.consume();
			return;
		}

		// --- HOTKEY COMBINATION ENGINE: CTRL + ALT + L ---
		if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.L) {
			suggestionPopup.hide();
			LiveJsonFormatter.forceFullDocumentFormatSweep(textArea);
			event.consume();
			return;
		}

		if (event.getCode() == KeyCode.ESCAPE || event.getCode().isNavigationKey()) {
			suggestionPopup.hide();
			return;
		}

		if (event.getCode().isModifierKey()) {
			return;
		}

		// Continuous autocomplete layer
		Platform.runLater(() -> {
			int currentCaret = textArea.getCaretPosition();
			String completeText = textArea.getText();
			
			if (currentCaret <= 0) {
				suggestionPopup.hide();
				return;
			}

			int extractionLimit = Math.min(currentCaret, completeText.length());
			String textUpToCaret = completeText.substring(0, extractionLimit);

			int lastWordBreak = Math.max(textUpToCaret.lastIndexOf(" "), textUpToCaret.lastIndexOf("\n"));
			lastWordBreak = Math.max(lastWordBreak, textUpToCaret.lastIndexOf("\""));
			lastWordBreak = Math.max(lastWordBreak, textUpToCaret.lastIndexOf(","));

			String dynamicWord = (lastWordBreak == -1) ? textUpToCaret : textUpToCaret.substring(lastWordBreak + 1);

			if (!dynamicWord.isBlank()) {
				String completion = JsonDataEngine.getAutofillSuggestion(dynamicWord);

				if (completion != null) {
					activeSuggestion = completion;
					typedWordBuffer = dynamicWord;
					dataText.setText(completion);

					Bounds caretBounds = null;
					try {
						if (textArea.lookup(".caret") != null) {
							caretBounds = textArea.lookup(".caret").getBoundsInLocal();
						}
					} catch (Exception ignored) {}
					
					if (caretBounds != null) {
						javafx.geometry.Point2D screenCoords = textArea.localToScreen(caretBounds.getMinX(), caretBounds.getMaxY());
						if (screenCoords != null) {
							suggestionPopup.show(textArea, screenCoords.getX(), screenCoords.getY() + 18);
							return;
						}
					}
					
					javafx.geometry.Point2D windowCoords = textArea.localToScreen(0, 0);
					if (windowCoords != null) {
						suggestionPopup.show(textArea, windowCoords.getX() + 60, windowCoords.getY() + 100);
					}
					return;
				}
			}
			suggestionPopup.hide();
		});
	}
}
