package backend.workspace.dto.Report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public record PerformanceDataResponse(
        Integer workspaceId,
        Integer assignmentId,
        Summary summary,
        List<AssignmentMetric> assignments,
        List<StudentMetric> students
) {
    public record Summary(
            Integer totalAssignments,
            Integer totalSubmissions,
            Integer gradedSubmissions,
            Integer pendingSubmissions,
            BigDecimal averageScore
    ) {
        public static Summary of(
                int totalAssignments,
                int totalSubmissions,
                int gradedSubmissions,
                int pendingSubmissions,
                double averageScore
        ) {
            return new Summary(
                    totalAssignments,
                    totalSubmissions,
                    gradedSubmissions,
                    pendingSubmissions,
                    round(averageScore)
            );
        }
    }

    public record AssignmentMetric(
            Integer assignmentId,
            String assignmentName,
            Integer totalSubmissions,
            Integer gradedSubmissions,
            Integer pendingSubmissions,
            BigDecimal averageScore,
            BigDecimal maxScore,
            Integer failedSubmissions,
            BigDecimal failureRate
    ) {
        public static AssignmentMetric of(
                Integer assignmentId,
                String assignmentName,
                int totalSubmissions,
                int gradedSubmissions,
                int pendingSubmissions,
                double averageScore,
                double maxScore,
                int failedSubmissions,
                double failureRate
        ) {
            return new AssignmentMetric(
                    assignmentId,
                    assignmentName,
                    totalSubmissions,
                    gradedSubmissions,
                    pendingSubmissions,
                    round(averageScore),
                    round(maxScore),
                    failedSubmissions,
                    round(failureRate)
            );
        }
    }

    public record StudentMetric(
            UUID userId,
            Integer totalSubmissions,
            Integer gradedSubmissions,
            Integer pendingSubmissions,
            BigDecimal averageScore
    ) {
        public static StudentMetric of(
                UUID userId,
                int totalSubmissions,
                int gradedSubmissions,
                int pendingSubmissions,
                double averageScore
        ) {
            return new StudentMetric(
                    userId,
                    totalSubmissions,
                    gradedSubmissions,
                    pendingSubmissions,
                    round(averageScore)
            );
        }
    }

    private static BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
