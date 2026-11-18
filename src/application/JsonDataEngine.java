package application;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonDataEngine {
	private static final Map<String, String> jsonStorageMap = new HashMap<>();

	/**
	 * Initializes the JSON pattern parsing engine at application boot.
	 * Dynamically utilizes the verified path parameter to scan configuration targets across various host PCs.
	 * 
	 * @param activeWorkspacePath The resolved target folder location context.
	 */
	public static void initializeEngineOnBoot(File activeWorkspacePath) {
		jsonStorageMap.clear();
		
		// Read raw data profiles out of the actively assigned directory tier
		List<String> rawContents = StorageEngine.readAllWorkspaceJsonFiles();

		for (String content : rawContents) {
			parseAndRegisterTextPatterns(content);
		}
		System.out.println(
				"[Data Engine] Indexing complete. Loaded " + jsonStorageMap.size() + " data objects into cache.");
	}

	/**
	 * Explicitly drops the underlying pattern map buffer to clear the active prompt suggestion context.
	 */
	public static void purgeCachedParsingDictionaries() {
		jsonStorageMap.clear();
		System.out.println("[Data Engine] Storage indexing map explicitly purged from application memory.");
	}

	/**
	 * Processes text blocks to isolate asset configurations.
	 * FLEXIBLE FALLBACK RULE: Matches complex objects, nested arrays, standard text properties, or raw numbers.
	 */
	public static void parseAndRegisterTextPatterns(String jsonRawContent) {
		if (jsonRawContent == null || jsonRawContent.isBlank())
			return;

		// ROBUST STRUCTURAL KEY-VALUE PARSER: Splitting key capture away from complex nested object boundaries 
		// to guarantee full property indexing without high backtracking processing delays on massive game properties.
		Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|\\d+(?:\\.\\d+)?|true|false|null|\\{|\\s*\\[)");
		Matcher matcher = pattern.matcher(jsonRawContent);

		while (matcher.find()) {
			String assetKey = matcher.group(1);
			String structuralValue = matcher.group(2).trim();
			
			// Normalize closing notations for raw layout representations
			if (structuralValue.equals("{")) {
				structuralValue = "{}";
			} else if (structuralValue.equals("[")) {
				structuralValue = "[]";
			}
			
			jsonStorageMap.put(assetKey, structuralValue);
		}
	}

	public static String getAutofillSuggestion(String currentWord) {
		if (currentWord == null || currentWord.isBlank() || jsonStorageMap.isEmpty()) {
			return null;
		}

		String searchKey = currentWord.toLowerCase().trim().replace("\"", "");
		if (searchKey.isBlank()) return null;

		for (Map.Entry<String, String> entry : jsonStorageMap.entrySet()) {
			String key = entry.getKey();
			if (key.toLowerCase().startsWith(searchKey) && !key.equalsIgnoreCase(searchKey)) {
				// STABLE COMPLETION FORMAT: Returns a cleanly formed JSON property component line
				// that lines up cleanly with your WorkspaceTab key events.
				return "\"" + key + "\": " + entry.getValue();
			}
		}
		return null;
	}
}
