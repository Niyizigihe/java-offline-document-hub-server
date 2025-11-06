package com.example.offlinedocumenthubserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static class User {
        private int id;
        private String username;
        private String fullname;
        private String password;
        private String role; // Changed to String

        public User(int id, String username, String fullname, String password, String role) {
            this.id = id;
            this.username = username;
            this.fullname = fullname;
            this.password = password;
            this.role = role;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullname; }
        public void setUsername(String username) { this.username = username; }
        public void setFullName(String fullname) { this.fullname = fullname; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class ActivityLog {
        private int logId;
        private int userId;
        private String userDisplayName;
        private String actionType;
        private String actionDetails;
        private LocalDateTime timestamp;

        public ActivityLog(int logId, int userId, String userDisplayName, String actionType,
                           String actionDetails, LocalDateTime timestamp) {
            this.logId = logId;
            this.userId = userId;
            this.userDisplayName = userDisplayName;
            this.actionType = actionType;
            this.actionDetails = actionDetails;
            this.timestamp = timestamp;
        }

        // Getters - these must match the PropertyValueFactory names exactly
        public int getLogId() { return logId; }
        public int getUserId() { return userId; }
        public String getUserDisplayName() { return userDisplayName; }
        public String getActionType() { return actionType; }
        public String getActionDetails() { return actionDetails; }
        public LocalDateTime getTimestamp() { return timestamp; }

        // Formatted timestamp for display
        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public static class Document {
        private int docId;
        private String title;
        private String filePath;
        private String uploadedBy;
        private LocalDate uploadDate;
        private int userId;
        private String fileSize;

        public Document(int docId, String title, String filePath, String uploadedBy, LocalDate uploadDate) {
            this(docId, title, filePath, uploadedBy, uploadDate, -1);
        }

        public Document(int docId, String title, String filePath, String uploadedBy, LocalDate uploadDate, int userId) {
            this.docId = docId;
            this.title = title;
            this.filePath = filePath;
            this.uploadedBy = uploadedBy;
            this.uploadDate = uploadDate;
            this.userId = userId;
        }

        // Getters and setters
        public int getDocId() { return docId; }
        public void setDocId(int docId) { this.docId = docId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

        public LocalDate getUploadDate() { return uploadDate; }
        public void setUploadDate(LocalDate uploadDate) { this.uploadDate = uploadDate; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getFileSize() { return fileSize; }
        public void setFileSize(String fileSize) { this.fileSize = fileSize; }
        public void setFileSize(long fileSizeBytes) {
            // Convert bytes to human readable format
            if (fileSizeBytes < 1024) {
                this.fileSize = fileSizeBytes + " B";
            } else if (fileSizeBytes < 1024 * 1024) {
                this.fileSize = String.format("%.1f KB", fileSizeBytes / 1024.0);
            } else {
                this.fileSize = String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
            }
        }
    }
}
