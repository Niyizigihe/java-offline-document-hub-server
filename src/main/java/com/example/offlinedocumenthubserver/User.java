package com.example.offlinedocumenthubserver;

public class User {
    private int id;
    private String username;
    private String fullname;
    private String password;
    private String role;

    // ADD DEFAULT CONSTRUCTOR
    public User() {}

    public User(int id, String username, String fullname, String password, String role) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullname; }
    public void setFullName(String fullname) { this.fullname = fullname; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}