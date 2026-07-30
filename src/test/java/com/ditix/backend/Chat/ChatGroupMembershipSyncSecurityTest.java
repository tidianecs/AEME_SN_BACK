package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Controllers.ChatGroupMembershipSyncController;
import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatGroupMembershipSyncController.class)
@Import(com.ditix.backend.Core.SecurityConfig.class)
public class ChatGroupMembershipSyncSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatGroupMembershipSyncService syncService;

    @Test
    void testSyncGroup_NoToken_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/chat/groups/1/sync-members")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void testSyncGroup_StandardUser_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/chat/groups/1/sync-members")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "admin")
    void testSyncGroup_AdminUser_Returns200() throws Exception {
        when(syncService.syncGroup(anyLong())).thenReturn(new ChatGroupSyncReport());

        mockMvc.perform(post("/api/v1/admin/chat/groups/1/sync-members")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "admin")
    void testSyncAllGroups_AdminUser_Returns200() throws Exception {
        when(syncService.syncAllActiveManagedGroups()).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/admin/chat/groups/sync-members")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }
}
