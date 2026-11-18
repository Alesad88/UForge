package application;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;

public class WorkspaceTabManager {

    public static void openNewEditorTab(String filename, String rawDataContent, String baseFileContent, boolean isModified) {
        WorkspaceTab newTab = new WorkspaceTab(filename, rawDataContent, baseFileContent);
        applyTabColorStyle(newTab, isModified);

        // Access static fields directly from TheStage
        TabPane tabPane = TheStage.getWorkspaceTabPane();
        TreeView<String> sidebarTree = TheStage.getGlobalSidebarTree();

        // Dynamically rebuild the global tree layout model when switching active tabs
        newTab.selectedProperty().addListener((obs, old, isNowSelected) -> {
            if (isNowSelected) {
                TextEditor.setTextArea(newTab.getTextArea());
                sidebarTree.setRoot(JsonTreeParser.buildStructuralTree(newTab.getTextArea().getText(), newTab.getText()));
            }
        });

        TextEditor.setTextArea(newTab.getTextArea());
        sidebarTree.setRoot(JsonTreeParser.buildStructuralTree(rawDataContent, filename));

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    public static void updateGlobalTreeModel(String jsonText, String rootName) {
        TheStage.getGlobalSidebarTree().setRoot(JsonTreeParser.buildStructuralTree(jsonText, rootName));
    }

    public static void flagActiveTabAsModified() {
        Tab current = TheStage.getWorkspaceTabPane().getSelectionModel().getSelectedItem();
        if (current != null) {
            applyTabColorStyle(current, true);
        }
    }

    public static void clearActiveTabModifiedState() {
        Tab current = TheStage.getWorkspaceTabPane().getSelectionModel().getSelectedItem();
        if (current != null) {
            applyTabColorStyle(current, false);
            
        }
    }


    public static void updateActiveTabDetails(String newFilename) {
        Tab current = TheStage.getWorkspaceTabPane().getSelectionModel().getSelectedItem();
        if (current != null) {
            current.setText(newFilename);
            applyTabColorStyle(current, false);
        }
    }

    private static void applyTabColorStyle(Tab tab, boolean isModified) {
        if (tab == null) return;
        
        String baseStyle = "-fx-font-weight: bold; -fx-padding: 6px 14px; -fx-font-size: 13px;";
        if (tab.isSelected()) {
            tab.setStyle(baseStyle
                    + " -fx-background-color: #2d2d2d; -fx-text-base-color: #ffffff; -fx-border-color: #52b788 transparent transparent transparent; -fx-border-width: 3px 0px 0px 0px;");
        } else if (isModified) {
            tab.setStyle(baseStyle
                    + " -fx-background-color: #362917; -fx-text-base-color: #ffb703; -fx-border-color: #ffb703 transparent transparent transparent; -fx-border-width: 2px 0px 0px 0px;");
        } else {
            tab.setStyle(baseStyle
                    + " -fx-background-color: #1f1f1f; -fx-text-base-color: #e0e0e0; -fx-border-color: transparent;");
        }
    }
}
