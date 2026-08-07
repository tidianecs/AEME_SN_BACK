package com.ditix.backend.Ministere.Repository;

import com.ditix.backend.Ministere.Model.Ministere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MinistereRepository extends JpaRepository<Ministere, Long> {
}
