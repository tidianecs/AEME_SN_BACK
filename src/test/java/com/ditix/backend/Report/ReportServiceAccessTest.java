package com.ditix.backend.Report;

import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Repository.ReportRepository;
import com.ditix.backend.Report.Services.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceAccessTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private com.ditix.backend.Report.Services.ReportAutorisationService reportAutorisationService;

    @InjectMocks
    private ReportService reportService;

    private Report createMockReport(String createdByUserId) {
        Report report = new Report();
        report.setCreatedByUserId(createdByUserId);
        report.setReportStatus(com.ditix.backend.Report.Model.ReportStatus.SUBMITTED);
        return report;
    }

    @Test
    void getReportById_owner_shouldReturnReport() {
        Report report = createMockReport("owner-user-id");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportAutorisationService.peutLireRapport(eq(report), any())).thenReturn(true);

        assertDoesNotThrow(() -> reportService.getReportById(1L, null));
    }

    @Test
    void getReportById_admin_shouldReturnReport() {
        Report report = createMockReport("owner-user-id");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportAutorisationService.peutLireRapport(eq(report), any())).thenReturn(true);

        assertDoesNotThrow(() -> reportService.getReportById(1L, null));
    }

    @Test
    void getReportById_otherUser_shouldThrow403() {
        Report report = createMockReport("owner-user-id");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportAutorisationService.peutLireRapport(eq(report), any())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                reportService.getReportById(1L, null));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void getReportById_missingReport_shouldThrow404() {
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                reportService.getReportById(1L, null));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getRawReport_owner_shouldReturnReport() {
        Report report = createMockReport("owner-user-id");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportAutorisationService.peutLireRapport(eq(report), any())).thenReturn(true);

        assertDoesNotThrow(() -> reportService.getRawReport(1L, null));
    }

    @Test
    void getRawReport_admin_shouldReturnReport() {
        Report report = createMockReport("owner-user-id");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportAutorisationService.peutLireRapport(eq(report), any())).thenReturn(true);

        assertDoesNotThrow(() -> reportService.getRawReport(1L, null));
    }

    @Test
    void getRawReport_otherUser_shouldThrow403() {
        Report report = createMockReport("owner-user-id");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportAutorisationService.peutLireRapport(eq(report), any())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                reportService.getRawReport(1L, null));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void getRawReport_missingReport_shouldThrow404() {
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                reportService.getRawReport(1L, null));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
