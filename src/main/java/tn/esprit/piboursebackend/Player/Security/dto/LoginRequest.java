package tn.esprit.piboursebackend.Player.Security.dto;

/**
 * DTO pour la requête de connexion
 */
public class LoginRequest {
    private String username;
    private String email;
    private String password;

    // Constructeurs
    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginRequest(String email, String password, boolean useEmail) {
        this.email = email;
        this.password = password;
    }

    // Getters et Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

