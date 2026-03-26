package com.ditix.backend.Report.DTO;

import java.time.LocalDateTime;
import com.ditix.backend.Report.Model.Report;

public class ReportResponseDTO {

    private Long id;
    private String reportType;
    private LocalDateTime reportDate;
    private String reportLocation;
    private String reportStatus;
    private String reportDesc;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor depuis entité
    public ReportResponseDTO(Report report) {
        this.id = report.getId();
        this.reportType = report.getReportType();
        this.reportDate = report.getReportDate();
        this.reportLocation = report.getReportLocation();
        this.reportStatus = report.getReportStatus().name();
        this.reportDesc = report.getReportDesc();
        this.fileName = report.getFileName();
        this.contentType = report.getContentType();
        this.fileSize = report.getFileSize();
        this.createdByUserId = report.getCreatedByUserId();
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();
    }

    public Long getId() { 
        return id; 
    }
    public String getReportType() { 
        return reportType; 
    }
    public LocalDateTime getReportDate() { 
        return reportDate; 
    }
    public String getReportLocation() { 
        return reportLocation; 
    }
    public String getReportStatus() { 
        return reportStatus; 
    }
    public String getReportDesc() { 
        return reportDesc;
    }
    public String getFileName() { 
        return fileName;
    }
    public String getContentType() { 
        return contentType; 
    }
    public Long getFileSize() { 
        return fileSize; 
    }
    public String getCreatedByUserId() { 
        return createdByUserId; 
    }
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
}
