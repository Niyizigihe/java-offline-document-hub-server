package com.example.offlinedocumenthubserver;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Offline Document Hub";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String PARENT_BACKUP_FOLDER_NAME = "OfflineDocumentHub_Backups";

    private Drive driveService;
    private String parentFolderId;

    public GoogleDriveService() {
        System.out.println("🔧 [INIT] Starting GoogleDriveService initialization...");

        try {
            // Load credentials from resources
            System.out.println("🔧 [INIT] Looking for credentials at: " + CREDENTIALS_FILE_PATH);
            InputStream credentialsStream = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

            if (credentialsStream == null) {
                System.err.println("❌ [INIT] Credentials file NOT FOUND at: " + CREDENTIALS_FILE_PATH);
                throw new FileNotFoundException("Credentials file not found: " + CREDENTIALS_FILE_PATH);
            }
            System.out.println("✅ [INIT] Credentials file found!");

            // Build HTTP transport
            System.out.println("🔧 [INIT] Building HTTP transport...");
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            System.out.println("✅ [INIT] HTTP transport created successfully");

            // Create credentials - simplified approach
            System.out.println("🔧 [INIT] Creating GoogleCredential from service account...");

            GoogleCredential credential = GoogleCredential
                    .fromStream(credentialsStream, HTTP_TRANSPORT, JSON_FACTORY)
                    .createScoped(Collections.singleton(DriveScopes.DRIVE));

            System.out.println("✅ [INIT] GoogleCredential created successfully");

            if (credential.getServiceAccountId() != null) {
                System.out.println("✅ [INIT] Service account: " + credential.getServiceAccountId());
            }

            // Create Drive service
            System.out.println("🔧 [INIT] Building Drive service...");
            driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            System.out.println("✅ [INIT] Google Drive service initialized successfully!");

            // Initialize parent backup folder
            initializeParentFolder();

            // Quick API test
            System.out.println("🔧 [INIT] Testing API connection...");
            try {
                driveService.about().get().setFields("user").execute();
                System.out.println("✅ [INIT] API connection test successful!");
            } catch (Exception e) {
                System.err.println("⚠️ [INIT] API test warning: " + e.getMessage());
                System.err.println("⚠️ [INIT] This might be due to:");
                System.err.println("   1. Google Drive API not enabled in your project");
                System.err.println("   2. Service account needs to be granted access");
                System.err.println("   3. Check: https://console.cloud.google.com/apis/library/drive.googleapis.com");
            }

            System.out.println("✅ [INIT] Ready to perform backups!");

        } catch (FileNotFoundException e) {
            System.err.println("❌ [INIT] File error: " + e.getMessage());
            e.printStackTrace();
            driveService = null;
        } catch (IOException e) {
            System.err.println("❌ [INIT] IO error: " + e.getMessage());
            System.err.println("❌ [INIT] This could mean:");
            System.err.println("   - Invalid JSON format in credentials.json");
            System.err.println("   - Credentials file is corrupted");
            System.err.println("   - Network connectivity issues");
            e.printStackTrace();
            driveService = null;
        } catch (GeneralSecurityException e) {
            System.err.println("❌ [INIT] Security error: " + e.getMessage());
            System.err.println("❌ [INIT] This is often due to SSL/TLS issues");
            e.printStackTrace();
            driveService = null;
        } catch (Exception e) {
            System.err.println("❌ [INIT] Unexpected error: " + e.getClass().getName());
            System.err.println("❌ [INIT] Message: " + e.getMessage());
            e.printStackTrace();
            driveService = null;
        }

        System.out.println("🔧 [INIT] Initialization complete. Service: " + (driveService != null ? "✅ READY" : "❌ FAILED"));
    }

    private void initializeParentFolder() throws Exception {
        System.out.println("🔧 [FOLDER] Looking for parent backup folder: " + PARENT_BACKUP_FOLDER_NAME);

        String query = "mimeType='application/vnd.google-apps.folder' and name='" + PARENT_BACKUP_FOLDER_NAME + "' and trashed=false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (result.getFiles().isEmpty()) {
            System.out.println("📁 [FOLDER] Parent folder not found, creating new one...");
            File folderMetadata = new File();
            folderMetadata.setName(PARENT_BACKUP_FOLDER_NAME);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setDescription("Offline Document Hub Backup Storage");

            File parentFolder = driveService.files().create(folderMetadata)
                    .setFields("id, name")
                    .execute();

            parentFolderId = parentFolder.getId();
            System.out.println("✅ [FOLDER] Parent folder created: " + PARENT_BACKUP_FOLDER_NAME + " (ID: " + parentFolderId + ")");
        } else {
            parentFolderId = result.getFiles().get(0).getId();
            System.out.println("✅ [FOLDER] Found existing parent folder: " + PARENT_BACKUP_FOLDER_NAME + " (ID: " + parentFolderId + ")");
        }
    }

    public Map<String, Object> performBackup() {
        System.out.println("\n========================================");
        System.out.println("📦 [BACKUP] Starting backup process...");
        System.out.println("========================================");

        Map<String, Object> result = new HashMap<>();
        try {
            if (driveService == null) {
                System.err.println("❌ [BACKUP] Drive service is NULL!");
                throw new Exception("Google Drive service not initialized. Check server logs for initialization errors.");
            }

            if (parentFolderId == null) {
                System.err.println("❌ [BACKUP] Parent folder not initialized!");
                throw new Exception("Parent backup folder not available.");
            }

            System.out.println("✅ [BACKUP] Drive service is ready");

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFolderName = "DocumentHub_Backup_" + timestamp;

            System.out.println("📦 [BACKUP] Creating backup folder: " + backupFolderName);

            File folderMetadata = new File();
            folderMetadata.setName(backupFolderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList(parentFolderId));

            File backupFolder = driveService.files().create(folderMetadata)
                    .setFields("id, name")
                    .execute();

            String folderId = backupFolder.getId();
            result.put("folderId", folderId);
            result.put("folderName", backupFolderName);
            result.put("parentFolder", PARENT_BACKUP_FOLDER_NAME);

            System.out.println("✅ [BACKUP] Backup folder created successfully!");

            // Backup steps
            result.put("progress", 25);
            result.put("status", "Backing up database...");
            backupDatabase(folderId);

            result.put("progress", 50);
            result.put("status", "Backing up documents...");
            backupDocuments(folderId);

            result.put("progress", 75);
            result.put("status", "Creating backup summary...");
            createBackupInfoFile(folderId, backupFolderName);

            result.put("progress", 100);
            result.put("status", "Backup completed successfully!");
            result.put("success", true);
            result.put("message", "Backup completed successfully to Google Drive in folder: " + PARENT_BACKUP_FOLDER_NAME);

            System.out.println("\n========================================");
            System.out.println("✅ [BACKUP] BACKUP COMPLETED!");
            System.out.println("========================================\n");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Backup failed: " + e.getMessage());
            result.put("progress", 0);
            result.put("status", "Backup failed");

            System.err.println("\n========================================");
            System.err.println("❌ [BACKUP] BACKUP FAILED!");
            System.err.println("❌ [BACKUP] Error: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
        }
        return result;
    }

    private void backupDatabase(String folderId) throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String sqlFileName = "database_backup_" + timestamp + ".sql";
        java.io.File tempFile = new java.io.File(sqlFileName);

        try (Connection conn = DatabaseConnection.getConnection();
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            writer.println("-- Database Backup - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println();

            int userCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
                 ResultSet rs = stmt.executeQuery()) {
                writer.println("-- Users Table");
                while (rs.next()) {
                    writer.printf("INSERT INTO users (user_id, username, password_hash, role, full_name) VALUES (%d, '%s', '%s', '%s', '%s');%n",
                            rs.getInt("user_id"),
                            escapeSql(rs.getString("username")),
                            escapeSql(rs.getString("password_hash")),
                            escapeSql(rs.getString("role")),
                            escapeSql(rs.getString("full_name")));
                    userCount++;
                }
            }
            System.out.println("💾 Exported " + userCount + " users");

            int docCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM documents");
                 ResultSet rs = stmt.executeQuery()) {
                writer.println("\n-- Documents Table");
                while (rs.next()) {
                    writer.printf("INSERT INTO documents (doc_id, title, file_path, uploaded_by, upload_date, user_id, file_size) VALUES (%d, '%s', '%s', '%s', '%s', %d, %d);%n",
                            rs.getInt("doc_id"),
                            escapeSql(rs.getString("title")),
                            escapeSql(rs.getString("file_path")),
                            escapeSql(rs.getString("uploaded_by")),
                            rs.getDate("upload_date"),
                            rs.getInt("user_id"),
                            rs.getLong("file_size"));
                    docCount++;
                }
            }
            System.out.println("💾 Exported " + docCount + " documents");

            int logCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM activity_logs");
                 ResultSet rs = stmt.executeQuery()) {
                writer.println("\n-- Activity Logs");
                while (rs.next()) {
                    writer.printf("INSERT INTO activity_logs (log_id, user_id, action_type, action_details, timestamp) VALUES (%d, %d, '%s', '%s', '%s');%n",
                            rs.getInt("log_id"),
                            rs.getInt("user_id"),
                            escapeSql(rs.getString("action_type")),
                            escapeSql(rs.getString("action_details")),
                            rs.getTimestamp("timestamp"));
                    logCount++;
                }
            }
            System.out.println("💾 Exported " + logCount + " logs");
            writer.flush();

            File fileMetadata = new File();
            fileMetadata.setName(sqlFileName);
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("application/sql", tempFile);

            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute();

            System.out.println("☁️ Uploaded: " + sqlFileName);

        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    private String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("'", "''").replace("\\", "\\\\");
    }

    private void backupDocuments(String folderId) throws Exception {
        java.io.File sharedFolder = new java.io.File("shared_documents");

        if (!sharedFolder.exists() || !sharedFolder.isDirectory()) {
            System.out.println("⚠️ No documents folder, skipping");
            return;
        }

        java.io.File[] files = sharedFolder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("⚠️ No documents found, skipping");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String zipFileName = "documents_backup_" + timestamp + ".zip";
        java.io.File zipFile = new java.io.File(zipFileName);

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (java.io.File file : files) {
                if (file.isFile()) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file.toPath(), zos);
                    zos.closeEntry();
                }
            }
            zos.finish();

            System.out.println("📦 Created ZIP: " + zipFile.length() + " bytes");

            File fileMetadata = new File();
            fileMetadata.setName(zipFileName);
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("application/zip", zipFile);

            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute();

            System.out.println("☁️ Uploaded: " + zipFileName);

        } finally {
            if (zipFile.exists()) zipFile.delete();
        }
    }

    private void createBackupInfoFile(String folderId, String folderName) throws Exception {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String infoContent = "Document Hub Backup Information\n" +
                "==============================\n" +
                "Backup Time: " + timestamp + "\n" +
                "Backup Folder: " + folderName + "\n" +
                "Parent Folder: " + PARENT_BACKUP_FOLDER_NAME + "\n" +
                "Items Included:\n" +
                "- Database backup (SQL format)\n" +
                "- All documents (ZIP format)\n" +
                "- This summary file\n\n" +
                "Restore Instructions:\n" +
                "1. Download all files from this folder\n" +
                "2. Extract documents from the ZIP file\n" +
                "3. Run the SQL file to restore database\n" +
                "4. Place documents in shared_documents folder";

        java.io.File tempFile = java.io.File.createTempFile("backup_info", ".txt");
        Files.write(tempFile.toPath(), infoContent.getBytes());

        try {
            File fileMetadata = new File();
            fileMetadata.setName("BACKUP_INFO.txt");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", tempFile);

            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute();

            System.out.println("☁️ Uploaded: BACKUP_INFO.txt");

        } finally {
            tempFile.delete();
        }
    }

    public List<Map<String, String>> listBackups() throws Exception {
        List<Map<String, String>> backups = new ArrayList<>();

        if (driveService == null) {
            throw new Exception("Google Drive service not initialized");
        }

        String query = "mimeType='application/vnd.google-apps.folder' and name contains 'DocumentHub_Backup_' and '" + parentFolderId + "' in parents and trashed=false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name, createdTime)")
                .setOrderBy("createdTime desc")
                .execute();

        for (File file : result.getFiles()) {
            Map<String, String> backup = new HashMap<>();
            backup.put("id", file.getId());
            backup.put("name", file.getName());
            backup.put("createdTime", file.getCreatedTime().toString());
            backup.put("parentFolder", PARENT_BACKUP_FOLDER_NAME);
            backups.add(backup);
        }

        return backups;
    }
}