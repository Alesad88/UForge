package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class StorageEngine {

	/**
	 * Dynamically verifies and ensures that the active workspace node directory exists.
	 * Automatically adapts if the system has migrated to a cross-PC fallback storage location.
	 */
	public static void verifyWorkspaceDirectories() {
		File workspace = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();

		if (!workspace.exists()) {
			workspace.mkdirs();
			System.out.println("[Storage Engine] Auto-generated fresh application data environment: "
					+ workspace.getAbsolutePath());

			File sampleFile = new File(workspace, "default_template.json");
			try {
				Files.writeString(sampleFile.toPath(), "{\n  \"status\": \"active\",\n  \"version\": \"1.0.0\"\n}");
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * Scans the active cross-PC working path and loads all local modding configurations.
	 */
	public static List<String> readAllWorkspaceJsonFiles() {
		verifyWorkspaceDirectories(); 
		List<String> payloadList = new ArrayList<>();
		File workspace = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();

		File[] targetFiles = workspace.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

		if (targetFiles != null) {
			for (File file : targetFiles) {
				try {
					String fileContent = Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
					payloadList.add(fileContent);
				} catch (IOException e) {
					System.err.println("[Storage Engine] Read failure on boot file scan: " + e.getMessage());
				}
			}
		}
		return payloadList;
	}

	/**
	 * Mirrors an external configuration file into the local active application workspace.
	 */
	public static File mirrorToWorkspaceFolder(File externalFile) {
		if (externalFile == null || !externalFile.exists()) {
			return null;
		}
			
		verifyWorkspaceDirectories();
		File workspace = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();

		File destinationFile = new File(workspace.getAbsolutePath() + File.separator + externalFile.getName());
		try {
			Files.copy(externalFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("[Storage Engine] Synced configuration data block into local application folders: " + destinationFile.getAbsolutePath());
			return destinationFile;
		} catch (IOException e) {
			System.err.println("[Storage Engine] Sync exception routing: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Pushes active edits saved in a specific scratchpad file out to its specific master target file.
	 */
	public static void saveWorkspaceToOriginal(File workspaceFile, File originalSourceFile) {
		if (workspaceFile == null || originalSourceFile == null) return;
		
		try {
			Files.copy(workspaceFile.toPath(), originalSourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("[Storage Engine] Pushed workspace edits out to master source file: " + originalSourceFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("[Storage Engine] Failed pushing edits to master file index: " + e.getMessage());
		}
	}

	/**
	 * Reads an explicit file block to raw String formats for the text area editor.
	 */
	public static String readFileToString(File file) {
		try {
			return Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("[Storage Engine] Failed reading custom file segment: " + e.getMessage());
			return "";
		}
	}

	/**
	 * Writes data packages back out to disk.
	 */
	public static void writeStringToFile(File file, String contents) {
		try {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			Files.writeString(file.toPath(), contents, java.nio.charset.StandardCharsets.UTF_8);
			System.out.println("[Storage Engine] Saved file layout state successfully: " + file.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("[Storage Engine] Critical save pipeline block failure: " + e.getMessage());
		}
	}

	/**
	 * VIRTUAL TRACKING FIX: Resolves a local workspace cache pointer using raw tab filenames 
	 * to prevent metadata mismatches when updating code content arrays.
	 */
	public static File getWorkspaceFileReference(String tabFilename) {
		if (tabFilename == null || tabFilename.isEmpty()) {
			return null;
		}
		File workspace = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();
		return new File(workspace.getAbsolutePath() + File.separator + tabFilename.replace("*", ""));
	}

	/**
	 * Computes the absolute combined byte size of all files inside the active scratchpad folder.
	 */
	public static long getWorkspaceDirectorySize() {
		File workspace = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();
		if (!workspace.exists() || !workspace.isDirectory()) return 0L;

		long totalBytes = 0;
		File[] fileList = workspace.listFiles();
		if (fileList != null) {
			for (File f : fileList) {
				if (f.isFile()) totalBytes += f.length();
			}
		}
		return totalBytes;
	}

	/**
	 * Safely deletes all files inside the active scratchpad directory to reclaim disk space.
	 */
	public static boolean clearWorkspaceDirectory() {
		File workspace = WorkspaceStorageManager.resolveSafeWorkingWorkspacePath();
		if (!workspace.exists() || !workspace.isDirectory()) return false;

		File[] fileList = workspace.listFiles();
		boolean success = true;
		if (fileList != null) {
			for (File f : fileList) {
				if (f.isFile()) {
					if (!f.delete()) success = false;
				}
			}
		}
		return success;
	}
}
