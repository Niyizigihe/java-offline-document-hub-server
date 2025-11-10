package com.example.offlinedocumenthubserver;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
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
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Offline Document Hub";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CLIENT_SECRETS_FILE_PATH = "/client_secret.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String PARENT_BACKUP_FOLDER_NAME = "OfflineDocumentHub_Backups";

    private Drive driveService;
    private String parentFolderId;

    // Progress tracking
    private volatile int currentProgress = 0;
    private volatile String currentStatus = "";
    private volatile boolean isBackupRunning = false;
    private volatile String currentBackupId = "";

    // Add progress callback interface
    public interface ProgressCallback {
        void onProgressUpdate(int progress, String status);
    }

    private ProgressCallback progressCallback;

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public GoogleDriveService() {
        System.out.println("🔧 [INIT] Starting GoogleDriveService initialization (User OAuth Flow)...");

        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            // Authorize as a user.
            Credential credential = authorize(HTTP_TRANSPORT);

            // Build the Drive service
            System.out.println("🔧 [INIT] Building Drive service...");
            driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            System.out.println("✅ [INIT] Google Drive service initialized successfully!");

            // Initialize parent backup folder
            initializeParentFolder();

            // Quick API test
            System.out.println("🔧 [INIT] Testing API connection...");
            driveService.about().get().setFields("user").execute();
            System.out.println("✅ [INIT] API connection test successful! User: " + driveService.about().get().setFields("user").execute().getUser().getEmailAddress());
            System.out.println("✅ [INIT] Ready to perform backups!");

        } catch (Exception e) {
            System.err.println("❌ [INIT] Initialization failed: " + e.getMessage());
            e.printStackTrace();
            driveService = null;
        }
    }

    private Credential authorize(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        System.out.println("🔧 [AUTH] Loading client secrets from: " + CLIENT_SECRETS_FILE_PATH);
        InputStream in = GoogleDriveService.class.getResourceAsStream(CLIENT_SECRETS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CLIENT_SECRETS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        System.out.println("✅ [AUTH] Client secrets loaded successfully.");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singleton(DriveScopes.DRIVE))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("defaultUser");
    }

    private void initializeParentFolder() throws Exception {
        System.out.println("🔧 [FOLDER] Looking for parent backup folder: " + PARENT_BACKUP_FOLDER_NAME);

        String query = "mimeType='application/vnd.google-apps.folder' and name='" + PARENT_BACKUP_FOLDER_NAME + "' and 'root' in parents and trashed=false";
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

public Map<String, Object> performBackup(String backupType, String createdBy) {
    System.out.println("\n========================================");
    System.out.println("📦 [BACKUP] Starting backup process...");
    System.out.println("📦 [BACKUP] Type: " + backupType + ", Created by: " + createdBy);
    System.out.println("========================================");

    Map<String, Object> result = new HashMap<>();
    isBackupRunning = true;
    currentBackupId = UUID.randomUUID().toString();

    try {
        if (driveService == null) {
            throw new Exception("Google Drive service not initialized");
        }

        if (parentFolderId == null) {
            throw new Exception("Parent backup folder not available");
        }

        updateProgress(5, "Initializing backup system...");

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFolderName = "DocumentHub_Backup_" + timestamp;

        updateProgress(10, "Creating backup folder in Google Drive...");

        File folderMetadata = new File();
        folderMetadata.setName(backupFolderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        folderMetadata.setParents(Collections.singletonList(parentFolderId));

        File backupFolder = driveService.files().create(folderMetadata)
                .setFields("id, name")
                .execute();

        String folderId = backupFolder.getId();

        updateProgress(20, "Starting database backup...");
        backupDatabase(folderId, backupType, createdBy);

        updateProgress(60, "Starting documents backup...");
        backupDocuments(folderId);

        updateProgress(80, "Creating backup summary...");
        createBackupInfoFile(folderId, backupFolderName, backupType, createdBy);

        updateProgress(95, "Finalizing backup...");
        // Small delay to show completion
        Thread.sleep(1000);

        updateProgress(100, "Backup completed successfully!");

        result.put("success", true);
        result.put("message", "Backup completed successfully");
        result.put("folderId", folderId);
        result.put("folderName", backupFolderName);
        result.put("parentFolder", PARENT_BACKUP_FOLDER_NAME);
        result.put("backupType", backupType);
        result.put("createdBy", createdBy);
        result.put("timestamp", timestamp);

        System.out.println("\n========================================");
        System.out.println("✅ [BACKUP] BACKUP COMPLETED!");
        System.out.println("========================================\n");

    } catch (Exception e) {
        updateProgress(0, "Backup failed: " + e.getMessage());
        result.put("success", false);
        result.put("message", "Backup failed: " + e.getMessage());
        result.put("backupType", backupType);
        result.put("createdBy", createdBy);

        System.err.println("\n========================================");
        System.err.println("❌ [BACKUP] BACKUP FAILED!");
        System.err.println("❌ [BACKUP] Error: " + e.getMessage());
        System.err.println("========================================");
        e.printStackTrace();
    } finally {
        isBackupRunning = false;
        currentBackupId = "";
    }
    return result;
}
private void backupDatabase(String folderId, String backupType, String createdBy) throws Exception {
    updateProgress(25, "Preparing database export...");

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String sqlFileName = "database_backup_" + timestamp + ".sql";
    java.io.File tempFile = new java.io.File(sqlFileName);

    try (Connection conn = DatabaseConnection.getConnection();
         PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

        writer.println("-- Database Backup - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        writer.println("-- Backup Type: " + backupType);
        writer.println("-- Created By: " + createdBy);
        writer.println();

        updateProgress(30, "Exporting users table...");
        int userCount = backupTable(conn, writer, "users", "user_id, username, password_hash, role, full_name");
        System.out.println("💾 Exported " + userCount + " users");

        updateProgress(35, "Exporting documents table...");
        int docCount = backupTable(conn, writer, "documents", "doc_id, title, file_path, uploaded_by, upload_date, user_id, file_size");
        System.out.println("💾 Exported " + docCount + " documents");

        updateProgress(40, "Exporting activity logs...");
        int logCount = backupTable(conn, writer, "activity_logs", "log_id, user_id, action_type, action_details, timestamp");
        System.out.println("💾 Exported " + logCount + " activity logs");

        updateProgress(45, "Exporting messages...");
        int messageCount = backupMessages(conn, writer);
        System.out.println("💾 Exported " + messageCount + " messages");

        writer.flush();

        updateProgress(50, "Uploading database backup to Google Drive...");
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
    private int backupTable(Connection conn, PrintWriter writer, String tableName, String columns) throws Exception {
        writer.println("-- " + tableName + " Table");
        String sql = "SELECT " + columns + " FROM " + tableName + " ORDER BY 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");

                // Build column names
                String[] columnArray = columns.split(",\\s*");
                for (int i = 0; i < columnArray.length; i++) {
                    if (i > 0) sqlBuilder.append(", ");
                    sqlBuilder.append(columnArray[i]);
                }
                sqlBuilder.append(") VALUES (");

                // Build values
                for (int i = 1; i <= columnArray.length; i++) {
                    if (i > 1) sqlBuilder.append(", ");
                    Object value = rs.getObject(i);
                    if (value == null) {
                        sqlBuilder.append("NULL");
                    } else if (value instanceof String) {
                        sqlBuilder.append("'").append(escapeSql((String) value)).append("'");
                    } else if (value instanceof java.sql.Date) {
                        sqlBuilder.append("'").append(value).append("'");
                    } else if (value instanceof java.sql.Timestamp) {
                        sqlBuilder.append("'").append(value).append("'");
                    } else {
                        sqlBuilder.append(value);
                    }
                }
                sqlBuilder.append(");");

                writer.println(sqlBuilder.toString());
                count++;
            }
            return count;
        }
    }

    private int backupMessages(Connection conn, PrintWriter writer) throws Exception {
        writer.println("-- Messages Table");
        String sql = "SELECT message_id, sender_id, receiver_id, message_text, sent_date, is_read FROM messages ORDER BY message_id";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                writer.printf("INSERT INTO messages (message_id, sender_id, receiver_id, message_text, sent_date, is_read) VALUES (%d, %d, %d, '%s', '%s', %b);%n",
                        rs.getInt("message_id"),
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        escapeSql(rs.getString("message_text")),
                        rs.getTimestamp("sent_date"),
                        rs.getBoolean("is_read"));
                count++;
            }
            return count;
        }
    }

    private String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("'", "''").replace("\\", "\\\\");
    }

private void backupDocuments(String folderId) throws Exception {
    updateProgress(65, "Scanning documents folder...");

    java.io.File sharedFolder = new java.io.File("shared_documents");

    if (!sharedFolder.exists() || !sharedFolder.isDirectory()) {
        System.out.println("⚠️ No documents folder, skipping");
        updateProgress(70, "No documents folder found - skipping");
        return;
    }

    java.io.File[] files = sharedFolder.listFiles();
    if (files == null || files.length == 0) {
        System.out.println("⚠️ No documents found, skipping");
        updateProgress(70, "No documents found - skipping");
        return;
    }

    updateProgress(70, "Creating documents archive (" + files.length + " files)...");
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String zipFileName = "documents_backup_" + timestamp + ".zip";
    java.io.File zipFile = new java.io.File(zipFileName);

    try (FileOutputStream fos = new FileOutputStream(zipFile);
         ZipOutputStream zos = new ZipOutputStream(fos)) {

        int fileCount = 0;
        for (java.io.File file : files) {
            if (file.isFile()) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
                fileCount++;

                // Update progress for each file (if many files)
                if (files.length > 10) {
                    int fileProgress = 70 + (int)((fileCount * 10.0) / files.length);
                    updateProgress(fileProgress, "Archiving documents (" + fileCount + "/" + files.length + ")...");
                }
            }
        }
        zos.finish();

        System.out.println("📦 Created ZIP: " + zipFile.length() + " bytes");

        updateProgress(80, "Uploading documents to Google Drive...");
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
    private void createBackupInfoFile(String folderId, String folderName, String backupType, String createdBy) throws Exception {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String infoContent = "Document Hub Backup Information\n" +
                "==============================\n" +
                "Backup Time: " + timestamp + "\n" +
                "Backup Folder: " + folderName + "\n" +
                "Backup Type: " + backupType + "\n" +
                "Created By: " + createdBy + "\n" +
                "Parent Folder: " + PARENT_BACKUP_FOLDER_NAME + "\n" +
                "Items Included:\n" +
                "- Database backup (SQL format) with users, documents, activity logs, and messages\n" +
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

    // Progress tracking methods
    private void updateProgress(int progress, String status) {
        this.currentProgress = progress;
        this.currentStatus = status;
        System.out.println("📊 [PROGRESS] " + progress + "% - " + status);

        // Notify callback if set
        if (progressCallback != null) {
            try {
                progressCallback.onProgressUpdate(progress, status);
            } catch (Exception e) {
                System.err.println("Error in progress callback: " + e.getMessage());
            }
        }
    }
    public Map<String, Object> getBackupProgress() {
        Map<String, Object> progress = new HashMap<>();
        progress.put("progress", currentProgress);
        progress.put("status", currentStatus);
        progress.put("active", isBackupRunning);
        progress.put("backupId", currentBackupId);

        // Add timestamp to prevent rapid updates
        progress.put("timestamp", System.currentTimeMillis());

        return progress;
    }

    public boolean isBackupRunning() {
        return isBackupRunning;
    }
}