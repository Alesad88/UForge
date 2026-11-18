UForge Workspace — User Guide & Feature Overview
What Is UForge?
UForge is a lightweight, dark-themed desktop JSON and structured text editor designed for fast, error-free configuration and data editing. It pairs a side-by-side baseline comparison editor with live JSON syntax validation, context-aware property autocomplete, and an interactive structural tree navigator.

How to Run UForge (Windows)
UForge ships with its own self-contained Java runtime—no separate Java or JavaFX installation is required on the user's PC.
1. Extract the archive: Unzip UForge.zip to any folder on your Windows 10 or Windows 11 PC (for example, Documents\UForge or Desktop\UForge).
2. Keep the folder intact: Do not move UForge.exe out of its folder by itself—it relies on the app and runtime folders located next to it.
3. Launch the app: Double-click UForge.exe to open the workspace.

Workspace Layout Overview
When you open UForge, the interface is organized into four main areas:
• Top Menu Bar: Quick access to File, Edit, and Help commands along with keyboard shortcuts.
• Left Navigation Sidebar: Displays your local workspace files when no document is open, and dynamically switches to an interactive Structural JSON Node Tree whenever you open or select an editor tab.
• Center Split-Screen Editor (Tabbed):
• Left Reference Pane (Read-Only): Displays the original, unmodified version of your opened file so you can compare your edits side by side.
• Right Code Editor Pane (Interactive): Your live editing workspace with line numbers, soft-tab indentation, and floating property autocomplete.
• Bottom Telemetry Footer: Shows live Lines and Chars counts plus a color-coded Schema Health Indicator.
• Bottom Terminal Console Drawer: A collapsible log bar (Terminal Console [^]) that records timestamped file operations, formatting metrics, and system alerts.

Key Features & How to Use Them
1. Safe Scratchpad Editing & File Management
• Open Files (Ctrl + O): Select any .json or structured data file. UForge automatically copies the file into a local workspace-scratchpad folder so your original file stays safe while you edit.
• Save Changes (Ctrl + S): Saves your edits to the local scratchpad and commits the updated file back to your original source file.
• Export / Save As (Ctrl + Shift + S): Save your current document to a new .json or custom file location.
• New Document (Ctrl + N): Opens a blank Untitled.json tab.
2. Interactive Structural Tree & Quick Injection
• Click to Jump: Click any category or key: value item in the left sidebar tree to automatically locate, highlight, and center that property in both the reference pane and active editor.
• Double-Click to Open or Insert:
• Double-clicking a .json or .txt file in the workspace directory list opens it in a new tab.
• Double-clicking a property node in an active JSON tree injects "key: value", directly at your cursor position in the editor.
3. Smart Autocomplete & Auto-Formatting
• Context-Aware Property Autofill: As you type property names, UForge searches all .json files in your workspace and displays a floating Tab to insert: "Property": value popup near your cursor. Press Tab to accept the suggestion (or press Tab anytime without a popup to insert a 4-space indent).
• Live Auto-Formatting: Closing a valid JSON object (}) or array (]) automatically formats your code with clean 4-space indentation while keeping your cursor in place.
• Manual Full-Document Format (Ctrl + Alt + L): Press Ctrl + Alt + L at any time to format and indent the entire active document on demand.
4. Real-Time JSON Schema Health Telemetry
At the bottom of every tab, UForge continuously scans your JSON structure without slowing down typing:
• Gray (Empty Document Workspace): The active tab has no text.
• Green (Schema Valid): All {} braces, [] brackets, quotes, and commas are properly balanced.
• Red (Error: ...): Immediately alerts you to unclosed string quotes, trailing commas before } or ], or mismatched braces/brackets before you save.
5. Workspace Storage Management (Ctrl + Alt + M)
• Automatic Low-Disk Protection: If your primary drive drops below 200 MB of free space, UForge automatically routes scratchpad files to a safe fallback folder in your Windows user profile (~/.uforge_fallback_storage).
• Maintenance Dashboard (File > Manage Storage): Shows your active scratchpad folder path and total cache size in MB, with a Purge Cache & Prompt History button to wipe cached files and reset autocomplete memory in one click.

Keyboard Shortcuts Reference
Action	Shortcut
New File	Ctrl + N
Open File	Ctrl + O
Save File	Ctrl + S
Save File As	Ctrl + Shift + S
Format Entire JSON Document	Ctrl + Alt + L
Accept Autocomplete / 4-Space Indent	Tab
Dismiss Autocomplete Popup	Esc
Cut / Copy / Paste	Ctrl + X / Ctrl + C / Ctrl + V
Manage Workspace Storage	Ctrl + Alt + M
