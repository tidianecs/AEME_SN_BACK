package com.ditix.backend.Structure;

import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class StructureIntegrationTest {

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private MinistereRepository ministereRepository;

    @Test
    @Transactional
    public void testStructureV2Columns() {
        Ministere ministere = new Ministere();
        ministere.setCode("MIN_TEST");
        ministere.setNom("Ministère de Test");
        ministere = ministereRepository.save(ministere);

        Structure structure = new Structure();
        structure.setName("Structure Test");
        structure.setLatitude("14.0");
        structure.setLongitude("-17.0");
        structure.setMinistere("Old Ministere String");
        
        // V2 Fields
        structure.setMinistereV2(ministere);
        structure.setLatitudeV2(14.716677);
        structure.setLongitudeV2(-17.467686);
        
        Structure saved = structureRepository.save(structure);
        
        assertNotNull(saved.getId());
        assertEquals("MIN_TEST", saved.getMinistereV2().getCode());
        assertEquals(14.716677, saved.getLatitudeV2());
        assertEquals(-17.467686, saved.getLongitudeV2());
        assertEquals("Old Ministere String", saved.getMinistere());
    }

    @Test
    @Transactional
    public void testValidLatitudeV2_Boundaries_ShouldBeAccepted() {
        // NULL is tested implicitly (default for double is null in Object, but we can test explicitly)
        Structure structure1 = new Structure();
        structure1.setName("Null Lat");
        structure1.setLatitudeV2(null);
        structureRepository.saveAndFlush(structure1);
        
        Structure structure2 = new Structure();
        structure2.setName("Lat -90");
        structure2.setLatitudeV2(-90.0);
        structureRepository.saveAndFlush(structure2);
        
        Structure structure3 = new Structure();
        structure3.setName("Lat 90");
        structure3.setLatitudeV2(90.0);
        structureRepository.saveAndFlush(structure3);
        
        Structure structure4 = new Structure();
        structure4.setName("Lat Precision");
        structure4.setLatitudeV2(14.716677);
        structureRepository.saveAndFlush(structure4);
    }

    @Test
    @Transactional
    public void testInvalidLatitudeV2_Boundaries_ShouldThrowException() {
        Structure structure1 = new Structure();
        structure1.setName("Lat -90.000001");
        structure1.setLatitudeV2(-90.000001);
        assertThrows(Exception.class, () -> {
            structureRepository.saveAndFlush(structure1);
        });

        Structure structure2 = new Structure();
        structure2.setName("Lat 90.000001");
        structure2.setLatitudeV2(90.000001);
        assertThrows(Exception.class, () -> {
            structureRepository.saveAndFlush(structure2);
        });
    }

    @Test
    @Transactional
    public void testValidLongitudeV2_Boundaries_ShouldBeAccepted() {
        Structure structure1 = new Structure();
        structure1.setName("Null Lon");
        structure1.setLongitudeV2(null);
        structureRepository.saveAndFlush(structure1);
        
        Structure structure2 = new Structure();
        structure2.setName("Lon -180");
        structure2.setLongitudeV2(-180.0);
        structureRepository.saveAndFlush(structure2);
        
        Structure structure3 = new Structure();
        structure3.setName("Lon 180");
        structure3.setLongitudeV2(180.0);
        structureRepository.saveAndFlush(structure3);
        
        Structure structure4 = new Structure();
        structure4.setName("Lon Precision");
        structure4.setLongitudeV2(-17.467686);
        structureRepository.saveAndFlush(structure4);
    }

    @Test
    @Transactional
    public void testInvalidLongitudeV2_Boundaries_ShouldThrowException() {
        Structure structure1 = new Structure();
        structure1.setName("Lon -180.000001");
        structure1.setLongitudeV2(-180.000001);
        assertThrows(Exception.class, () -> {
            structureRepository.saveAndFlush(structure1);
        });

        Structure structure2 = new Structure();
        structure2.setName("Lon 180.000001");
        structure2.setLongitudeV2(180.000001);
        assertThrows(Exception.class, () -> {
            structureRepository.saveAndFlush(structure2);
        });
    }
}
