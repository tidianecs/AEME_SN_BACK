package com.ditix.backend.Chat.DTO;

import com.ditix.backend.Chat.Models.ConversationType;
import java.time.LocalDateTime;

public class ChatGroupSyncReport {

    private Long conversationId;
    private ConversationType type;
    private String referenceId;
    private int eligibleUsers;
    private int activeMembersBefore;
    private int addedMembers;
    private int deactivatedMembers;
    private int unchangedMembers;
    private int activeMembersAfter;
    private LocalDateTime synchronizedAt;

    public ChatGroupSyncReport() {
    }

    public ChatGroupSyncReport(Long conversationId, ConversationType type, String referenceId,
                               int eligibleUsers, int activeMembersBefore, int addedMembers,
                               int deactivatedMembers, int unchangedMembers, int activeMembersAfter,
                               LocalDateTime synchronizedAt) {
        this.conversationId = conversationId;
        this.type = type;
        this.referenceId = referenceId;
        this.eligibleUsers = eligibleUsers;
        this.activeMembersBefore = activeMembersBefore;
        this.addedMembers = addedMembers;
        this.deactivatedMembers = deactivatedMembers;
        this.unchangedMembers = unchangedMembers;
        this.activeMembersAfter = activeMembersAfter;
        this.synchronizedAt = synchronizedAt;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public int getEligibleUsers() {
        return eligibleUsers;
    }

    public void setEligibleUsers(int eligibleUsers) {
        this.eligibleUsers = eligibleUsers;
    }

    public int getActiveMembersBefore() {
        return activeMembersBefore;
    }

    public void setActiveMembersBefore(int activeMembersBefore) {
        this.activeMembersBefore = activeMembersBefore;
    }

    public int getAddedMembers() {
        return addedMembers;
    }

    public void setAddedMembers(int addedMembers) {
        this.addedMembers = addedMembers;
    }

    public int getDeactivatedMembers() {
        return deactivatedMembers;
    }

    public void setDeactivatedMembers(int deactivatedMembers) {
        this.deactivatedMembers = deactivatedMembers;
    }

    public int getUnchangedMembers() {
        return unchangedMembers;
    }

    public void setUnchangedMembers(int unchangedMembers) {
        this.unchangedMembers = unchangedMembers;
    }

    public int getActiveMembersAfter() {
        return activeMembersAfter;
    }

    public void setActiveMembersAfter(int activeMembersAfter) {
        this.activeMembersAfter = activeMembersAfter;
    }

    public LocalDateTime getSynchronizedAt() {
        return synchronizedAt;
    }

    public void setSynchronizedAt(LocalDateTime synchronizedAt) {
        this.synchronizedAt = synchronizedAt;
    }
}
