package application;

import javafx.scene.control.TreeItem;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTreeParser {

	/**
	 * Scans a JSON configuration data string and returns a structured, clickable tree hierarchy model.
	 * Utilizes a Stack engine to dynamically manage nested object and array layers.
	 */
	public static TreeItem<String> buildStructuralTree(String jsonText, String rootName) {
		TreeItem<String> rootItem = new TreeItem<>(rootName);
		rootItem.setExpanded(true);

		if (jsonText == null || jsonText.strip().isEmpty()) {
			return rootItem;
		}

		try {
			// REGEX: Captures key names, structural openings ({, [), closures (}, ]), strings, numbers, and booleans
			Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:(\\{)|(\\[)|\\s*\"([^\"]*)\"|([\\d\\.\\-]+|true|false|null))|(\\})|(\\])");
			Matcher matcher = pattern.matcher(jsonText);

			// Track structural parents down the hierarchy
			Stack<TreeItem<String>> nodeStack = new Stack<>();
			nodeStack.push(rootItem);

			while (matcher.find()) {
				// Check for structural closures first to pop up a level safely
				boolean isObjectClose = (matcher.group(6) != null);
				boolean isArrayClose = (matcher.group(7) != null);

				if (isObjectClose || isArrayClose) {
					if (nodeStack.size() > 1) {
						nodeStack.pop();
					}
					continue;
				}

				String key = matcher.group(1);
				if (key == null) continue; // Skip structural mismatches

				boolean isObjectOpen = (matcher.group(2) != null);
				boolean isArrayOpen = (matcher.group(3) != null);
				String textValue = matcher.group(4);
				String primitiveValue = matcher.group(5);

				TreeItem<String> currentParent = nodeStack.peek();

				// Catch opening structural categories flexibly (e.g., "Exports": {, "Rows": [)
				if (isObjectOpen || isArrayOpen) {
					TreeItem<String> newCategory = new TreeItem<>(key);
					// Auto-expand major containers for readability
					if (key.equalsIgnoreCase("Exports") || key.equalsIgnoreCase("Rows") || 
					    key.equalsIgnoreCase("Table") || key.equalsIgnoreCase("Configuration")) {
						newCategory.setExpanded(true);
					} else {
						newCategory.setExpanded(false);
					}
					currentParent.getChildren().add(newCategory);
					nodeStack.push(newCategory); // Move context focus into this sub-layer
					
				} else {
					// Fallback layout formatting for standard text values, numbers, and booleans
					String displayValue = (textValue != null) ? textValue : primitiveValue;
					
					// Keep the format as "key: value" so TreeNavigationEngine can split by ":" and find the exact key name
					TreeItem<String> leafItem = new TreeItem<>(key + ": " + displayValue);
					currentParent.getChildren().add(leafItem);
				}
			}
		} catch (Exception e) {
			System.err.println("[Tree Parser Error] Failed to map structural node tree: " + e.getMessage());
		}

		return rootItem;
	}
}
