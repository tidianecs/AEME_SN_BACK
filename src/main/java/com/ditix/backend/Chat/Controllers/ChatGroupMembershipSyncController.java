package com.ditix.backend.Chat.Controllers;

import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/chat/groups")
public class ChatGroupMembershipSyncController {

    private final ChatGroupMembershipSyncService syncService;

    public ChatGroupMembershipSyncController(ChatGroupMembershipSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/{id:\\d+}/sync-members")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ChatGroupSyncReport> syncGroupMembers(@PathVariable Long id) {
        ChatGroupSyncReport report = syncService.syncGroup(id);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/sync-members")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<ChatGroupSyncReport>> syncAllGroupMembers() {
        List<ChatGroupSyncReport> reports = syncService.syncAllActiveManagedGroups();
        return ResponseEntity.ok(reports);
    }
}
