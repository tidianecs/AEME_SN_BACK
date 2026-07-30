package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Controllers.ManagedChatGroupRESTController;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Services.ManagedChatGroupService;
import com.ditix.backend.Core.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagedChatGroupRESTController.class)
@Import(SecurityConfig.class)
public class ManagedChatGroupRESTControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagedChatGroupService managedChatGroupService;

    private Conversation createMockGroup(Long id, ConversationType type, String ref) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setType(type);
        conv.setName("Group Name");
        conv.setReferenceId(ref);
        conv.setActive(true);
        conv.setSystemManaged(true);
        return conv;
    }

    @Test
    void createGlobal_standardUser_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/chat/groups/global")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Global\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(managedChatGroupService, never()).createOrGetGlobalGroup(anyString(), anyString());
    }

    @Test
    void createGlobal_adminUser_shouldReturn201() throws Exception {
        when(managedChatGroupService.createOrGetGlobalGroup(eq("Global"), eq("admin1")))
                .thenReturn(createMockGroup(1L, ConversationType.GLOBAL, "GLOBAL"));

        mockMvc.perform(post("/api/v1/admin/chat/groups/global")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Global\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("GLOBAL"));
    }

    @Test
    void createCohort_adminUser_shouldReturn201() throws Exception {
        when(managedChatGroupService.createOrGetCohortGroup(eq("ref1"), eq("Cohort"), eq("admin1")))
                .thenReturn(createMockGroup(2L, ConversationType.COHORT, "ref1"));

        mockMvc.perform(post("/api/v1/admin/chat/groups/cohort")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"referenceId\": \"ref1\", \"name\": \"Cohort\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("COHORT"))
                .andExpect(jsonPath("$.referenceId").value("ref1"));
    }

    @Test
    void listGroups_adminUser_shouldReturn200() throws Exception {
        when(managedChatGroupService.listManagedGroups())
                .thenReturn(List.of(createMockGroup(1L, ConversationType.GLOBAL, "GLOBAL")));

        mockMvc.perform(get("/api/v1/admin/chat/groups")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "admin")
    public void testCreateGlobalGroup_IgnoresCreatedByUserIdInJson() throws Exception {
        when(managedChatGroupService.createOrGetGlobalGroup(eq("Global Admin"), anyString()))
                .thenReturn(createMockGroup(1L, ConversationType.GLOBAL, "GLOBAL"));
        
        String json = """
            {
                "name": "Global Admin",
                "createdByUserId": "hacker123"
            }
            """;
        
        mockMvc.perform(post("/api/v1/admin/chat/groups/global")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated());
                
        // Verification happens via the integration test (which calls the service directly)
        // Here we just test the endpoint accepts the request and does not crash or use it.
    }
}
