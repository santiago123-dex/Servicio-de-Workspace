package backend.workspace.dto.Report;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record BasicWorkspaceReportResponse(
        Integer workspaceId,
        Integer totalAssignments,
        Integer gradedSubmissions,
        Integer pendingSubmissions,
        BigDecimal averageScore
) {
    public static BasicWorkspaceReportResponse of(
            Integer workspaceId,
            int totalAssignments,
            int gradedSubmissions,
            int pendingSubmissions,
            double averageScore
    ) {
        return new BasicWorkspaceReportResponse(
                workspaceId,
                totalAssignments,
                gradedSubmissions,
                pendingSubmissions,
                BigDecimal.valueOf(averageScore).setScale(2, RoundingMode.HALF_UP)
        );
    }
}
