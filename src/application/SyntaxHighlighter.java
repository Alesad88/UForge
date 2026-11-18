package application;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class SyntaxHighlighter {

    private static final Pattern JSON_PATTERN = Pattern.compile(
            "(?<KEY>\"(BP_[a-zA-Z0-9_]+)\"\\s*:)"
            + "|(?<STRING>\"([^\"\\\\]|\\\\.)*\")"
            + "|(?<NUMBER>\\b-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b)"
            + "|(?<LITERAL>\\b(true|false|null)\\b)"
            + "|(?<BRACE>[\\{\\}\\[\\]])"
    );

    /**
     * Parses raw JSON text and overlays live ghost suggestions based on stored memory files.
     */
    public static List<Node> highlightJson(String text, String activeWord, String activeSuggestion) {
        List<Node> nodes = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return nodes;
        }

        Matcher matcher = JSON_PATTERN.matcher(text);
        int lastKwEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastKwEnd) {
                Text plainText = new Text(text.substring(lastKwEnd, matcher.start()));
                styleNode(plainText, "#d4d4d4", false, false);
                nodes.add(plainText);
            }

            Text styledText = new Text(matcher.group());
            if (matcher.group("KEY") != null) {
                styleNode(styledText, "#9cdcfe", true, false);  // Light Blue Blueprint Keys
            } else if (matcher.group("STRING") != null) {
                styleNode(styledText, "#ce9178", false, false); // Orange Strings
            } else if (matcher.group("NUMBER") != null) {
                styleNode(styledText, "#b5cea8", false, false); // Green Quantities
            } else if (matcher.group("LITERAL") != null) {
                styleNode(styledText, "#569cd6", true, false);  // Royal Blue Literals
            } else if (matcher.group("BRACE") != null) {
                styleNode(styledText, "#ffd700", true, false);  // Gold Brackets
            }
            
            nodes.add(styledText);
            lastKwEnd = matcher.end();
        }

        if (lastKwEnd < text.length()) {
            Text trailingText = new Text(text.substring(lastKwEnd));
            styleNode(trailingText, "#d4d4d4", false, false);
            nodes.add(trailingText);
        }

        // VISUAL HOOK: If the engine has an active auto-fill suggestion stored from a file,
        // render it as ghost text at the very end of the line on the left side!
        if (activeSuggestion != null && activeWord != null && text.endsWith(activeWord)) {
            // Isolate only the remaining part of the block to show as a ghost prompt
            String ghostTextPayload = " /* TAB: " + activeSuggestion.substring(activeWord.length()) + " */";
            Text ghostNode = new Text(ghostTextPayload);
            styleNode(ghostNode, "#6a9955", false, true); // Rendered as an italicized forest green code comment
            nodes.add(ghostNode);
        }

        return nodes;
    }

    /**
     * NEW CORE UTILITY: Safe programmatic interface engine to flush, render, 
     * and present stylized text flows directly inside layout view panes.
     */
    public static void renderToPane(TextFlow targetFlowPane, String rawJsonText, String activeWord, String activeSuggestion) {
        if (targetFlowPane == null) return;
        
        // Clear previous color node runs before painting fresh tokens
        targetFlowPane.getChildren().clear();
        
        // Parse and ingest colored sub-nodes list array
        List<Node> stylizedTokens = highlightJson(rawJsonText, activeWord, activeSuggestion);
        
        // Re-inject elements into rendering pipelines safely
        targetFlowPane.getChildren().addAll(stylizedTokens);
    }

    private static void styleNode(Text node, String hexColor, boolean bold, boolean italic) {
        node.setStyle(
            "-fx-fill: " + hexColor + ";" +
            "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
            "-fx-font-size: 14px;" +
            (bold ? "-fx-font-weight: bold;" : "") +
            (italic ? "-fx-font-style: italic;" : "")
        );
    }
}
