package application;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;

public class TreeNavigationEngine {

    /**
     * Initializes the tree selection mechanics safely on the active layout threads.
     */
    public static void initializeNavigation(WorkspaceTab currentTab, TextArea leftArea, TextArea rightArea) {
        TreeView<String> globalTree = TheStage.getGlobalSidebarTree();
        if (globalTree == null) return;

        // Clear previous event listeners by re-binding the selection property model cleanly
        globalTree.getSelectionModel().selectedItemProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode == null || newNode.getValue() == null) return;

            String clickedValue = newNode.getValue();
            String targetKey = cleanNodeValue(clickedValue);

            locateAndHighlightSyntax(leftArea, targetKey);
            locateAndHighlightSyntax(rightArea, targetKey);
        });
    }

    /**
     * Pushes raw string data up to the global hierarchy layout block.
     */
    public static void updateGlobalTreeStructure(String textContent, String activeTabTitle) {
        TreeView<String> globalTree = TheStage.getGlobalSidebarTree();
        if (globalTree != null) {
            TreeItem<String> newRoot = JsonTreeParser.buildStructuralTree(textContent, activeTabTitle);
            globalTree.setRoot(newRoot);
        }
    }

    private static String cleanNodeValue(String rawValue) {
        if (rawValue == null) return "";
        if (rawValue.contains(":")) {
            return rawValue.split(":")[0].trim().replace("\"", "");
        }
        return rawValue.replace("{", "").replace("}", "").replace("[", "").replace("]", "").trim();
    }

    private static void locateAndHighlightSyntax(TextArea targetArea, String targetKeyword) {
        if (targetKeyword.isEmpty()) return;
        
        String content = targetArea.getText();
        int index = content.indexOf("\"" + targetKeyword + "\"");
        
        if (index == -1) {
            index = content.indexOf(targetKeyword);
        }

        if (index != -1) {
            final int targetIndex = index;
            Platform.runLater(() -> {
                targetArea.requestFocus();
                targetArea.selectRange(targetIndex, targetIndex + targetKeyword.length());
                targetArea.positionCaret(targetIndex); 
            });
        }
    }
}
