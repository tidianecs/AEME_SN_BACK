package com.ditix.backend.Report.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Model.ReportStatus;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByCreatedByUserId(String userId);
    long countByCreatedByUserIdAndReportStatus(String userId, ReportStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Report r " +
            "JOIN r.profilUtilisateur p " +
            "JOIN p.structure s " +
            "JOIN s.ministereV2 m " +
            "WHERE p.role = 'GESTIONNAIRE' AND m.id = :ministereId")
    List<Report> findByMinistereIdAndRoleGestionnaire(@org.springframework.data.repository.query.Param("ministereId") Long ministereId);
}