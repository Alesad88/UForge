package application;

import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonHighlighter {

	// Unified regex capturing Blueprint Keys, normal Keys, Strings, Numbers/Booleans/Nulls, and Braces
	private static final Pattern JSON_PATTERN = Pattern.compile(
			"(?<BPKEY>\"(BP_[a-zA-Z0-9_]+)\"\\s*:)"
			+ "|(?<KEY>\"([^\"]+)\"\\s*:)"
			+ "|(?<STRING>\"([^\"\\\\]|\\\\.)*\")"
			+ "|(?<NUMBER>\\b-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b)"
			+ "|(?<LITERAL>\\b(true|false|null)\\b)"
			+ "|(?<BRACE>[\\{\\}\\[\\]])"
	);

	/**
	 * Tokenizes a raw JSON string, paints distinct IDE syntax colors,
	 * and overlays live forest green ghost suggestions onto a TextFlow canvas.
	 * 
	 * @param text The raw JSON payload to format
	 * @param flow The target TextFlow panel node to populate
	 * @param promptText The placeholder message to display if the string is empty
	 * @param activeWord The current prefix word the user has typed
	 * @param activeSuggestion The autocomplete suggestion actively pending
	 */
	public static void colorize(String text, TextFlow flow, String promptText, String activeWord, String activeSuggestion) {
		flow.getChildren().clear();
		
		if (text == null || text.isEmpty()) {
			Text emptyPrompt = new Text(promptText);
			emptyPrompt.setStyle("-fx-fill: #555555; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-style: italic;");
			flow.getChildren().add(emptyPrompt);
			return;
		}

		Matcher matcher = JSON_PATTERN.matcher(text);
		int lastIdx = 0;

		while (matcher.find()) {
			// 1. Fill normal structural formatting gaps (spaces, tabs, commas, colons)
			if (matcher.start() > lastIdx) {
				Text normalText = new Text(text.substring(lastIdx, matcher.start()));
				normalText.setStyle("-fx-fill: #a9b7c6; -fx-font-family: 'Consolas'; -fx-font-size: 14px;"); // Muted Off-White
				flow.getChildren().add(normalText);
			}

			// 2. Extract and colorize individual tokens matching Eclipse Dark definitions
			Text tokenText = new Text(matcher.group());
			
			if (matcher.group("BPKEY") != null) {
				tokenText.setStyle("-fx-fill: #9cdcfe; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-weight: bold;"); // Light Blue Blueprint Keys
			} else if (matcher.group("KEY") != null) {
				tokenText.setStyle("-fx-fill: #3a86ff; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-weight: bold;"); // Standard Sky Blue Keys
			} else if (matcher.group("STRING") != null) {
				tokenText.setStyle("-fx-fill: #52b788; -fx-font-family: 'Consolas'; -fx-font-size: 14px;"); // Emerald Green Strings
			} else if (matcher.group("NUMBER") != null) {
				tokenText.setStyle("-fx-fill: #f4a261; -fx-font-family: 'Consolas'; -fx-font-size: 14px;"); // Soft Orange Numbers
			} else if (matcher.group("LITERAL") != null) {
				tokenText.setStyle("-fx-fill: #569cd6; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-weight: bold;"); // Royal Blue Literals
			} else if (matcher.group("BRACE") != null) {
				tokenText.setStyle("-fx-fill: #ffd700; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-weight: bold;"); // Gold Brackets
			} else {
				tokenText.setStyle("-fx-fill: #a9b7c6; -fx-font-family: 'Consolas'; -fx-font-size: 14px;");
			}

			flow.getChildren().add(tokenText);
			lastIdx = matcher.end();
		}

		// 3. Fill any trailing remainder text block strings
		if (lastIdx < text.length()) {
			Text trailingText = new Text(text.substring(lastIdx));
			trailingText.setStyle("-fx-fill: #a9b7c6; -fx-font-family: 'Consolas'; -fx-font-size: 14px;");
			flow.getChildren().add(trailingText);
		}

		// 4. VISUAL HOOK: Append the live autocomplete ghost prompt at the very end of line if a suggestion matches
		if (activeSuggestion != null && activeWord != null && text.endsWith(activeWord)) {
			String ghostTextPayload = " /* TAB: " + activeSuggestion.substring(activeWord.length()) + " */";
			Text ghostNode = new Text(ghostTextPayload);
			ghostNode.setStyle("-fx-fill: #6a9955; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-style: italic;"); // Forest Green Comment style
			flow.getChildren().add(ghostNode);
		}
	}
}
