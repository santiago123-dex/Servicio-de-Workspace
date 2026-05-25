package backend.workspace.controller;

import backend.workspace.dto.Report.BasicWorkspaceReportResponse;
import backend.workspace.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{workspaceId}/reports/basic")
    public ResponseEntity<BasicWorkspaceReportResponse> getBasicWorkspaceReport(@PathVariable Integer workspaceId) {
        return ResponseEntity.ok(reportService.getBasicWorkspaceReport(workspaceId));
    }
}
