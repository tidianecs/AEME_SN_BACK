package com.ditix.backend.Structure;

import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.Structure.DTO.StructureDTO;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import com.ditix.backend.Structure.Services.StructureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class StructureLazyLoadingTest {

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private StructureService structureService;

    @Test
    public void testGetAllStructuresLazyLoadingFix() {
        // 1. Create a legacy structure (ministereV2 is null)
        Structure legacyStructure = new Structure();
        legacyStructure.setName("Legacy Structure " + UUID.randomUUID().toString());
        structureRepository.save(legacyStructure);

        // 2. Create a V2 Ministere
        Ministere ministere = new Ministere();
        ministere.setNom("Ministère V2 " + UUID.randomUUID().toString());
        ministere.setCode("MIN-V2-" + UUID.randomUUID().toString());
        ministere = ministereRepository.save(ministere);

        // 3. Create a Structure linked to that Ministere
        Structure v2Structure = new Structure();
        String v2Name = "V2 Structure " + UUID.randomUUID().toString();
        v2Structure.setName(v2Name);
        v2Structure.setMinistereV2(ministere);
        structureRepository.save(v2Structure);

        // 4. Execute StructureService.getAllStructures() to verify no LazyInitializationException occurs
        List<StructureDTO> dtoList = structureService.getAllStructures();
        assertNotNull(dtoList);

        // 5. Verify the returned DTOs
        boolean legacyFound = false;
        boolean v2Found = false;

        for (StructureDTO dto : dtoList) {
            if (dto.getName().equals(legacyStructure.getName())) {
                legacyFound = true;
                assertNull(dto.getMinistereId());
                assertNull(dto.getMinistereNom());
            }

            if (dto.getName().equals(v2Name)) {
                v2Found = true;
                assertEquals(v2Structure.getId(), dto.getId());
                assertEquals(ministere.getId(), dto.getMinistereId());
                assertEquals(ministere.getNom(), dto.getMinistereNom());
            }
        }

        assertTrue(legacyFound, "Legacy structure should be mapped successfully");
        assertTrue(v2Found, "V2 structure should be mapped successfully");
    }
}
