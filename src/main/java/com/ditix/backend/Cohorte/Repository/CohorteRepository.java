package com.ditix.backend.Cohorte.Repository;

import com.ditix.backend.Cohorte.Model.Cohorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CohorteRepository extends JpaRepository<Cohorte, Long> {
}
