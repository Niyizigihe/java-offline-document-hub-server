//package com.example.offlinedocumenthubserver;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import io.javalin.Javalin;
//import io.javalin.http.Context;
//import io.javalin.http.UploadedFile;
//
//import java.io.File;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//import java.sql.*;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class RESTServer {
//    private static final int PORT = 8080;
//    private static Javalin app;
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    static {
//        // Configure ObjectMapper for Java 8 dates
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//    }
//
//    public static void startRESTServer() {
//        app = Javalin.create(config -> {
//            config.http.defaultContentType = "application/json";
//            config.plugins.enableCors(cors -> {
//                cors.add(it -> {
//                    it.anyHost();
//                });
//            });
//        }).start("0.0.0.0", PORT);
//
//        setupRoutes();
//        System.out.println("🚀 REST Server started on port " + PORT);
//        System.out.println("📡 API available at: http://localhost:" + PORT + "/api");
//        System.out.println("🌐 LAN access: http://[YOUR_IP]:" + PORT + "/api");
//    }
//
//    private static void setupRoutes() {
//        // Health check
//        app.get("/api/health", RESTServer::healthCheck);
//        app.get("/api/system/health", RESTServer::systemHealth);
//
//        // Authentication
//        app.post("/api/login", RESTServer::handleLogin);
//        app.post("/api/register", RESTServer::handleRegister);
//
//        // Documents
//        app.get("/api/documents", RESTServer::getAllDocuments);
//        app.post("/api/documents", RESTServer::createDocument);
//        app.delete("/api/documents/{id}", RESTServer::deleteDocument);
//
//        // Users (admin only)
//        app.get("/api/users", RESTServer::getAllUsers);
//        app.post("/api/users", RESTServer::createUser);
//        app.put("/api/users/{id}", RESTServer::updateUser);
//        app.delete("/api/users/{id}", RESTServer::deleteUser);
//
//        // Activity logs
//        app.get("/api/activity-logs", RESTServer::getActivityLogs);
//    }
//
//    // ============ HEALTH ENDPOINTS ============
//    private static void healthCheck(Context ctx) {
//        ctx.json(createSuccessResponse("Server is running"));
//    }
//
//    private static void systemHealth(Context ctx) {
//        Map<String, Object> healthData = new HashMap<>();
//        try {
//            // Test database connection
//            try (Connection conn = DatabaseConnection.getConnection()) {
//                healthData.put("database", "Connected");
//
//                // Get counts
//                healthData.put("users_count", getTableCount(conn, "USERS"));
//                healthData.put("documents_count", getTableCount(conn, "DOCUMENTS"));
//                healthData.put("activity_logs_count", getTableCount(conn, "ACTIVITY_LOGS"));
//            }
//
//            // Check shared folder
//            File sharedFolder = new File("shared_documents");
//            if (sharedFolder.exists() && sharedFolder.isDirectory()) {
//                File[] files = sharedFolder.listFiles();
//                healthData.put("shared_folder", "Accessible");
//                healthData.put("shared_files_count", files != null ? files.length : 0);
//            } else {
//                healthData.put("shared_folder", "Not accessible");
//            }
//
//            ctx.json(createSuccessResponse("System health check", healthData));
//        } catch (Exception e) {
//            ctx.json(createErrorResponse("Health check failed: " + e.getMessage()));
//        }
//    }
//
//    private static int getTableCount(Connection conn, String tableName) throws SQLException {
//        String sql = "SELECT COUNT(*) as count FROM " + tableName;
//        try (PreparedStatement stmt = conn.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//            return rs.next() ? rs.getInt("count") : 0;
//        }
//    }
//
//    // ============ AUTHENTICATION ENDPOINTS ============
//    private static void handleLogin(Context ctx) {
//        try {
//            String username;
//            String password;
//            if (ctx.header("Content-Type") != null && ctx.header("Content-Type").contains("application/json")) {
//                Map<String, String> body = ctx.bodyAsClass(Map.class);
//                username = body.get("username");
//                password = body.get("password");
//            } else {
//                username = ctx.formParam("username");
//                password = ctx.formParam("password");
//            }
////            String username = ctx.formParam("username");
////            String password = ctx.formParam("password");
//
//            if (username == null || password == null || username.trim().isEmpty()) {
//                ctx.json(createErrorResponse("Username and password are required"));
//                return;
//            }
//
//            String hashedPass = hashPassword(password);
//            String sql = "SELECT user_id, username, role, full_name FROM users WHERE username = ? AND password_hash = ?";
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//                stmt.setString(1, username.trim());
//                stmt.setString(2, hashedPass);
//                ResultSet rs = stmt.executeQuery();
//
//                if (rs.next()) {
//                    int userId = rs.getInt("user_id");
//                    String role = rs.getString("role");
//                    String fullName = rs.getString("full_name");
//
//                    // Log successful login
//                    logActivity(userId, "LOGIN", "User logged in successfully");
//
//                    // Return user info
//                    Map<String, Object> userData = new HashMap<>();
//                    userData.put("userId", userId);
//                    userData.put("username", username);
//                    userData.put("role", role);
//                    userData.put("fullName", fullName != null ? fullName : "");
//
//                    ctx.json(createSuccessResponse("Login successful", userData));
//                } else {
//                    logActivity(-1, "LOGIN_FAILED", "Failed login attempt for: " + username);
//                    ctx.json(createErrorResponse("Invalid username or password"));
//                }
//            }
//        } catch (Exception e) {
//            logActivity(-1, "LOGIN_ERROR", "Login error: " + e.getMessage());
//            ctx.json(createErrorResponse("Login error: " + e.getMessage()));
//        }
//    }
//
//    private static void handleRegister(Context ctx) {
//        try {
//            String username = ctx.formParam("username");
//            String email = ctx.formParam("email");
//            String password = ctx.formParam("password");
//
//            if (username == null || password == null || username.trim().isEmpty()) {
//                ctx.json(createErrorResponse("Username and password are required"));
//                return;
//            }
//
//            String hashedPass = hashPassword(password);
//            String sql = "INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, 'student', ?)";
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//                stmt.setString(1, username.trim());
//                stmt.setString(2, hashedPass);
//                stmt.setString(3, username.trim()); // Use username as full name initially
//
//                int affectedRows = stmt.executeUpdate();
//                if (affectedRows > 0) {
//                    logActivity(-1, "REGISTER", "New user registered: " + username);
//                    ctx.json(createSuccessResponse("Registration successful"));
//                } else {
//                    ctx.json(createErrorResponse("Registration failed"));
//                }
//            }
//        } catch (SQLException e) {
//            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("Duplicate")) {
//                ctx.json(createErrorResponse("Username already exists"));
//            } else {
//                logActivity(-1, "REGISTER_ERROR", "Registration error: " + e.getMessage());
//                ctx.json(createErrorResponse("Registration error: " + e.getMessage()));
//            }
//        } catch (Exception e) {
//            logActivity(-1, "REGISTER_ERROR", "Registration error: " + e.getMessage());
//            ctx.json(createErrorResponse("Registration error: " + e.getMessage()));
//        }
//    }
//
//    // ============ DOCUMENT ENDPOINTS ============
//    private static void getAllDocuments(Context ctx) {
//        try {
//            List<Document> documents = new ArrayList<>();
//            String sql = "SELECT d.doc_id, d.title, d.file_path, d.upload_date, " +
//                    "u.username as uploaded_by_username, u.full_name as uploaded_by_fullname " +
//                    "FROM documents d " +
//                    "LEFT JOIN users u ON d.uploaded_by = u.user_id " +
//                    "ORDER BY d.upload_date DESC";
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql);
//                 ResultSet rs = stmt.executeQuery()) {
//
//                while (rs.next()) {
//                    String uploadedBy = rs.getString("uploaded_by_fullname");
//                    if (uploadedBy == null || uploadedBy.trim().isEmpty()) {
//                        uploadedBy = rs.getString("uploaded_by_username");
//                    }
//
//                    Document doc = new Document(
//                            rs.getInt("doc_id"),
//                            rs.getString("title"),
//                            rs.getString("file_path"),
//                            uploadedBy,
//                            rs.getDate("upload_date").toLocalDate()
//                    );
//
//                    // Set file size
//                    File file = new File(doc.getFilePath());
//                    if (file.exists()) {
//                        doc.setFileSize(file.length());
//                    }
//
//                    documents.add(doc);
//                }
//            }
//
//            ctx.json(objectMapper.writeValueAsString(documents));
//        } catch (Exception e) {
//            logActivity(-1, "DOCUMENTS_LOAD_ERROR", "Failed to load documents: " + e.getMessage());
//            ctx.json(createErrorResponse("Failed to load documents: " + e.getMessage()));
//        }
//    }
//
//    private static void createDocument(Context ctx) {
//        try {
//            String title = ctx.formParam("title");
//            UploadedFile uploadedFile = ctx.uploadedFile("file");
//
//            if (title == null || title.trim().isEmpty() || uploadedFile == null) {
//                ctx.json(createErrorResponse("Title and file are required"));
//                return;
//            }
//
//            // Get user ID from context (you might want to add authentication)
//            int userId = 1; // Default for now - you'll want to get this from session/token
//
//            // Copy file to shared folder
//            String originalFileName = uploadedFile.filename();
//            String safeTitle = makeFileNameSafe(title);
//            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//            String newFileName = safeTitle + "_" + timestamp + getFileExtension(originalFileName);
//
//            Path destinationPath = Path.of("shared_documents", newFileName);
//            Files.createDirectories(destinationPath.getParent());
//
//            try (InputStream inputStream = uploadedFile.content()) {
//                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
//            }
//
//            // Save to database
//            String sql = "INSERT INTO documents (title, file_path, uploaded_by, upload_date) VALUES (?, ?, ?, ?)";
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//                stmt.setString(1, title.trim());
//                stmt.setString(2, destinationPath.toString());
//                stmt.setInt(3, userId);
//                stmt.setDate(4, Date.valueOf(LocalDate.now()));
//
//                int affectedRows = stmt.executeUpdate();
//                if (affectedRows > 0) {
//                    logActivity(userId, "UPLOAD", "Uploaded document: " + title);
//                    ctx.json(createSuccessResponse("Document uploaded successfully"));
//                } else {
//                    ctx.json(createErrorResponse("Failed to save document"));
//                }
//            }
//
//        } catch (Exception e) {
//            logActivity(-1, "UPLOAD_ERROR", "Upload error: " + e.getMessage());
//            ctx.json(createErrorResponse("Upload failed: " + e.getMessage()));
//        }
//    }
//
//    private static void deleteDocument(Context ctx) {
//        try {
//            String docIdParam = ctx.pathParam("id");
//            int docId = Integer.parseInt(docIdParam);
//
//            // First get document details
//            String selectSql = "SELECT file_path, uploaded_by FROM documents WHERE doc_id = ?";
//            String filePath = null;
//            int uploadedBy = 0;
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
//
//                selectStmt.setInt(1, docId);
//                ResultSet rs = selectStmt.executeQuery();
//
//                if (rs.next()) {
//                    filePath = rs.getString("file_path");
//                    uploadedBy = rs.getInt("uploaded_by");
//                } else {
//                    ctx.json(createErrorResponse("Document not found"));
//                    return;
//                }
//            }
//
//            // Delete from database
//            String deleteSql = "DELETE FROM documents WHERE doc_id = ?";
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
//
//                deleteStmt.setInt(1, docId);
//                int affectedRows = deleteStmt.executeUpdate();
//
//                if (affectedRows > 0) {
//                    // Delete physical file
//                    if (filePath != null) {
//                        File file = new File(filePath);
//                        if (file.exists()) {
//                            file.delete();
//                        }
//                    }
//
//                    logActivity(uploadedBy, "DELETE", "Deleted document ID: " + docId);
//                    ctx.json(createSuccessResponse("Document deleted successfully"));
//                } else {
//                    ctx.json(createErrorResponse("Document not found"));
//                }
//            }
//        } catch (Exception e) {
//            logActivity(-1, "DELETE_ERROR", "Delete error: " + e.getMessage());
//            ctx.json(createErrorResponse("Delete failed: " + e.getMessage()));
//        }
//    }
//
//    // ============ USER MANAGEMENT ENDPOINTS ============
//    private static void getAllUsers(Context ctx) {
//        try {
//            List<User> users = new ArrayList<>();
//            String sql = "SELECT user_id, username, full_name, role FROM users ORDER BY user_id";
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql);
//                 ResultSet rs = stmt.executeQuery()) {
//
//                while (rs.next()) {
//                    User user = new User(
//                            rs.getInt("user_id"),
//                            rs.getString("username"),
//                            rs.getString("full_name"),
//                            "", // Don't return password
//                            rs.getString("role")
//                    );
//                    users.add(user);
//                }
//            }
//
//            ctx.json(objectMapper.writeValueAsString(users));
//        } catch (Exception e) {
//            logActivity(-1, "USERS_LOAD_ERROR", "Failed to load users: " + e.getMessage());
//            ctx.json(createErrorResponse("Failed to load users: " + e.getMessage()));
//        }
//    }
//
//    private static void createUser(Context ctx) {
//        try {
//            User user = ctx.bodyAsClass(User.class);
//
//            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
//                ctx.json(createErrorResponse("Username is required"));
//                return;
//            }
//
//            String hashedPassword = hashPassword(user.getPassword() != null ? user.getPassword() : "password");
//            String sql = "INSERT INTO users (username, full_name, password_hash, role) VALUES (?, ?, ?, ?)";
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//                stmt.setString(1, user.getUsername().trim());
//                stmt.setString(2, user.getFullName() != null ? user.getFullName().trim() : user.getUsername());
//                stmt.setString(3, hashedPassword);
//                stmt.setString(4, user.getRole() != null ? user.getRole() : "student");
//
//                int affectedRows = stmt.executeUpdate();
//                if (affectedRows > 0) {
//                    logActivity(-1, "USER_CREATE", "Created user: " + user.getUsername());
//                    ctx.json(createSuccessResponse("User created successfully"));
//                } else {
//                    ctx.json(createErrorResponse("Failed to create user"));
//                }
//            }
//        } catch (SQLException e) {
//            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("Duplicate")) {
//                ctx.json(createErrorResponse("Username already exists"));
//            } else {
//                logActivity(-1, "USER_CREATE_ERROR", "User creation error: " + e.getMessage());
//                ctx.json(createErrorResponse("User creation error: " + e.getMessage()));
//            }
//        } catch (Exception e) {
//            logActivity(-1, "USER_CREATE_ERROR", "User creation error: " + e.getMessage());
//            ctx.json(createErrorResponse("User creation error: " + e.getMessage()));
//        }
//    }
//
//    private static void updateUser(Context ctx) {
//        try {
//            String userIdParam = ctx.pathParam("id");
//            int userId = Integer.parseInt(userIdParam);
//            User user = ctx.bodyAsClass(User.class);
//
//            // Build dynamic SQL based on what's being updated
//            StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
//            List<Object> params = new ArrayList<>();
//
//            if (user.getUsername() != null) {
//                sqlBuilder.append("username = ?, ");
//                params.add(user.getUsername().trim());
//            }
//            if (user.getFullName() != null) {
//                sqlBuilder.append("full_name = ?, ");
//                params.add(user.getFullName().trim());
//            }
//            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
//                sqlBuilder.append("password_hash = ?, ");
//                params.add(hashPassword(user.getPassword()));
//            }
//            if (user.getRole() != null) {
//                sqlBuilder.append("role = ?, ");
//                params.add(user.getRole());
//            }
//
//            // Remove trailing comma and space
//            if (sqlBuilder.charAt(sqlBuilder.length() - 2) == ',') {
//                sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
//            }
//
//            sqlBuilder.append(" WHERE user_id = ?");
//            params.add(userId);
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
//
//                for (int i = 0; i < params.size(); i++) {
//                    stmt.setObject(i + 1, params.get(i));
//                }
//
//                int affectedRows = stmt.executeUpdate();
//                if (affectedRows > 0) {
//                    logActivity(-1, "USER_UPDATE", "Updated user ID: " + userId);
//                    ctx.json(createSuccessResponse("User updated successfully"));
//                } else {
//                    ctx.json(createErrorResponse("User not found"));
//                }
//            }
//        } catch (Exception e) {
//            logActivity(-1, "USER_UPDATE_ERROR", "User update error: " + e.getMessage());
//            ctx.json(createErrorResponse("User update error: " + e.getMessage()));
//        }
//    }
//
//    private static void deleteUser(Context ctx) {
//        try {
//            String userIdParam = ctx.pathParam("id");
//            int userId = Integer.parseInt(userIdParam);
//
//            String sql = "DELETE FROM users WHERE user_id = ?";
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//                stmt.setInt(1, userId);
//                int affectedRows = stmt.executeUpdate();
//
//                if (affectedRows > 0) {
//                    logActivity(-1, "USER_DELETE", "Deleted user ID: " + userId);
//                    ctx.json(createSuccessResponse("User deleted successfully"));
//                } else {
//                    ctx.json(createErrorResponse("User not found"));
//                }
//            }
//        } catch (Exception e) {
//            logActivity(-1, "USER_DELETE_ERROR", "User delete error: " + e.getMessage());
//            ctx.json(createErrorResponse("User delete error: " + e.getMessage()));
//        }
//    }
//
//    // ============ ACTIVITY LOGS ENDPOINT ============
//    private static void getActivityLogs(Context ctx) {
//        try {
//            List<ActivityLog> activityLogs = new ArrayList<>();
//            String sql = "SELECT al.log_id, al.user_id, u.username, u.full_name, " +
//                    "al.action_type, al.action_details, al.timestamp " +
//                    "FROM activity_logs al " +
//                    "LEFT JOIN users u ON al.user_id = u.user_id " +
//                    "ORDER BY al.timestamp DESC";
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql);
//                 ResultSet rs = stmt.executeQuery()) {
//
//                while (rs.next()) {
//                    String username = rs.getString("username");
//                    String fullName = rs.getString("full_name");
//                    String displayName = fullName != null && !fullName.trim().isEmpty()
//                            ? fullName + " (" + username + ")"
//                            : username;
//
//                    ActivityLog log = new ActivityLog(
//                            rs.getInt("log_id"),
//                            rs.getInt("user_id"),
//                            displayName,
//                            rs.getString("action_type"),
//                            rs.getString("action_details"),
//                            rs.getTimestamp("timestamp").toLocalDateTime()
//                    );
//                    activityLogs.add(log);
//                }
//            }
//
//            ctx.json(objectMapper.writeValueAsString(activityLogs));
//        } catch (Exception e) {
//            logActivity(-1, "LOGS_LOAD_ERROR", "Failed to load activity logs: " + e.getMessage());
//            ctx.json(createErrorResponse("Failed to load activity logs: " + e.getMessage()));
//        }
//    }
//
//    // ============ HELPER METHODS ============
//    private static String hashPassword(String password) {
//        return String.valueOf(password.hashCode());
//    }
//
//    private static void logActivity(int userId, String actionType, String details) {
//        String sql = "INSERT INTO activity_logs (user_id, action_type, action_details) VALUES (?, ?, ?)";
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            ps.setString(2, actionType);
//            ps.setString(3, details);
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("Failed to log activity: " + e.getMessage());
//        }
//    }
//
//    private static Map<String, Object> createSuccessResponse(String message) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("message", message);
//        return response;
//    }
//
//    private static Map<String, Object> createSuccessResponse(String message, Object data) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("message", message);
//        response.put("data", data);
//        return response;
//    }
//
//    private static Map<String, Object> createErrorResponse(String message) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", false);
//        response.put("message", message);
//        return response;
//    }
//
//    private static String makeFileNameSafe(String fileName) {
//        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
//    }
//
//    private static String getFileExtension(String fileName) {
//        int lastDotIndex = fileName.lastIndexOf(".");
//        return (lastDotIndex > 0) ? fileName.substring(lastDotIndex) : "";
//    }
//
//    public static void stopServer() {
//        if (app != null) {
//            app.stop();
//        }
//    }
//}
//




package com.example.offlinedocumenthubserver;

import com.example.offlinedocumenthubserver.dto.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RESTServer {
    private static final int PORT = 8080;
    private static Javalin app;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Session management - store active sessions
    private static final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
//    private static final long SESSION_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours
    private static final long SESSION_TIMEOUT = 10 * 60 * 1000; // 10 minutes in milliseconds

    static {
        // Configure ObjectMapper for Java 8 dates
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void startRESTServer() {
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });
        }).start("0.0.0.0", PORT);

        setupRoutes();
        startSessionCleanupTask();
        System.out.println("🚀 REST Server started on port " + PORT);
        System.out.println("📡 API available at: http://localhost:" + PORT + "/api");
        System.out.println("🌐 LAN access: http://[YOUR_IP]:" + PORT + "/api");
    }

    private static void setupRoutes() {
        // Health check
        app.get("/api/health", RESTServer::healthCheck);
        app.get("/api/system/health", RESTServer::systemHealth);

        // Authentication
        app.post("/api/login", RESTServer::handleLogin);
        app.post("/api/register", RESTServer::handleRegister);
        app.post("/api/logout", RESTServer::handleLogout);

        // Documents (require authentication)
        app.get("/api/documents", RESTServer::getAllDocuments);
        app.post("/api/documents", RESTServer::createDocument);
        app.put("/api/documents/{id}", RESTServer::updateDocument);
        app.delete("/api/documents/{id}", RESTServer::deleteDocument);
        app.get("/api/documents/{id}/download", RESTServer::downloadDocument);

        // Users (admin only)
        app.get("/api/users", RESTServer::getAllUsers);
        app.post("/api/users", RESTServer::createUser);
        app.put("/api/users/{id}", RESTServer::updateUser);
        app.delete("/api/users/{id}", RESTServer::deleteUser);

        // Messages routes
        app.get("/api/messages/conversations", RESTServer::getConversations);
        app.get("/api/messages/{userId}", RESTServer::getMessages);
        app.post("/api/messages", RESTServer::sendMessage);
        app.post("/api/messages/{userId}/read", RESTServer::markMessagesAsRead);

        // Activity logs (admin only)
        app.get("/api/activity-logs", RESTServer::getActivityLogs);

        // Backup routes
        app.post("/api/backup/trigger", RESTServer::triggerBackup);
        app.get("/api/backup/progress", RESTServer::getBackupProgress);
        app.get("/api/backup/list", RESTServer::listBackups);
//        app.get("/api/backup/stats", RESTServer::getBackupStats);
    }

    // ============ SESSION MANAGEMENT ============
    private static class UserSession {
        private final int userId;
        private final String username;
        private final String role;
        private final String fullName;
        private final long createdAt;
        private long lastAccessed;

        public UserSession(int userId, String username, String role, String fullName) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.fullName = fullName;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessed = this.createdAt;
        }

        public void updateLastAccessed() {
            this.lastAccessed = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - lastAccessed) > SESSION_TIMEOUT;
        }
    }

    private static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private static UserSession getSessionFromToken(String authToken) {
        if (authToken == null || !activeSessions.containsKey(authToken)) {
            return null;
        }

        UserSession session = activeSessions.get(authToken);
        if (session.isExpired()) {
            activeSessions.remove(authToken);
            return null;
        }

        session.updateLastAccessed();
        return session;
    }

    private static void startSessionCleanupTask() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cleanExpiredSessions();
            }
        }, 0, 60 * 60 * 1000); // Run every hour
    }

    private static void cleanExpiredSessions() {
        activeSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // ============ AUTHENTICATION MIDDLEWARE ============
    private static UserSession requireAuth(Context ctx) {
        String authToken = ctx.header("Authorization");
        if (authToken != null && authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7);
        }

        UserSession session = getSessionFromToken(authToken);
        if (session == null) {
            ctx.status(401).json(createErrorResponse("Authentication required"));
            return null;
        }
        return session;
    }

    private static UserSession requireAdmin(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return null;

        if (!"admin".equals(session.role)) {
            ctx.status(403).json(createErrorResponse("Admin access required"));
            return null;
        }
        return session;
    }

    // ============ HEALTH ENDPOINTS ============
    private static void healthCheck(Context ctx) {
        ctx.json(createSuccessResponse("Server is running"));
    }

    private static void systemHealth(Context ctx) {
        Map<String, Object> healthData = new HashMap<>();
        try {
            // Test database connection
            try (Connection conn = DatabaseConnection.getConnection()) {
                healthData.put("database", "Connected");

                // Get counts
                healthData.put("users_count", getTableCount(conn, "users"));
                healthData.put("documents_count", getTableCount(conn, "documents"));
                healthData.put("activity_logs_count", getTableCount(conn, "activity_logs"));
                healthData.put("active_sessions", activeSessions.size());
            }

            // Check shared folder
            File sharedFolder = new File("shared_documents");
            if (sharedFolder.exists() && sharedFolder.isDirectory()) {
                File[] files = sharedFolder.listFiles();
                healthData.put("shared_folder", "Accessible");
                healthData.put("shared_files_count", files != null ? files.length : 0);
            } else {
                healthData.put("shared_folder", "Not accessible");
            }

            ctx.json(createSuccessResponse("System health check", healthData));
        } catch (Exception e) {
            ctx.json(createErrorResponse("Health check failed: " + e.getMessage()));
        }
    }

    private static int getTableCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("count") : 0;
        }
    }

    // ============ AUTHENTICATION ENDPOINTS ============
    private static void handleLogin(Context ctx) {
        try {
            String username;
            String password;

            if (ctx.header("Content-Type") != null && ctx.header("Content-Type").contains("application/json")) {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                username = body.get("username");
                password = body.get("password");
            } else {
                username = ctx.formParam("username");
                password = ctx.formParam("password");
            }

            if (username == null || password == null || username.trim().isEmpty()) {
                ctx.json(createErrorResponse("Username and password are required"));
                return;
            }

            String hashedPass = hashPassword(password);
            String sql = "SELECT user_id, username, role, full_name FROM users WHERE username = ? AND password_hash = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username.trim());
                stmt.setString(2, hashedPass);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String role = rs.getString("role");
                    String fullName = rs.getString("full_name");

                    // Create session
                    String authToken = generateAuthToken();
                    UserSession session = new UserSession(userId, username, role, fullName);
                    activeSessions.put(authToken, session);

                    // Log successful login
                    logActivity(userId, "LOGIN", "User logged in successfully");

                    // Return user info with auth token
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", userId);
                    userData.put("username", username);
                    userData.put("role", role);
                    userData.put("fullName", fullName != null ? fullName : "");
                    userData.put("authToken", authToken);

                    ctx.json(createSuccessResponse("Login successful", userData));
                } else {
                    logActivity(-1, "LOGIN_FAILED", "Failed login attempt for: " + username);
                    ctx.json(createErrorResponse("Invalid username or password"));
                }
            }
        } catch (Exception e) {
            logActivity(-1, "LOGIN_ERROR", "Login error: " + e.getMessage());
            ctx.json(createErrorResponse("Login error: " + e.getMessage()));
        }
    }

    private static void handleLogout(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            if (authToken != null && authToken.startsWith("Bearer ")) {
                authToken = authToken.substring(7);
                UserSession session = activeSessions.remove(authToken);
                if (session != null) {
                    logActivity(session.userId, "LOGOUT", "User logged out");
                }
            }
            ctx.json(createSuccessResponse("Logout successful"));
        } catch (Exception e) {
            ctx.json(createErrorResponse("Logout error: " + e.getMessage()));
        }
    }

    private static void handleRegister(Context ctx) {
        try {
            String username = ctx.formParam("username");
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");

            if (username == null || password == null || username.trim().isEmpty()) {
                ctx.json(createErrorResponse("Username and password are required"));
                return;
            }

            String hashedPass = hashPassword(password);
            String sql = "INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, 'student', ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username.trim());
                stmt.setString(2, hashedPass);
                stmt.setString(3, username.trim()); // Use username as full name initially

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    logActivity(-1, "REGISTER", "New user registered: " + username);
                    ctx.json(createSuccessResponse("Registration successful"));
                } else {
                    ctx.json(createErrorResponse("Registration failed"));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("Duplicate")) {
                ctx.json(createErrorResponse("Username already exists"));
            } else {
                logActivity(-1, "REGISTER_ERROR", "Registration error: " + e.getMessage());
                ctx.json(createErrorResponse("Registration error: " + e.getMessage()));
            }
        } catch (Exception e) {
            logActivity(-1, "REGISTER_ERROR", "Registration error: " + e.getMessage());
            ctx.json(createErrorResponse("Registration error: " + e.getMessage()));
        }
    }

    // ============ DOCUMENT ENDPOINTS ============
    private static void getAllDocuments(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            List<Document> documents = new ArrayList<>();
            // FIXED: Use uploaded_by (VARCHAR) for display, user_id for ownership
            String sql = "SELECT d.doc_id, d.title, d.file_path, d.upload_date, " +
                    "d.uploaded_by, d.user_id, d.file_size, " +
                    "u.username as uploaded_by_username, u.full_name as uploaded_by_fullname " +
                    "FROM documents d " +
                    "LEFT JOIN users u ON d.user_id = u.user_id " +
                    "ORDER BY d.upload_date DESC";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    // Use the uploaded_by field directly (it's already a VARCHAR for display name)
                    String uploadedBy = rs.getString("uploaded_by");

                    Document doc = new Document(
                            rs.getInt("doc_id"),
                            rs.getString("title"),
                            rs.getString("file_path"),
                            uploadedBy,
                            rs.getDate("upload_date").toLocalDate(),
                            rs.getInt("user_id")
                    );

                    // Set file size from database (it's stored as BIGINT)
                    long fileSize = rs.getLong("file_size");
                    doc.setFileSize(fileSize); // This will format it properly

                    documents.add(doc);
                }
            }

            ctx.json(objectMapper.writeValueAsString(documents));
        } catch (Exception e) {
            logActivity(session.userId, "DOCUMENTS_LOAD_ERROR", "Failed to load documents: " + e.getMessage());
            ctx.json(createErrorResponse("Failed to load documents: " + e.getMessage()));
        }
    }

    // In RESTServer.java - add this download endpoint
//    private static void downloadDocument(Context ctx) {
//        UserSession session = requireAuth(ctx);
//        if (session == null) return;
//
//        try {
//            String docIdParam = ctx.pathParam("id");
//            int docId = Integer.parseInt(docIdParam);
//
//            // Get document details from database
//            String sql = "SELECT file_path, title FROM documents WHERE doc_id = ?";
//            String filePath = null;
//            String title = null;
//
//            try (Connection conn = DatabaseConnection.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//                stmt.setInt(1, docId);
//                ResultSet rs = stmt.executeQuery();
//
//                if (rs.next()) {
//                    filePath = rs.getString("file_path");
//                    title = rs.getString("title");
//                } else {
//                    ctx.json(createErrorResponse("Document not found"));
//                    return;
//                }
//            }
//
//            // Check if file exists
//            File file = new File(filePath);
//            if (!file.exists()) {
//                ctx.json(createErrorResponse("File not found on server"));
//                return;
//            }
//
//            // Set headers for file download
//            ctx.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
//            ctx.header("Content-Type", "application/octet-stream");
//            ctx.header("Content-Length", String.valueOf(file.length()));
//
//            // Send the file
//            ctx.result(file);
//
//            // Log the download activity
//            logActivity(session.userId, "DOWNLOAD", "Downloaded document: " + title);
//
//        } catch (Exception e) {
//            logActivity(session.userId, "DOWNLOAD_ERROR", "Download error: " + e.getMessage());
//            ctx.json(createErrorResponse("Download failed: " + e.getMessage()));
//        }
//    }
    private static void downloadDocument(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        InputStream fileStream = null;
        try {
            String docIdParam = ctx.pathParam("id");
            int docId = Integer.parseInt(docIdParam);

            // Get document details from database
            String sql = "SELECT file_path, title FROM documents WHERE doc_id = ?";
            String filePath = null;
            String title = null;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, docId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    filePath = rs.getString("file_path");
                    title = rs.getString("title");
                    System.out.println("Found document: " + title + " at path: " + filePath);
                } else {
                    System.err.println("Document not found in database: " + docId);
                    ctx.status(404).json(createErrorResponse("Document not found"));
                    return;
                }
            }

            // Check if file exists
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found on server: " + filePath);
                System.err.println("Absolute path: " + file.getAbsolutePath());
                ctx.status(404).json(createErrorResponse("File not found on server"));
                return;
            }

            System.out.println("Sending file: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");

            // Set headers for file download
            ctx.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            ctx.header("Content-Type", "application/octet-stream");
            ctx.header("Content-Length", String.valueOf(file.length()));

            // CORRECTED: Send the file as InputStream (better for large files)
            fileStream = new FileInputStream(file);
            ctx.result(fileStream);

            // Log the download activity
            logActivity(session.userId, "DOWNLOAD", "Downloaded document: " + title);

        } catch (Exception e) {
            System.err.println("DOWNLOAD ERROR: " + e.getMessage());
            e.printStackTrace();
            logActivity(session.userId, "DOWNLOAD_ERROR", "Download error: " + e.getMessage());
            ctx.status(500).json(createErrorResponse("Download failed: " + e.getMessage()));

            // Close stream if open
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException ex) {
                    System.err.println("Error closing file stream: " + ex.getMessage());
                }
            }
        }
    }
    private static void createDocument(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            String title = ctx.formParam("title");
            UploadedFile uploadedFile = ctx.uploadedFile("file");

            if (title == null || title.trim().isEmpty() || uploadedFile == null) {
                ctx.json(createErrorResponse("Title and file are required"));
                return;
            }

            // Copy file to shared folder
            String originalFileName = uploadedFile.filename();
            String safeTitle = makeFileNameSafe(title);
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String newFileName = safeTitle + "_" + timestamp + getFileExtension(originalFileName);

            Path destinationPath = Path.of("shared_documents", newFileName);
            Files.createDirectories(destinationPath.getParent());

            try (InputStream inputStream = uploadedFile.content()) {
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Get file size
            long fileSize = Files.size(destinationPath);

            // FIXED: Save to database with both uploaded_by and user_id
            String sql = "INSERT INTO documents (title, file_path, uploaded_by, upload_date, user_id, file_size) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, title.trim());
                stmt.setString(2, destinationPath.toString());
                stmt.setString(3, session.fullName != null ? session.fullName : session.username); // uploaded_by (VARCHAR)
                stmt.setDate(4, Date.valueOf(LocalDate.now()));
                stmt.setInt(5, session.userId); // user_id (INT)
                stmt.setLong(6, fileSize); // file_size (BIGINT)

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    logActivity(session.userId, "UPLOAD", "Uploaded document: " + title);
                    ctx.json(createSuccessResponse("Document uploaded successfully"));
                } else {
                    ctx.json(createErrorResponse("Failed to save document"));
                }
            }

        } catch (Exception e) {
            logActivity(session.userId, "UPLOAD_ERROR", "Upload error: " + e.getMessage());
            ctx.json(createErrorResponse("Upload failed: " + e.getMessage()));
        }
    }

    private static void deleteDocument(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            String docIdParam = ctx.pathParam("id");
            int docId = Integer.parseInt(docIdParam);

            // First get document details and check ownership
            String selectSql = "SELECT file_path, user_id FROM documents WHERE doc_id = ?";
            String filePath = null;
            int documentOwnerId = 0;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

                selectStmt.setInt(1, docId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    filePath = rs.getString("file_path");
                    documentOwnerId = rs.getInt("user_id");

                    // Check if user owns the document or is admin
                    if (documentOwnerId != session.userId && !"admin".equals(session.role)) {
                        ctx.json(createErrorResponse("You can only delete your own documents"));
                        return;
                    }
                } else {
                    ctx.json(createErrorResponse("Document not found"));
                    return;
                }
            }

            // Delete from database
            String deleteSql = "DELETE FROM documents WHERE doc_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

                deleteStmt.setInt(1, docId);
                int affectedRows = deleteStmt.executeUpdate();

                if (affectedRows > 0) {
                    // Delete physical file
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }

                    logActivity(session.userId, "DELETE", "Deleted document ID: " + docId);
                    ctx.json(createSuccessResponse("Document deleted successfully"));
                } else {
                    ctx.json(createErrorResponse("Document not found"));
                }
            }
        } catch (Exception e) {
            logActivity(session.userId, "DELETE_ERROR", "Delete error: " + e.getMessage());
            ctx.json(createErrorResponse("Delete failed: " + e.getMessage()));
        }
    }

    // ============ USER MANAGEMENT ENDPOINTS ============
    // In getAllUsers method - FIXED
    // In RESTServer.java - update getAllUsers method
    private static void getAllUsers(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            List<User> users = new ArrayList<>();
            String sql = "SELECT user_id, username, full_name, role FROM users WHERE user_id != ? ORDER BY full_name, username";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, session.userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            "", // Don't return password
                            rs.getString("role")
                    );
                    users.add(user);
                }
            }

            ctx.json(objectMapper.writeValueAsString(users));
        } catch (Exception e) {
            logActivity(session.userId, "USERS_LOAD_ERROR", "Failed to load users: " + e.getMessage());
            ctx.json(createErrorResponse("Failed to load users: " + e.getMessage()));
        }
    }

    private static void createUser(Context ctx) {
        UserSession session = requireAdmin(ctx);
        if (session == null) return;

        try {
            User user = ctx.bodyAsClass(User.class);

            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                ctx.json(createErrorResponse("Username is required"));
                return;
            }

            String hashedPassword = hashPassword(user.getPassword() != null ? user.getPassword() : "password");
            String sql = "INSERT INTO users (username, full_name, password_hash, role) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getUsername().trim());
                stmt.setString(2, user.getFullName() != null ? user.getFullName().trim() : user.getUsername());
                stmt.setString(3, hashedPassword);
                stmt.setString(4, user.getRole() != null ? user.getRole() : "student");

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    logActivity(session.userId, "USER_CREATE", "Created user: " + user.getUsername());
                    ctx.json(createSuccessResponse("User created successfully"));
                } else {
                    ctx.json(createErrorResponse("Failed to create user"));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("Duplicate")) {
                ctx.json(createErrorResponse("Username already exists"));
            } else {
                logActivity(session.userId, "USER_CREATE_ERROR", "User creation error: " + e.getMessage());
                ctx.json(createErrorResponse("User creation error: " + e.getMessage()));
            }
        } catch (Exception e) {
            logActivity(session.userId, "USER_CREATE_ERROR", "User creation error: " + e.getMessage());
            ctx.json(createErrorResponse("User creation error: " + e.getMessage()));
        }
    }

    private static void updateUser(Context ctx) {
        UserSession session = requireAdmin(ctx);
        if (session == null) return;

        try {
            String userIdParam = ctx.pathParam("id");
            int userId = Integer.parseInt(userIdParam);
            User user = ctx.bodyAsClass(User.class);

            // Build dynamic SQL based on what's being updated
            StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
            List<Object> params = new ArrayList<>();

            if (user.getUsername() != null) {
                sqlBuilder.append("username = ?, ");
                params.add(user.getUsername().trim());
            }
            if (user.getFullName() != null) {
                sqlBuilder.append("full_name = ?, ");
                params.add(user.getFullName().trim());
            }
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                sqlBuilder.append("password_hash = ?, ");
                params.add(hashPassword(user.getPassword()));
            }
            if (user.getRole() != null) {
                sqlBuilder.append("role = ?, ");
                params.add(user.getRole());
            }

            // Remove trailing comma and space
            if (sqlBuilder.charAt(sqlBuilder.length() - 2) == ',') {
                sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
            }

            sqlBuilder.append(" WHERE user_id = ?");
            params.add(userId);

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    logActivity(session.userId, "USER_UPDATE", "Updated user ID: " + userId);
                    ctx.json(createSuccessResponse("User updated successfully"));
                } else {
                    ctx.json(createErrorResponse("User not found"));
                }
            }
        } catch (Exception e) {
            logActivity(session.userId, "USER_UPDATE_ERROR", "User update error: " + e.getMessage());
            ctx.json(createErrorResponse("User update error: " + e.getMessage()));
        }
    }
    // In RESTServer.java - add this method to the document endpoints section
    private static void updateDocument(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            String docIdParam = ctx.pathParam("id");
            int docId = Integer.parseInt(docIdParam);

            // Parse the updated document from request body
            Document updatedDoc = ctx.bodyAsClass(Document.class);

            // First, get the existing document to check ownership
            String selectSql = "SELECT user_id FROM documents WHERE doc_id = ?";
            int documentOwnerId = 0;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

                selectStmt.setInt(1, docId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    documentOwnerId = rs.getInt("user_id");

                    // Check if user owns the document or is admin
                    if (documentOwnerId != session.userId && !"admin".equals(session.role)) {
                        ctx.json(createErrorResponse("You can only edit your own documents"));
                        return;
                    }
                } else {
                    ctx.json(createErrorResponse("Document not found"));
                    return;
                }
            }

            // Update the document in database
            String updateSql = "UPDATE documents SET title = ?, uploaded_by = ? WHERE doc_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                updateStmt.setString(1, updatedDoc.getTitle());
                updateStmt.setString(2, session.fullName != null ? session.fullName : session.username);
                updateStmt.setInt(3, docId);

                int affectedRows = updateStmt.executeUpdate();
                if (affectedRows > 0) {
                    logActivity(session.userId, "EDIT", "Updated document: " + updatedDoc.getTitle());
                    ctx.json(createSuccessResponse("Document updated successfully"));
                } else {
                    ctx.json(createErrorResponse("Document not found"));
                }
            }

        } catch (Exception e) {
            logActivity(session.userId, "EDIT_ERROR", "Edit error: " + e.getMessage());
            ctx.json(createErrorResponse("Update failed: " + e.getMessage()));
        }
    }

    private static void deleteUser(Context ctx) {
        UserSession session = requireAdmin(ctx);
        if (session == null) return;

        try {
            String userIdParam = ctx.pathParam("id");
            int userId = Integer.parseInt(userIdParam);

            // Prevent self-deletion
            if (userId == session.userId) {
                ctx.json(createErrorResponse("Cannot delete your own account"));
                return;
            }

            String sql = "DELETE FROM users WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    logActivity(session.userId, "USER_DELETE", "Deleted user ID: " + userId);
                    ctx.json(createSuccessResponse("User deleted successfully"));
                } else {
                    ctx.json(createErrorResponse("User not found"));
                }
            }
        } catch (Exception e) {
            logActivity(session.userId, "USER_DELETE_ERROR", "User delete error: " + e.getMessage());
            ctx.json(createErrorResponse("User delete error: " + e.getMessage()));
        }
    }



    // Add these message endpoint implementations
// In RESTServer.java - update message endpoints
    private static void getConversations(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            List<com.example.offlinedocumenthubserver.dto.Message> conversations = new ArrayList<>();

            // Get distinct conversations (last message with each user)
            String sql = "SELECT m1.*, " +
                    "sender.username as sender_name, receiver.username as receiver_name " +
                    "FROM messages m1 " +
                    "INNER JOIN users sender ON m1.sender_id = sender.user_id " +
                    "INNER JOIN users receiver ON m1.receiver_id = receiver.user_id " +
                    "WHERE m1.sent_date = ( " +
                    "    SELECT MAX(m2.sent_date) " +
                    "    FROM messages m2 " +
                    "    WHERE (m2.sender_id = m1.sender_id AND m2.receiver_id = m1.receiver_id) " +
                    "       OR (m2.sender_id = m1.receiver_id AND m2.receiver_id = m1.sender_id) " +
                    ") " +
                    "AND (m1.sender_id = ? OR m1.receiver_id = ?) " +
                    "ORDER BY m1.sent_date DESC";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, session.userId);
                stmt.setInt(2, session.userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    com.example.offlinedocumenthubserver.dto.Message message = new com.example.offlinedocumenthubserver.dto.Message(
                            rs.getInt("message_id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("message_text"),
                            rs.getTimestamp("sent_date").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    );
                    message.setSenderName(rs.getString("sender_name"));
                    message.setReceiverName(rs.getString("receiver_name"));
                    conversations.add(message);
                }
            }

            // Return the array directly, not wrapped in a response object
            ctx.json(objectMapper.writeValueAsString(conversations));

        } catch (Exception e) {
            System.err.println("Error in getConversations: " + e.getMessage());
            e.printStackTrace();
            // Return empty array on error
            ctx.json("[]");
        }
    }

    private static void getMessages(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            String otherUserIdParam = ctx.pathParam("userId");
            int otherUserId = Integer.parseInt(otherUserIdParam);

            List<com.example.offlinedocumenthubserver.dto.Message> messages = new ArrayList<>();
            String sql = "SELECT m.*, " +
                    "sender.username as sender_name, receiver.username as receiver_name " +
                    "FROM messages m " +
                    "INNER JOIN users sender ON m.sender_id = sender.user_id " +
                    "INNER JOIN users receiver ON m.receiver_id = receiver.user_id " +
                    "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                    "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                    "ORDER BY m.sent_date ASC";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, session.userId);
                stmt.setInt(2, otherUserId);
                stmt.setInt(3, otherUserId);
                stmt.setInt(4, session.userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    com.example.offlinedocumenthubserver.dto.Message message = new com.example.offlinedocumenthubserver.dto.Message(
                            rs.getInt("message_id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("message_text"),
                            rs.getTimestamp("sent_date").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    );
                    message.setSenderName(rs.getString("sender_name"));
                    message.setReceiverName(rs.getString("receiver_name"));
                    messages.add(message);
                }
            }

            // Return the array directly, not wrapped in a response object
            ctx.json(objectMapper.writeValueAsString(messages));

        } catch (Exception e) {
            System.err.println("Error in getMessages: " + e.getMessage());
            e.printStackTrace();
            // Return empty array on error
            ctx.json("[]");
        }
    }

    private static void sendMessage(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            int receiverId = ((Number) body.get("receiverId")).intValue();
            String messageText = (String) body.get("messageText");

            if (messageText == null || messageText.trim().isEmpty()) {
                ctx.json(createErrorResponse("Message text is required"));
                return;
            }

            String sql = "INSERT INTO messages (sender_id, receiver_id, message_text, sent_date, is_read) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, session.userId);
                stmt.setInt(2, receiverId);
                stmt.setString(3, messageText.trim());
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setBoolean(5, false);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    logActivity(session.userId, "SEND_MESSAGE", "Sent message to user ID: " + receiverId);
                    ctx.json(createSuccessResponse("Message sent successfully"));
                } else {
                    ctx.json(createErrorResponse("Failed to send message"));
                }
            }
        } catch (Exception e) {
            logActivity(session.userId, "SEND_MESSAGE_ERROR", "Message send error: " + e.getMessage());
            ctx.json(createErrorResponse("Failed to send message: " + e.getMessage()));
        }
    }

    private static void markMessagesAsRead(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            String otherUserIdParam = ctx.pathParam("userId");
            int otherUserId = Integer.parseInt(otherUserIdParam);

            String sql = "UPDATE messages SET is_read = true " +
                    "WHERE sender_id = ? AND receiver_id = ? AND is_read = false";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, otherUserId);
                stmt.setInt(2, session.userId);
                stmt.executeUpdate();
            }

            ctx.json(createSuccessResponse("Messages marked as read"));
        } catch (Exception e) {
            logActivity(session.userId, "MARK_READ_ERROR", "Mark read error: " + e.getMessage());
            ctx.json(createErrorResponse("Failed to mark messages as read: " + e.getMessage()));
        }
    }

    // ============ ACTIVITY LOGS ENDPOINT ============
    // In getActivityLogs method - FIXED
    private static void getActivityLogs(Context ctx) {
        UserSession session = requireAdmin(ctx);
        if (session == null) return;

        try {
            List<ActivityLog> activityLogs = new ArrayList<>();
            // FIXED: Use COALESCE to handle null usernames better
            String sql = "SELECT al.log_id, al.user_id, " +
                    "COALESCE(u.username, 'System') as username, " +
                    "COALESCE(u.full_name, 'System') as full_name, " +
                    "al.action_type, al.action_details, al.timestamp " +
                    "FROM activity_logs al " +
                    "LEFT JOIN users u ON al.user_id = u.user_id " +
                    "ORDER BY al.timestamp DESC";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");
                    String displayName = fullName != null && !fullName.trim().isEmpty() && !fullName.equals("System")
                            ? fullName + " (" + username + ")"
                            : username;

                    ActivityLog log = new ActivityLog(
                            rs.getInt("log_id"),
                            rs.getInt("user_id"),
                            displayName,
                            rs.getString("action_type"),
                            rs.getString("action_details"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    activityLogs.add(log);
                }
            }

            ctx.json(objectMapper.writeValueAsString(activityLogs));
        } catch (Exception e) {
            logActivity(session.userId, "LOGS_LOAD_ERROR", "Failed to load activity logs: " + e.getMessage());
            ctx.json(createErrorResponse("Failed to load activity logs: " + e.getMessage()));
        }
    }

    // ============ HELPER METHODS ============
    private static String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    // In RESTServer.java - fix the logActivity method calls
    private static void logActivity(int userId, String actionType, String details) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, action_details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Use -1 for system actions, otherwise use actual user ID
            if (userId <= 0) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, actionType);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    private static Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    private static Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private static Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    private static String makeFileNameSafe(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return (lastDotIndex > 0) ? fileName.substring(lastDotIndex) : "";
    }

    public static void stopServer() {
        if (app != null) {
            app.stop();
        }
    }
    // In RESTServer.java - add these backup endpoints
    private static void triggerBackup(Context ctx) {
        UserSession session = requireAdmin(ctx);
        if (session == null) return;

        try {
            System.out.println("Backup triggered by user: " + session.username);

            GoogleDriveService driveService = new GoogleDriveService();
            Map<String, Object> backupResult = driveService.performBackup();

            if (Boolean.TRUE.equals(backupResult.get("success"))) {
                System.out.println("Backup completed successfully: " + backupResult.get("folderName"));
                logActivity(session.userId, "BACKUP", "Cloud backup completed: " + backupResult.get("folderName"));
            } else {
                System.err.println("Backup failed: " + backupResult.get("message"));
                logActivity(session.userId, "BACKUP_ERROR", "Cloud backup failed: " + backupResult.get("message"));
            }

            ctx.json(backupResult);

        } catch (Exception e) {
            System.err.println("Backup endpoint error: " + e.getMessage());
            e.printStackTrace();

            logActivity(session.userId, "BACKUP_ERROR", "Backup error: " + e.getMessage());

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Backup failed: " + e.getMessage());
            errorResult.put("progress", 0);
            ctx.json(errorResult);
        }
    }

    private static void getBackupProgress(Context ctx) {
        UserSession session = requireAuth(ctx);
        if (session == null) return;

        try {
            // In a real implementation, you'd track progress in a session or database
            // For now, we'll return a simulated progress
            Map<String, Object> progress = new HashMap<>();
            progress.put("progress", 0);
            progress.put("status", "Backup not started");
            progress.put("active", false);

            ctx.json(createSuccessResponse("Backup progress", progress));

        } catch (Exception e) {
            ctx.json(createErrorResponse("Failed to get backup progress: " + e.getMessage()));
        }
    }

    private static void listBackups(Context ctx) {
        UserSession session = requireAdmin(ctx);
        if (session == null) return;

        try {
            GoogleDriveService driveService = new GoogleDriveService();
            List<Map<String, String>> backups = driveService.listBackups();

            ctx.json(createSuccessResponse("Backup list retrieved", backups));

        } catch (Exception e) {
            logActivity(session.userId, "BACKUP_LIST_ERROR", "Failed to list backups: " + e.getMessage());
            ctx.json(createErrorResponse("Failed to list backups: " + e.getMessage()));
        }
    }
}