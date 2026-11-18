package application;

import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.control.SplitPane;

public class ThemeEngine {

    /**
     * Applies an industrial dark style footprint directly onto core JavaFX SDK nodes
     * and strictly strips out redundant container scrollbars.
     */
    public static void applyDarkTheme(Node rootNode) {
        if (rootNode == null) return;

        // Apply a global base color variable override onto the root layout layer
        rootNode.setStyle(
            "-fx-base: #1e1e1e; " +                     
            "-fx-background-color: #1e1e1e; " +         
            "-fx-control-inner-background: #1e1e1e; " + 
            "-fx-text-fill: #e0e0e0;"
        );

        if (rootNode instanceof MenuBar) {
            rootNode.setStyle(
                "-fx-background-color: #252526; " +
                "-fx-border-color: transparent transparent #3e3e42 transparent; " +
                "-fx-border-width: 0 0 1px 0;"
            );
            // STABLE THEME ENGINE FIX: Strip programmatic menu item loops that trigger layout exceptions.
            // Menu stylings are managed cleanly via the top-level application Scene sheet definitions instead.
        } 
        else if (rootNode instanceof TreeView) {
            rootNode.setStyle(
                "-fx-background-color: #252526; " +
                "-fx-control-inner-background: #252526; " + 
                "-fx-border-color: transparent #3e3e42 transparent transparent; " +
                "-fx-border-width: 0 1px 0 0;"
            );
        } 
        else if (rootNode instanceof TabPane) {
            // FIX: Force the TabPane and ALL of its hidden internal content regions 
            // to completely disable scrollbar rendering.
            rootNode.setStyle(
                "-fx-background-color: #1e1e1e; " + 
                "-fx-control-inner-background: #1e1e1e; " +
                "-fx-edge-to-edge: true; " +
                "scrollbar-policy: never; " + 
                "-fx-scrollbar-policy: never;"
            );
        } 
        else if (rootNode instanceof TextArea) {
            rootNode.setStyle(
                "-fx-control-inner-background: #1e1e1e; " +
                "-fx-text-fill: #e0e0e0; " +
                "-fx-font-family: 'Consolas'; " +
                "-fx-font-size: 14px;"
            );
        } 
        else if (rootNode instanceof SplitPane) {
            // FIX: Prevents the split pane channels from generating phantom scroll zones
            rootNode.setStyle(
                "-fx-box-border: transparent; " +
                "-fx-background-color: #1e1e1e; " +
                "-fx-scrollbar-policy: never;"
            );
        }
    }

    /**
     * Programmatically darkens an individual editor text area tab upon dynamic creation.
     */
    public static void styleEditorTextArea(TextArea textArea) {
        if (textArea == null) return;
        textArea.setStyle(
            "-fx-control-inner-background: #1e1e1e; " +
            "-fx-text-fill: #e0e0e0; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-size: 14px; " +
            "-fx-border-color: transparent;"
        );
    }
}
