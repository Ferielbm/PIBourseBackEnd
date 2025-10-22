package tn.esprit.piboursebackend.Player.Security.dto;

/**
 * DTO pour les messages de réponse simples
 */
public class MessageResponse {
    private String message;

    // Constructeurs
    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    // Getters et Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

