package com.example.offlinedocumenthubserver;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileManager {
    private static final String SHARED_FOLDER = "shared_documents";
    private static final int MAX_FILENAME_LENGTH = 100;

    static {
        // Create shared folder if it doesn't exist
        File folder = new File(SHARED_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static String copyFileToSharedFolder(File sourceFile, String title) throws IOException {
        // Generate unique filename to avoid conflicts
        String originalFileName = sourceFile.getName();
        String fileExtension = getFileExtension(originalFileName);
        String safeTitle = makeFileNameSafe(title);

        // Create unique filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String newFileName = safeTitle + "_" + timestamp + fileExtension;

        // Truncate if too long
        if (newFileName.length() > MAX_FILENAME_LENGTH) {
            newFileName = newFileName.substring(0, MAX_FILENAME_LENGTH - fileExtension.length()) + fileExtension;
        }

        Path destinationPath = Paths.get(SHARED_FOLDER, newFileName);

        // Copy file to shared folder
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return destinationPath.toString();
    }

    public static File getFileFromSharedFolder(String filePath) {
        return new File(filePath);
    }

    public static boolean deleteFileFromSharedFolder(String filePath) {
        try {
            File file = new File(filePath);
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return (lastDotIndex > 0) ? fileName.substring(lastDotIndex) : "";
    }

    private static String makeFileNameSafe(String fileName) {
        // Remove or replace invalid characters
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public static long getFileSize(String filePath) {
        try {
            File file = new File(filePath);
            return file.length();
        } catch (Exception e) {
            return 0;
        }
    }
}