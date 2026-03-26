package com.ditix.backend.Report.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false)
    private LocalDateTime reportDate;

    @Column(nullable = false)
    private String reportLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;

    @Column(columnDefinition = "TEXT")
    private String reportDesc;

    private String fileName;
    private String filePath;
    private String contentType;
    private Long fileSize;

    @Column(nullable = false)
    private String createdByUserId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.reportStatus == null) {
            this.reportStatus = ReportStatus.DRAFT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getReportType() { 
        return reportType; 
    }
    public void setReportType(String reportType) { 
        this.reportType = reportType; 
    }

    public LocalDateTime getReportDate() { 
        return reportDate; 
    }
    public void setReportDate(LocalDateTime reportDate) { 
        this.reportDate = reportDate; 
    }

    public String getReportLocation() { 
        return reportLocation; 
    }
    public void setReportLocation(String reportLocation) { 
        this.reportLocation = reportLocation; 
    }

    public ReportStatus getReportStatus() { 
        return reportStatus; 
    }
    public void setReportStatus(ReportStatus reportStatus) { 
        this.reportStatus = reportStatus; 
    }

    public String getReportDesc() { return reportDesc; }
    public void setReportDesc(String reportDesc) { 
        this.reportDesc = reportDesc; 
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { 
        this.fileName = fileName; 
    }

    public String getFilePath() { 
        return filePath; 
    }
    public void setFilePath(String filePath) { 
        this.filePath = filePath; 
    }

    public String getContentType() { 
        return contentType; 
    }
    public void setContentType(String contentType) { 
        this.contentType = contentType; 
    }

    public Long getFileSize() { 
        return fileSize; 
    }
    public void setFileSize(Long fileSize) { 
        this.fileSize = fileSize; 
    }

    public String getCreatedByUserId() { 
        return createdByUserId; 
    }
    public void setCreatedByUserId(String createdByUserId) { 
        this.createdByUserId = createdByUserId; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
}
