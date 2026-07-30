package com.ditix.backend.Chat.Controllers;

import com.ditix.backend.Chat.DTO.ManagedChatGroupDTO;
import com.ditix.backend.Chat.DTO.ManagedChatGroupRequest;
import com.ditix.backend.Chat.Services.ManagedChatGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/chat/groups")
public class ManagedChatGroupRESTController {

    private final ManagedChatGroupService managedChatGroupService;

    public ManagedChatGroupRESTController(ManagedChatGroupService managedChatGroupService) {
        this.managedChatGroupService = managedChatGroupService;
    }

    private String extractUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    @PostMapping("/global")
    public ResponseEntity<ManagedChatGroupDTO> createGlobalGroup(
            @RequestBody ManagedChatGroupRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ManagedChatGroupDTO(
                managedChatGroupService.createOrGetGlobalGroup(request.getName(), extractUserId(jwt))));
    }

    @PostMapping("/cohort")
    public ResponseEntity<ManagedChatGroupDTO> createCohortGroup(
            @RequestBody ManagedChatGroupRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ManagedChatGroupDTO(
                managedChatGroupService.createOrGetCohortGroup(request.getReferenceId(), request.getName(), extractUserId(jwt))));
    }

    @PostMapping("/structure")
    public ResponseEntity<ManagedChatGroupDTO> createStructureGroup(
            @RequestBody ManagedChatGroupRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ManagedChatGroupDTO(
                managedChatGroupService.createOrGetStructureGroup(request.getReferenceId(), request.getName(), extractUserId(jwt))));
    }

    @GetMapping
    public ResponseEntity<List<ManagedChatGroupDTO>> listManagedGroups() {
        return ResponseEntity.ok(managedChatGroupService.listManagedGroups()
                .stream()
                .map(ManagedChatGroupDTO::new)
                .collect(Collectors.toList()));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ManagedChatGroupDTO> archiveGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new ManagedChatGroupDTO(
                managedChatGroupService.archiveGroup(id, extractUserId(jwt))));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ManagedChatGroupDTO> reactivateGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new ManagedChatGroupDTO(
                managedChatGroupService.reactivateGroup(id, extractUserId(jwt))));
    }
}
