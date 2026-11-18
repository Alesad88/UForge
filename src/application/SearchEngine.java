package application;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

public class SearchEngine {

    /**
     * Finds a keyword in the text area within a specific block context,
     * focuses the text cursor, and centers the scroll view viewport on that location.
     *
     * @param textArea The interactive editor text area control node
     * @param editorScrollPane The parent scroll frame containing the text engine stack
     * @param targetKeyword The exact string literal token target name
     * @param matchOccurrenceIndex Duplicate instance tracking offset index counter
     * @param contextBlockHeader The structural parent collection group token name
     * @return The exact line/row index where the match was centered, or -1 if the search missed
     */
    public static int performViewportJump(TextArea textArea, ScrollPane editorScrollPane, 
                                          String targetKeyword, int matchOccurrenceIndex, 
                                          String contextBlockHeader) {
        if (textArea == null) return -1;
        String text = textArea.getText();
        
        // Step 1: Establish our scanning anchor entry index position (Default to start of document)
        int contextStartAnchor = 0;
        if (contextBlockHeader != null && !contextBlockHeader.isEmpty()) {
            int headerMatch = text.indexOf("\"" + contextBlockHeader + "\"");
            if (headerMatch != -1) {
                contextStartAnchor = headerMatch;
            }
        }
        
        // Step 2: Loop past earlier keyword occurrences *strictly starting within our structural group bounds*
        int currentFindIndex = contextStartAnchor - 1;
        for (int i = 0; i <= matchOccurrenceIndex; i++) {
            currentFindIndex = text.indexOf("\"" + targetKeyword + "\"", currentFindIndex + 1);
            if (currentFindIndex == -1) {
                // Fallback retry block without string literal decoration markers if strict scan misses alignment mapping
                currentFindIndex = text.indexOf(targetKeyword, contextStartAnchor);
                break;
            }
        }
        
        // Step 3: Viewport focus and positioning calculations
        if (currentFindIndex != -1) {
            // Request cursor emphasis and highlight the string match range
            textArea.requestFocus();
            textArea.selectRange(currentFindIndex, currentFindIndex + targetKeyword.length());
            
            // Parse layout line row indicators up to our localized index point
            String textUpToMatch = text.substring(0, currentFindIndex);
            int targetLineRow = textUpToMatch.split("\r\n|\r|\n", -1).length;
            
            // Convert rows natively to precise pixel position tracking locations
            double estimatedLineHeightPixels = 19.5;
            double targetPixelScrollY = Math.max(0, (targetLineRow - 4) * estimatedLineHeightPixels); // Center cushion
            
            // Command the inner layout engine to slide viewport focus vectors directly down
            textArea.setScrollTop(targetPixelScrollY);
            
            // Step 4: Mirror calculations back out to the parent ScrollPane flawlessly
            // CRASH FIX: Guard check against null references before modifying ScrollPane properties
            if (editorScrollPane != null) {
                int totalLinesCount = text.split("\r\n|\r|\n", -1).length;
                double totalEstimatedHeight = totalLinesCount * estimatedLineHeightPixels;
                
                if (totalEstimatedHeight > 0) {
                    double scrollPercent = targetPixelScrollY / totalEstimatedHeight;
                    editorScrollPane.setVvalue(Math.max(0.0, Math.min(1.0, scrollPercent)));
                }
            }
            
            return targetLineRow; // Success: Returns line location for console diagnostics
        }
        
        return -1; // Missing: Signal out-of-bounds tracking state back to tab controller
    }
}
