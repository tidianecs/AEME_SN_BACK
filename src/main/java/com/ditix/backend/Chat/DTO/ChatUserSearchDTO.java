package com.ditix.backend.Chat.DTO;

public class ChatUserSearchDTO {

    private String userId;
    private String prenom;
    private String nom;
    private String role;
    private String structureName;
    private String ministereName;

    public ChatUserSearchDTO() {}

    public ChatUserSearchDTO(String userId, String prenom, String nom, String role, String structureName, String ministereName) {
        this.userId = userId;
        this.prenom = prenom;
        this.nom = nom;
        this.role = role;
        this.structureName = structureName;
        this.ministereName = ministereName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStructureName() {
        return structureName;
    }

    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }

    public String getMinistereName() {
        return ministereName;
    }

    public void setMinistereName(String ministereName) {
        this.ministereName = ministereName;
    }
}
