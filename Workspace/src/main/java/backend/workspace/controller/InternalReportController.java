package backend.workspace.controller;

import backend.workspace.dto.Report.PerformanceDataResponse;
import backend.workspace.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/reports")
public class InternalReportController {

    private final ReportService reportService;

    @GetMapping("/workspaces/{workspaceId}/performance-data")
    public ResponseEntity<PerformanceDataResponse> getPerformanceData(
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) Integer assignmentId
    ) {
        return ResponseEntity.ok(reportService.getPerformanceData(workspaceId, assignmentId));
    }
}
