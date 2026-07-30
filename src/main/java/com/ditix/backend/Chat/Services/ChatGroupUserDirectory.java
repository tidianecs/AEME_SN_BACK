package com.ditix.backend.Chat.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ditix.backend.Chat.DTO.ChatGroupUser;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatGroupUserDirectory {

    private static final Logger log = LoggerFactory.getLogger(ChatGroupUserDirectory.class);

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    public ChatGroupUserDirectory(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public List<ChatGroupUser> fetchAllEnabledUsers() {
        Map<String, ChatGroupUser> uniqueUsers = new HashMap<>();
        int pageSize = 100;
        int first = 0;

        try {
            while (true) {
                List<UserRepresentation> page = keycloak.realm(realm).users().list(first, pageSize);
                if (page == null) {
                    break;
                }

                for (UserRepresentation user : page) {
                    if (user.getId() == null || user.getId().trim().isEmpty()) {
                        continue;
                    }
                    if (user.isEnabled() == null || !user.isEnabled()) {
                        continue;
                    }

                    String userId = user.getId().trim();
                    String cohorte = extractNormalizedAttribute(user, "cohorte");
                    String structureId = extractNormalizedAttribute(user, "structureId");

                    uniqueUsers.putIfAbsent(userId, new ChatGroupUser(userId, cohorte, structureId));
                }

                if (page.size() < pageSize) {
                    break;
                }
                first += pageSize;
            }
        } catch (Exception e) {
            log.error("Unable to retrieve Keycloak users for chat group synchronization", e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User directory is temporarily unavailable");
        }

        return new ArrayList<>(uniqueUsers.values());
    }

    private String extractNormalizedAttribute(UserRepresentation user, String attributeName) {
        if (user.getAttributes() == null || !user.getAttributes().containsKey(attributeName)) {
            return null;
        }
        List<String> values = user.getAttributes().get(attributeName);
        if (values == null) {
            return null;
        }
        for (String val : values) {
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }
}
