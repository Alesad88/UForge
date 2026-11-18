package application;

import javafx.scene.control.TextArea;

public class LiveJsonFormatter {

    private static boolean isFormattingInternal = false;

    /**
     * Checks if the document qualifies for formatting, runs the structural pretty-printer,
     * restores caret tracking coordinates seamlessly, and synchronizes memory state maps.
     * 
     * @param textArea The target interactive code editor control node.
     */
    public static void formatOnTypeIfValid(TextArea textArea) {
        if (isFormattingInternal || textArea == null) {
            return;
        }

        String completeText = textArea.getText();
        if (completeText == null || completeText.isEmpty()) {
            return;
        }

        // Trigger structural re-alignment on structural entry closing points ('}', ']')
        if (completeText.endsWith("}") || completeText.endsWith("]")) {
            if (isValidJsonStructure(completeText)) {
                executeFormatPipeline(textArea, "[Auto-Formatter] Structural re-alignment applied successfully.");
            }
        }
    }

    /**
     * HOTKEY EXPLICIT SWEEP HOOK: Force reformats the whole text array instantly
     * regardless of trailing closure markers, making structural adjustments known to the console.
     */
    public static void forceFullDocumentFormatSweep(TextArea textArea) {
        if (isFormattingInternal || textArea == null) {
            return;
        }

        String completeText = textArea.getText();
        if (completeText == null || completeText.isBlank()) {
            TheStage.logToConsole("[Formatter Warning] Action blocked. Current document workspace is entirely empty.");
            return;
        }

        if (!isValidJsonStructure(completeText)) {
            TheStage.logToConsole("[Formatter Error] Force sweep bypassed. Structural braces are unclosed or mismatched.");
            return;
        }

        int originalLength = completeText.length();
        executeFormatPipeline(textArea, "[Hotkey Action] Full-document layout format completed successfully.");
        
        // Log clean optimization metrics back down onto your terminal console track
        int netChange = textArea.getText().length() - originalLength;
        TheStage.logToConsole(String.format("[System Node] Workspace code footprint optimized. Delta modification track: %+d chars.", netChange));
    }

    private static void executeFormatPipeline(TextArea textArea, String successConsoleLog) {
        isFormattingInternal = true;

        String rawInput = textArea.getText();
        int originalCaretPosition = textArea.getCaretPosition();
        String formattedJson = prettyPrintJson(rawInput);

        // Update text view content
        textArea.setText(formattedJson);

        // Maintain the visible caret tracker location cleanly without text cursor jumping glitches
        int restoredCaretPosition = Math.min(originalCaretPosition, formattedJson.length());
        textArea.positionCaret(restoredCaretPosition);

        // Synchronize text mutations directly back to master tracking buffer maps
        var activeTab = TheStage.getWorkspaceTabPane().getSelectionModel().getSelectedItem();
        if (activeTab != null) {
            String cleanTitleKey = activeTab.getText().replace("*", "");
            AppMenuEventHandler.getTabTextBufferMap().put(cleanTitleKey, formattedJson);
            
            // Notify the layout system to make changes explicit across the telemetry footer bar
            if (activeTab instanceof WorkspaceTab) {
                ((WorkspaceTab) activeTab).refreshTelemetryMetrics();
            }
        }

        // Push an explicit system notification trace line down to the Terminal Console drawer panel
        TheStage.logToConsole(successConsoleLog);

        isFormattingInternal = false;
    }

    /**
     * Validation shield checking basic structural markers before modifying text blocks.
     */
    private static boolean isValidJsonStructure(String text) {
        String clean = text.trim();
        return (clean.startsWith("{") && clean.endsWith("}")) || (clean.startsWith("[") && clean.endsWith("]"));
    }

    /**
     * Pure zero-dependency structural pretty-printing engine with escaped quote safety tracking.
     */
    private static String prettyPrintJson(String raw) {
        StringBuilder pretty = new StringBuilder();
        int indentLevel = 0;
        boolean inQuotes = false;

        char[] charArray = raw.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char character = charArray[i];

            // Safely parse and skip escaped string literal tokens
            if (character == '"') {
                boolean isEscaped = false;
                int backslashCount = 0;
                int j = i - 1;
                while (j >= 0 && charArray[j] == '\\') {
                    backslashCount++;
                    j--;
                }
                if (backslashCount % 2 != 0) {
                    isEscaped = true;
                }

                if (!isEscaped) {
                    inQuotes = !inQuotes;
                    pretty.append(character);
                    continue;
                }
            }

            if (inQuotes) {
                pretty.append(character);
                continue;
            }

            switch (character) {
                case '{':
                case '[':
                    pretty.append(character).append("\n");
                    indentLevel++;
                    appendIndent(pretty, indentLevel);
                    break;
                case '}':
                case ']':
                    pretty.append("\n");
                    indentLevel--;
                    appendIndent(pretty, indentLevel);
                    pretty.append(character);
                    break;
                case ',':
                    pretty.append(character).append("\n");
                    appendIndent(pretty, indentLevel);
                    break;
                case ':':
                    pretty.append(character).append(" ");
                    break;
                case '\n':
                case '\r':
                case ' ':
                case '\t':
                    // Drop stray structural layout formatting whitespace safely
                    break;
                default:
                    pretty.append(character);
                    break;
            }
        }
        return pretty.toString();
    }

    private static void appendIndent(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            sb.append("    "); // Consistent 4-space soft tab profile
        }
    }
}
