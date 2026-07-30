package com.ditix.backend.Chat.DTO;

public class ManagedChatGroupRequest {
    private String name;
    private String referenceId;

    public ManagedChatGroupRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
