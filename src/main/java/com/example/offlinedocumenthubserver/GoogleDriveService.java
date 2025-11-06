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
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.http.HttpCredentialsAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

//    public GoogleDriveService() {
//        System.out.println("🔧 [INIT] Starting GoogleDriveService initialization...");
//        java.io.File tempCredFile = null;
//
//        try {
//            // Load credentials from resources
//            System.out.println("🔧 [INIT] Looking for credentials at: " + CREDENTIALS_FILE_PATH);
//            InputStream credentialsStream = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//
//            if (credentialsStream == null) {
//                System.err.println("❌ [INIT] Credentials file NOT FOUND at: " + CREDENTIALS_FILE_PATH);
//                System.err.println("❌ [INIT] Make sure credentials.json is in src/main/resources/");
//                throw new FileNotFoundException("Credentials file not found: " + CREDENTIALS_FILE_PATH);
//            }
//            System.out.println("✅ [INIT] Credentials file found!");
//
//            // Read credentials to byte array
//            System.out.println("🔧 [INIT] Reading credentials file content...");
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = credentialsStream.read(buffer)) != -1) {
//                baos.write(buffer, 0, length);
//            }
//            credentialsStream.close();
//            byte[] credentialsBytes = baos.toByteArray();
//            String credentialsContent = new String(credentialsBytes, StandardCharsets.UTF_8);
//
//            System.out.println("✅ [INIT] Credentials file size: " + credentialsBytes.length + " bytes");
//            System.out.println("🔧 [INIT] First 150 chars: " +
//                    credentialsContent.substring(0, Math.min(150, credentialsContent.length())) + "...");
//
//            // Validate JSON structure
//            if (!credentialsContent.contains("\"type\"") || !credentialsContent.contains("\"private_key\"")) {
//                System.err.println("❌ [INIT] Invalid credentials file format!");
//                throw new IllegalArgumentException("Invalid credentials.json format");
//            }
//            System.out.println("✅ [INIT] Credentials file has valid JSON structure");
//
//            // Write to temporary file
//            System.out.println("🔧 [INIT] Creating temporary credentials file...");
//            tempCredFile = java.io.File.createTempFile("google_creds_", ".json");
//            tempCredFile.deleteOnExit();
//            Files.write(tempCredFile.toPath(), credentialsBytes);
//            System.out.println("✅ [INIT] Temp credentials file created: " + tempCredFile.getAbsolutePath());
//
//            // Build HTTP transport
//            System.out.println("🔧 [INIT] Building HTTP transport...");
//            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//            System.out.println("✅ [INIT] HTTP transport created successfully");
//
//            // Create credentials with broader scope
//            System.out.println("🔧 [INIT] Creating GoogleCredential from service account...");
//            System.out.println("🔧 [INIT] Reading from temp file: " + tempCredFile.getName());
//
//            FileInputStream fileInputStream = new FileInputStream(tempCredFile);
//            GoogleCredential credential;
//
//            try {
//                System.out.println("🔧 [INIT] Calling GoogleCredential.fromStream()...");
//                credential = GoogleCredential.fromStream(fileInputStream, HTTP_TRANSPORT, JSON_FACTORY);
//                System.out.println("✅ [INIT] Base credential created");
//
//                // Use broader scopes for full Drive access
//                System.out.println("🔧 [INIT] Creating scoped credential with DRIVE scope...");
//                List<String> scopes = Arrays.asList(
//                        DriveScopes.DRIVE,
//                        DriveScopes.DRIVE_FILE,
//                        DriveScopes.DRIVE_METADATA
//                );
//                credential = credential.createScoped(scopes);
//                System.out.println("✅ [INIT] GoogleCredential created successfully");
//
//            } catch (Exception e) {
//                System.err.println("❌ [INIT] Failed to create GoogleCredential!");
//                System.err.println("❌ [INIT] Error type: " + e.getClass().getName());
//                System.err.println("❌ [INIT] Error message: " + e.getMessage());
//                e.printStackTrace();
//                throw e;
//            } finally {
//                fileInputStream.close();
//            }
//
//            if (credential.getServiceAccountId() != null) {
//                System.out.println("✅ [INIT] Service account: " + credential.getServiceAccountId());
//            }
//
//            // Create Drive service
//            System.out.println("🔧 [INIT] Building Drive service...");
//            driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
//                    .setApplicationName(APPLICATION_NAME)
//                    .build();
//
//            System.out.println("✅ [INIT] Google Drive service initialized successfully!");
//
//            // Initialize parent backup folder
//            initializeParentFolder();
//
//            // Quick API test
//            System.out.println("🔧 [INIT] Testing API connection...");
//            try {
//                driveService.about().get().setFields("user").execute();
//                System.out.println("✅ [INIT] API connection test successful!");
//            } catch (Exception e) {
//                System.err.println("⚠️ [INIT] API test warning: " + e.getMessage());
//                // Don't fail initialization, just warn
//            }
//
//            System.out.println("✅ [INIT] Ready to perform backups!");
//
//        } catch (FileNotFoundException e) {
//            System.err.println("❌ [INIT] File error: " + e.getMessage());
//            e.printStackTrace();
//            driveService = null;
//        } catch (IOException e) {
//            System.err.println("❌ [INIT] IO error: " + e.getMessage());
//            e.printStackTrace();
//            driveService = null;
//        } catch (Exception e) {
//            System.err.println("❌ [INIT] Unexpected error: " + e.getClass().getName());
//            System.err.println("❌ [INIT] Message: " + e.getMessage());
//            e.printStackTrace();
//            driveService = null;
//        } finally {
//            // Clean up temp file
//            if (tempCredFile != null && tempCredFile.exists()) {
//                try {
//                    System.out.println("🔧 [INIT] Cleaning up temp credentials file...");
//                    tempCredFile.delete();
//                } catch (Exception e) {
//                    System.err.println("⚠️ [INIT] Could not delete temp file: " + e.getMessage());
//                }
//            }
//        }
//
//        System.out.println("🔧 [INIT] Initialization complete. Service: " + (driveService != null ? "✅ READY" : "❌ FAILED"));
//    }
public GoogleDriveService() {
    System.out.println("🔧 [INIT] Starting GoogleDriveService initialization...");
    java.io.File tempCredFile = null;

    try {
        // Load credentials from resources
        System.out.println("🔧 [INIT] Looking for credentials at: " + CREDENTIALS_FILE_PATH);
        InputStream credentialsStream = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (credentialsStream == null) {
            System.err.println("❌ [INIT] Credentials file NOT FOUND at: " + CREDENTIALS_FILE_PATH);
            System.err.println("❌ [INIT] Make sure credentials.json is in src/main/resources/");
            throw new FileNotFoundException("Credentials file not found: " + CREDENTIALS_FILE_PATH);
        }
        System.out.println("✅ [INIT] Credentials file found!");

        // Read credentials to byte array
        System.out.println("🔧 [INIT] Reading credentials file content...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = credentialsStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        credentialsStream.close();
        byte[] credentialsBytes = baos.toByteArray();
        String credentialsContent = new String(credentialsBytes, StandardCharsets.UTF_8);

        System.out.println("✅ [INIT] Credentials file size: " + credentialsBytes.length + " bytes");
        System.out.println("🔧 [INIT] First 150 chars: " +
                credentialsContent.substring(0, Math.min(150, credentialsContent.length())) + "...");

        // Validate JSON structure
        if (!credentialsContent.contains("\"type\"") || !credentialsContent.contains("\"private_key\"")) {
            System.err.println("❌ [INIT] Invalid credentials file format!");
            throw new IllegalArgumentException("Invalid credentials.json format");
        }
        System.out.println("✅ [INIT] Credentials file has valid JSON structure");

        // Write to temporary file
        System.out.println("🔧 [INIT] Creating temporary credentials file...");
        tempCredFile = java.io.File.createTempFile("google_creds_", ".json");
        tempCredFile.deleteOnExit();
        Files.write(tempCredFile.toPath(), credentialsBytes);
        System.out.println("✅ [INIT] Temp credentials file created: " + tempCredFile.getAbsolutePath());
        testCredentials();
        // Build HTTP transport
        System.out.println("🔧 [INIT] Building HTTP transport...");
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            System.out.println("✅ [INIT] HTTP transport created successfully");
        } catch (Exception e) {
            System.err.println("❌ [INIT] Failed to create HTTP transport!");
            System.err.println("❌ [INIT] Error: " + e.getClass().getName() + " - " + e.getMessage());
            throw e;
        }

        // Create credentials
        System.out.println("🔧 [INIT] Creating GoogleCredential from service account...");
        System.out.println("🔧 [INIT] Reading from temp file: " + tempCredFile.getName());

        GoogleCredential credential;
        try (FileInputStream fileInputStream = new FileInputStream(tempCredFile)) {
            System.out.println("🔧 [INIT] Calling GoogleCredential.fromStream()...");

            // Try with different approaches
            try {
                // Approach 1: Standard method
                credential = GoogleCredential.fromStream(fileInputStream, HTTP_TRANSPORT, JSON_FACTORY);
                System.out.println("✅ [INIT] GoogleCredential created using standard method");
            } catch (Exception e) {
                System.err.println("⚠️ [INIT] Standard method failed, trying alternative approach...");
                System.err.println("⚠️ [INIT] Error: " + e.getMessage());

                // Approach 2: Alternative method - recreate stream
                try (FileInputStream newStream = new FileInputStream(tempCredFile)) {
                    credential = GoogleCredential.fromStream(newStream);
                    System.out.println("✅ [INIT] GoogleCredential created using alternative method");
                }
            }

            System.out.println("✅ [INIT] Base credential created");

            // Use broader scopes for full Drive access
            System.out.println("🔧 [INIT] Creating scoped credential with DRIVE scope...");
            List<String> scopes = Arrays.asList(
                    DriveScopes.DRIVE,
                    DriveScopes.DRIVE_FILE,
                    DriveScopes.DRIVE_METADATA
            );
            credential = credential.createScoped(scopes);
            System.out.println("✅ [INIT] GoogleCredential scoped successfully");

        } catch (Exception e) {
            System.err.println("❌ [INIT] Failed to create GoogleCredential!");
            System.err.println("❌ [INIT] Error type: " + e.getClass().getName());
            System.err.println("❌ [INIT] Error message: " + e.getMessage());
            System.err.println("❌ [INIT] This could be due to:");
            System.err.println("   - Invalid credentials format");
            System.err.println("   - Network connectivity issues");
            System.err.println("   - SSL certificate problems");
            System.err.println("   - Service account configuration issues");
            e.printStackTrace();
            throw e;
        }

        if (credential.getServiceAccountId() != null) {
            System.out.println("✅ [INIT] Service account: " + credential.getServiceAccountId());
        } else {
            System.err.println("⚠️ [INIT] Service account ID is null");
        }

        // Create Drive service
        System.out.println("🔧 [INIT] Building Drive service...");
        try {
            driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            System.out.println("✅ [INIT] Google Drive service built successfully!");
        } catch (Exception e) {
            System.err.println("❌ [INIT] Failed to build Drive service!");
            System.err.println("❌ [INIT] Error: " + e.getMessage());
            throw e;
        }

        // Initialize parent backup folder
        try {
            initializeParentFolder();
        } catch (Exception e) {
            System.err.println("❌ [INIT] Failed to initialize parent folder!");
            System.err.println("❌ [INIT] Error: " + e.getMessage());
            throw e;
        }

        // Quick API test
        System.out.println("🔧 [INIT] Testing API connection...");
        try {
            var about = driveService.about().get().setFields("user, storageQuota").execute();
            System.out.println("✅ [INIT] API connection test successful!");
            if (about.getUser() != null) {
                System.out.println("✅ [INIT] Connected as: " + about.getUser().getDisplayName());
            }
        } catch (Exception e) {
            System.err.println("⚠️ [INIT] API test warning: " + e.getMessage());
            // Don't fail initialization, just warn
        }

        System.out.println("✅ [INIT] Ready to perform backups!");

    } catch (FileNotFoundException e) {
        System.err.println("❌ [INIT] File error: " + e.getMessage());
        e.printStackTrace();
        driveService = null;
    } catch (IOException e) {
        System.err.println("❌ [INIT] IO error: " + e.getMessage());
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
    } finally {
        // Clean up temp file
        if (tempCredFile != null && tempCredFile.exists()) {
            try {
                System.out.println("🔧 [INIT] Cleaning up temp credentials file...");
                boolean deleted = tempCredFile.delete();
                System.out.println("✅ [INIT] Temp file deleted: " + deleted);
            } catch (Exception e) {
                System.err.println("⚠️ [INIT] Could not delete temp file: " + e.getMessage());
            }
        }
    }

    System.out.println("🔧 [INIT] Initialization complete. Service: " + (driveService != null ? "✅ READY" : "❌ FAILED"));
}
    public void testCredentials() {
        try {
            System.out.println("🧪 Testing credentials file...");
            InputStream stream = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (stream != null) {
                String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("✅ Credentials content length: " + content.length());
                System.out.println("✅ Has private_key: " + content.contains("private_key"));
                System.out.println("✅ Has client_email: " + content.contains("client_email"));
                stream.close();
            } else {
                System.err.println("❌ Cannot read credentials file");
            }
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
        }
    }
    private void initializeParentFolder() throws Exception {
        System.out.println("🔧 [FOLDER] Looking for parent backup folder: " + PARENT_BACKUP_FOLDER_NAME);

        // Search for existing parent folder
        String query = "mimeType='application/vnd.google-apps.folder' and name='" + PARENT_BACKUP_FOLDER_NAME + "' and trashed=false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (result.getFiles().isEmpty()) {
            System.out.println("📁 [FOLDER] Parent folder not found, creating new one...");
            // Create parent folder
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
            // Check if service is initialized
            System.out.println("🔧 [BACKUP] Checking Drive service status...");
            if (driveService == null) {
                System.err.println("❌ [BACKUP] Drive service is NULL!");
                throw new Exception("Google Drive service not initialized. Check server logs for initialization errors.");
            }

            if (parentFolderId == null) {
                System.err.println("❌ [BACKUP] Parent folder not initialized!");
                throw new Exception("Parent backup folder not available.");
            }

            System.out.println("✅ [BACKUP] Drive service is ready");
            System.out.println("✅ [BACKUP] Parent folder ID: " + parentFolderId);

            // Create backup folder with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFolderName = "DocumentHub_Backup_" + timestamp;

            System.out.println("📦 [BACKUP] Creating backup folder: " + backupFolderName);
            System.out.println("🔧 [BACKUP] Preparing folder metadata...");

            // Create folder in the parent backup folder
            File folderMetadata = new File();
            folderMetadata.setName(backupFolderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList(parentFolderId));

            System.out.println("🔧 [BACKUP] Sending folder creation request to Google Drive API...");
            File backupFolder;
            try {
                backupFolder = driveService.files().create(folderMetadata)
                        .setFields("id, name")
                        .execute();
            } catch (Exception e) {
                System.err.println("❌ [BACKUP] Failed to create folder in Google Drive!");
                System.err.println("❌ [BACKUP] Error: " + e.getMessage());
                System.err.println("❌ [BACKUP] This could mean:");
                System.err.println("   - Google Drive API is not enabled in your project");
                System.err.println("   - Service account doesn't have proper permissions");
                System.err.println("   - Network connectivity issues");
                throw e;
            }

            String folderId = backupFolder.getId();
            result.put("folderId", folderId);
            result.put("folderName", backupFolderName);
            result.put("parentFolder", PARENT_BACKUP_FOLDER_NAME);

            System.out.println("✅ [BACKUP] Backup folder created successfully!");
            System.out.println("✅ [BACKUP] Folder ID: " + folderId);
            System.out.println("✅ [BACKUP] Folder Name: " + backupFolder.getName());
            System.out.println("✅ [BACKUP] Parent Folder: " + PARENT_BACKUP_FOLDER_NAME);

            // Step 1: Backup database
            result.put("progress", 25);
            result.put("status", "Backing up database...");
            System.out.println("\n--- Step 1/3: Database Backup ---");
            backupDatabase(folderId);
            System.out.println("✅ [DATABASE] Completed!");

            // Step 2: Backup documents
            result.put("progress", 50);
            result.put("status", "Backing up documents...");
            System.out.println("\n--- Step 2/3: Documents Backup ---");
            backupDocuments(folderId);
            System.out.println("✅ [DOCUMENTS] Completed!");

            // Step 3: Create backup info
            result.put("progress", 75);
            result.put("status", "Creating backup summary...");
            System.out.println("\n--- Step 3/3: Backup Info ---");
            createBackupInfoFile(folderId, backupFolderName);
            System.out.println("✅ [INFO] Completed!");

            // Complete
            result.put("progress", 100);
            result.put("status", "Backup completed successfully!");
            result.put("success", true);
            result.put("message", "Backup completed successfully to Google Drive in folder: " + PARENT_BACKUP_FOLDER_NAME);

            System.out.println("\n========================================");
            System.out.println("✅ [BACKUP] BACKUP COMPLETED!");
            System.out.println("✅ [BACKUP] Parent Folder: " + PARENT_BACKUP_FOLDER_NAME);
            System.out.println("✅ [BACKUP] Backup Folder: " + backupFolderName);
            System.out.println("========================================\n");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Backup failed: " + e.getMessage());
            result.put("progress", 0);
            result.put("status", "Backup failed");

            System.err.println("\n========================================");
            System.err.println("❌ [BACKUP] BACKUP FAILED!");
            System.err.println("❌ [BACKUP] Error: " + e.getClass().getName());
            System.err.println("❌ [BACKUP] Message: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            System.err.println("========================================\n");
        }
        return result;
    }

    // ... rest of your methods (backupDatabase, backupDocuments, createBackupInfoFile, listBackups) remain the same
    // but they will now automatically use the parent folder structure

    private void backupDatabase(String folderId) throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String sqlFileName = "database_backup_" + timestamp + ".sql";
        java.io.File tempFile = new java.io.File(sqlFileName);

        try (Connection conn = DatabaseConnection.getConnection();
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            writer.println("-- Database Backup - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println();

            // Users
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

            // Documents
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

            // Activity logs
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

            // Upload to Drive
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

            // Upload
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

        // Search for backup folders within the parent folder
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