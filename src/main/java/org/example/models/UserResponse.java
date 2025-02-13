package org.example.models;

public class UserResponse {
    public UserResponse() {
    }

    public UserResponse(User user) {
        username = user.getUsername();
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
