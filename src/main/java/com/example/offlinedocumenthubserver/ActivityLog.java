package com.example.offlinedocumenthubserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLog {
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