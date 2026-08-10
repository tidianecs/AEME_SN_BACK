package com.ditix.backend.Chat;

import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


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
    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        try {
            org.mockito.Mockito.lenient().when(profilUtilisateurCourantService.obtenirProfilCourant(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
                Object arg = invocation.getArgument(0);
                if (arg instanceof JwtAuthenticationToken) {
                    JwtAuthenticationToken auth = (JwtAuthenticationToken) arg;
                    String sub = auth.getToken().getSubject();
                    if (sub == null || sub.trim().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }
                    ProfilUtilisateur profil = new ProfilUtilisateur();

                    // Use a standard UUID if it's not a UUID format to prevent parsing errors elsewhere
                    try {
                        profil.setKeycloakId(UUID.fromString(sub));
                    } catch (Exception e) {
                        // fallback for tests using dummy strings like "11111111-1111-1111-1111-111111111111"
                        if ("00000000-0000-0000-0000-000000000001".equals(sub)) {
                             profil.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                        } else {
                             profil.setKeycloakId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
                        }
                    }

                    profil.setActif(true);

                    if (sub.contains("admin") || sub.contains("Admin")) {
                        profil.setRole(RoleUtilisateur.ADMIN);
                    } else if (sub.contains("dage")) {
                        profil.setRole(RoleUtilisateur.DAGE);
                    } else if (sub.contains("gestionnaire")) {
                        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
                    } else {
                        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
                    }

                    if (sub.contains("inactive")) {
                        profil.setActif(false);
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }

                    if (sub.contains("missing_profile")) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }

                    return profil;
                }
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
            });
        } catch (Exception e) {}
    }

    @MockBean
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private AuthService authService;

    @Tes
    void getCounterpart_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart"))
                .andExpect(status().isUnauthorized());

        verify(chatService, never()).getCounterpartUserId(anyLong(), anyString());
        verify(authService, never()).getUserById(anyString());
    }

    @Tes
    void getCounterpart_participantShouldReturnMinimalProfile() throws Exception {
        when(chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111")).thenReturn("11111111-1111-1111-1111-111111111111");
        when(authService.getUserById("11111111-1111-1111-1111-111111111111")).thenReturn(Map.of(
                "id", "11111111-1111-1111-1111-111111111111",
                "fullName", "Nom User B",
                "email", "userb@test.com",
                "contact1", "123456",
                "dateNaissance", "01/01/1990"
        ));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.fullName").value("Nom User B"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.contact1").doesNotExist())
                .andExpect(jsonPath("$.dateNaissance").doesNotExist());

        verify(chatService, times(1)).getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111");
        verify(authService, times(1)).getUserById("11111111-1111-1111-1111-111111111111");
    }

    @Tes
    void getCounterpart_nonParticipant_shouldReturn403() throws Exception {
        when(chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Tes
    void getCounterpart_missingConversation_shouldReturn403() throws Exception {
        when(chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Tes
    void getCounterpart_adminNonParticipant_shouldReturn403() throws Exception {
        when(chatService.getCounterpartUserId(1L, "00000000-0000-0000-0000-000000000001"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000001"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Tes
    void getCounterpart_missingKeycloakUser_shouldReturn404() throws Exception {
        when(chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111")).thenReturn("11111111-1111-1111-1111-111111111111");
        when(authService.getUserById("11111111-1111-1111-1111-111111111111"))
                .thenThrow(new jakarta.ws.rs.NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Tes
    void getCounterpart_blankSubject_shouldReturn403() throws Exception {
        // Jwt subject with blank values might be rejected by the builder or handled by our logic.
        mockMvc.perform(get("/api/v1/chat/conversations/1/counterpart")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("   "))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(chatService, never()).getCounterpartUserId(anyLong(), anyString());
        verify(authService, never()).getUserById(anyString());
    }
    @Tes
    void getMessages_missingConversation_shouldReturn404() throws Exception {
        when(chatService.getMessages(1L, "11111111-1111-1111-1111-111111111111"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/messages")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Tes
    void getMessages_nonParticipant_shouldReturn403() throws Exception {
        when(chatService.getMessages(1L, "11111111-1111-1111-1111-111111111111"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        mockMvc.perform(get("/api/v1/chat/conversations/1/messages")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Tes
    void deleteConversation_nonParticipant_shouldReturn403() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"))
                .when(chatService).deleteConversation(1L, "11111111-1111-1111-1111-111111111111");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/chat/conversations/1")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }
}
