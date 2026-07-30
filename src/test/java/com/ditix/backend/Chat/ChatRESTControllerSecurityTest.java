package com.ditix.backend.Chat;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Chat.Controllers.ChatRESTController;
import com.ditix.backend.Chat.Services.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatRESTController.class)
public class ChatRESTControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private AuthService authService;

    @Test
    void getCounterpart_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart"))
                .andExpect(status().isUnauthorized());

        verify(chatService, never()).getCounterpartUserId(anyLong(), anyString());
        verify(authService, never()).getUserById(anyString());
    }

    @Test
    void getCounterpart_participantShouldReturnMinimalProfile() throws Exception {
        when(chatService.getCounterpartUserId(1L, "user-a")).thenReturn("user-b");
        when(authService.getUserById("user-b")).thenReturn(Map.of(
                "id", "user-b",
                "fullName", "Nom User B",
                "email", "userb@test.com",
                "contact1", "123456",
                "dateNaissance", "01/01/1990"
        ));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-b"))
                .andExpect(jsonPath("$.fullName").value("Nom User B"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.contact1").doesNotExist())
                .andExpect(jsonPath("$.dateNaissance").doesNotExist());

        verify(chatService, times(1)).getCounterpartUserId(1L, "user-a");
        verify(authService, times(1)).getUserById("user-b");
    }

    @Test
    void getCounterpart_nonParticipant_shouldReturn403() throws Exception {
        when(chatService.getCounterpartUserId(1L, "user-c"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-c"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Test
    void getCounterpart_missingConversation_shouldReturn403() throws Exception {
        when(chatService.getCounterpartUserId(1L, "user-a"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Test
    void getCounterpart_adminNonParticipant_shouldReturn403() throws Exception {
        when(chatService.getCounterpartUserId(1L, "admin-a"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Test
    void getCounterpart_missingKeycloakUser_shouldReturn404() throws Exception {
        when(chatService.getCounterpartUserId(1L, "user-a")).thenReturn("user-b");
        when(authService.getUserById("user-b"))
                .thenThrow(new jakarta.ws.rs.NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCounterpart_blankSubject_shouldReturn403() throws Exception {
        // Jwt subject with blank values might be rejected by the builder or handled by our logic.
        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("   "))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isUnauthorized());

        verify(chatService, never()).getCounterpartUserId(anyLong(), anyString());
        verify(authService, never()).getUserById(anyString());
    }
    @Test
    void getMessages_missingConversation_shouldReturn404() throws Exception {
        when(chatService.getMessages(1L, "user-a"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/messages")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMessages_nonParticipant_shouldReturn403() throws Exception {
        when(chatService.getMessages(1L, "user-a"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/messages")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteConversation_nonParticipant_shouldReturn403() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"))
                .when(chatService).deleteConversation(1L, "user-a");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/chat/conversations/1")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }
}
