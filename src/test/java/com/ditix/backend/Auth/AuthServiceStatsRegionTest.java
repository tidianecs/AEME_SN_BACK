package com.ditix.backend.Auth;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Core.EmailService;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceStatsRegionTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private EmailService emailService;

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "realm", "test-realm");
    }

    private Structure createStructure(String region) {
        Structure s = new Structure();
        s.setRegion(region);
        return s;
    }

    private UserRepresentation createUser(String region) {
        UserRepresentation u = new UserRepresentation();
        Map<String, List<String>> attributes = new HashMap<>();
        if (region != null) {
            attributes.put("region", Arrays.asList(region));
        }
        u.setAttributes(attributes);
        return u;
    }

    @Test
    void testGetStatsByRegion_NormalizesAndMerges() {
        // Mock structures
        List<Structure> structures = Arrays.asList(
                createStructure("Dakar"),
                createStructure("DAKAR"),
                createStructure(" dakar "),
                createStructure("Saint-Louis"),
                createStructure(null),
                createStructure("   "),
                createStructure("THIÈS") // Only in structures
        );
        when(structureRepository.findAll()).thenReturn(structures);

        // Mock users
        List<UserRepresentation> users = Arrays.asList(
                createUser("Dakar"),
                createUser(" dAKAR"),
                createUser("SAINT-LOUIS"),
                createUser(null),
                createUser(""),
                createUser("Ziguinchor") // Only in users
        );
        when(keycloak.realm("test-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list(anyInt(), anyInt())).thenReturn(users);

        List<Map<String, Object>> stats = authService.getStatsByRegion();

        assertEquals(4, stats.size()); // Dakar, Saint-louis, Thiès, Ziguinchor

        Map<String, Map<String, Object>> statsMap = new HashMap<>();
        for (Map<String, Object> stat : stats) {
            statsMap.put((String) stat.get("region"), stat);
        }

        assertTrue(statsMap.containsKey("Dakar"));
        assertEquals(3L, statsMap.get("Dakar").get("structures"));
        assertEquals(2L, statsMap.get("Dakar").get("gestionnaires"));

        assertTrue(statsMap.containsKey("Saint-Louis"));
        assertEquals(1L, statsMap.get("Saint-Louis").get("structures"));
        assertEquals(1L, statsMap.get("Saint-Louis").get("gestionnaires"));

        assertTrue(statsMap.containsKey("Thiès"));
        assertEquals(1L, statsMap.get("Thiès").get("structures"));
        assertEquals(0L, statsMap.get("Thiès").get("gestionnaires"));

        assertTrue(statsMap.containsKey("Ziguinchor"));
        assertEquals(0L, statsMap.get("Ziguinchor").get("structures"));
        assertEquals(1L, statsMap.get("Ziguinchor").get("gestionnaires"));
    }
}
