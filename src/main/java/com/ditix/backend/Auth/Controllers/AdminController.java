package com.ditix.backend.Auth.Controllers;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Report.DTO.ReportResponseDTO;
import com.ditix.backend.Report.Services.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuthService authService;
    private final ReportService reportService;

    public AdminController(AuthService authService, ReportService reportService) {
        this.authService = authService;
        this.reportService = reportService;
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0")  int first,
            @RequestParam(defaultValue = "20") int max,
            @RequestParam(required = false)    String search
    ) {
        return ResponseEntity.ok(authService.getAllUsersPaginated(first, max, search));
    }

    @GetMapping("/users/{userId}/reports")
    public ResponseEntity<List<ReportResponseDTO>> getUserReports(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.getReportsByUserId(userId));
    }

    @PatchMapping("/reports/{id}/status")
    public ResponseEntity<ReportResponseDTO> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if ("APPROVED".equals(status)) {
            return ResponseEntity.ok(reportService.approveReport(id));
        } else if ("REJECTED".equals(status)) {
            return ResponseEntity.ok(reportService.rejectReport(id));
        } else {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Status invalide — utilise 'APPROVED' ou 'REJECTED'");
        }
    }


}