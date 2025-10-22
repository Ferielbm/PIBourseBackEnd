package tn.esprit.piboursebackend.Player.Security.dto;

import tn.esprit.piboursebackend.Player.Entities.Role;

/**
 * DTO pour la requête d'inscription
 */
public class SignupRequest {
    private String username;
    private String email;
    private String password;
    private Role role;

    // Constructeurs
    public SignupRequest() {
    }

    public SignupRequest(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

