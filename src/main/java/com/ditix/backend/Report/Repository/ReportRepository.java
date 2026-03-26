package com.ditix.backend.Report.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ditix.backend.Report.Model.Report;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByCreatedByUserId(String userId);
    List<Report> findByCreatedByUserIdAndReportType(String userId, String reportType);
}