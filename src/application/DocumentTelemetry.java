package application;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DocumentTelemetry {

	/**
	 * Scans the active text array to calculate performance telemetry metrics and
	 * verify structural JSON formatting health.
	 */
	public static void updateMetrics(String text, Text lineCountText, Text charCountText, Text statusLight,
			Text statusMessage) {
		if (text == null || text.isEmpty()) {
			lineCountText.setText("Lines: 0");
			charCountText.setText("Chars: 0");
			statusLight.setText("●");
			statusLight.setStyle("-fx-fill: #858585;"); // Muted Gray
			statusMessage.setText("Empty Document Workspace");
			return;
		}

		// Calculate total strings and physical line breaks
		int chars = text.length();
		int lines = text.split("\r\n|\r|\n", -1).length;

		lineCountText.setText("Lines: " + lines);
		charCountText.setText("Chars: " + chars);

		// High-performance structural balance validator (Zero dependencies)
		String validationError = checkStructuralIntegrity(text);

		if (validationError == null) {
			statusLight.setText("●");
			statusLight.setStyle("-fx-fill: #52b788;"); // Neon Emerald
			statusMessage.setText("Schema Valid");
		} else {
			statusLight.setText("●");
			statusLight.setStyle("-fx-fill: #ef476f;"); // Error Coral Red
			statusMessage.setText("Error: " + validationError);
		}
	}

	private static String checkStructuralIntegrity(String text) {
		int braces = 0;
		int brackets = 0;
		boolean inQuotes = false;

		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];

			// SAFELY ESCAPE BACKSLASH RUNS: Backtrack to accurately check if the quote char is escaped
			if (c == '"') {
				boolean isEscaped = false;
				int backslashCount = 0;
				int j = i - 1;
				while (j >= 0 && chars[j] == '\\') {
					backslashCount++;
					j--;
				}
				if (backslashCount % 2 != 0) {
					isEscaped = true;
				}

				if (!isEscaped) {
					inQuotes = !inQuotes;
					continue;
				}
			}

			// Only validate structure outside literal string data bounds
			if (!inQuotes) {
				if (c == '{')
					braces++;
				else if (c == '}')
					braces--;
				else if (c == '[')
					brackets++;
				else if (c == ']')
					brackets--;

				// Quick catch for illegal trailing commas before closing marks
				if (c == ',' && (i + 1 < chars.length)) {
					char next = peekNextNonWhitespace(chars, i + 1);
					if (next == '}' || next == ']') {
						return "Trailing comma detected";
					}
				}

				if (braces < 0)
					return "Mismatched closing curly brace '}'";
				if (brackets < 0)
					return "Mismatched closing bracket ']'";
			}
		}

		if (inQuotes)
			return "Unclosed literal string quote detect";
		if (braces > 0)
			return "Missing closing curly brace '}'";
		if (brackets > 0)
			return "Missing closing bracket ']'";

		return null; // Document structural footprint is healthy
	}

	private static char peekNextNonWhitespace(char[] chars, int start) {
		for (int i = start; i < chars.length; i++) {
			if (!Character.isWhitespace(chars[i])) {
				return chars[i];
			}
		}
		return ' ';
	}
}
