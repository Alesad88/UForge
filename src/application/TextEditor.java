package application;

import java.io.File;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TextEditor {
    private static TextArea textArea;
    private static TextArea baseTextArea; 
    private static File currentFile = null; 
    private static File originalSourceFile = null; // Track true multi-tab external source location

    public static void setTextArea(TextArea area) {
        textArea = area;
    }

    public static TextArea getTextArea() {
        return textArea;
    }

    public static void setBaseTextArea(TextArea area) {
        baseTextArea = area;
    }

    public static TextArea getBaseTextArea() {
        return baseTextArea;
    }

    public static void setCurrentFile(File file, Stage stage) {
        currentFile = file;
        
        if (stage != null) {
            if (file != null) {
                // Strips temporary workspace markers so the user sees a pristine file label
                String clearName = file.getName().replace(".tmp", "");
                stage.setTitle("UForge - " + clearName);
            } else {
                stage.setTitle("UForge - Universal Data Studio");
            }
        }
    }

    public static File getCurrentFile() {
        return currentFile;
    }

    // Explicit hooks to manage external master file index mapping records cleanly
    public static File getOriginalSourceFile() {
        return originalSourceFile;
    }

    public static void setOriginalSourceFile(File file) {
        originalSourceFile = file;
    }
}
