package com.ditix.backend.Structure.Repository;

import com.ditix.backend.Structure.Model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StructureRepository extends JpaRepository<Structure, Long> {
    Optional<Structure> findByNameIgnoreCase(String name);
    List<Structure> findByRegion(String region);
    List<Structure> findByMinistere(String ministere);
}