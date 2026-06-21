package backend.workspace.service;

import backend.workspace.dto.Report.BasicWorkspaceReportResponse;
import backend.workspace.dto.Report.PerformanceDataResponse;
import backend.workspace.entity.Assignment;
import backend.workspace.entity.Submission;
import backend.workspace.entity.WorkspaceMember;
import backend.workspace.exception.Assignment.AssignmentNotFoundException;
import backend.workspace.repository.AssignmentRepository;
import backend.workspace.repository.SubmissionRepository;
import backend.workspace.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final double FAILURE_THRESHOLD = 0.60d;

    private final WorkspaceService workspaceService;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public ReportService(
            WorkspaceService workspaceService,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            WorkspaceMemberRepository workspaceMemberRepository
    ) {
        this.workspaceService = workspaceService;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public BasicWorkspaceReportResponse getBasicWorkspaceReport(Integer workspaceId) {
        workspaceService.findWorkspaceOrThrow(workspaceId);

        List<Assignment> assignments = assignmentRepository.findByWorkspaceId(workspaceId);
        List<Submission> submissions = submissionRepository.findByAssignmentWorkspaceId(workspaceId);

        Set<UUID> adminUserIds = workspaceMemberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .filter(m -> m.getRole() == WorkspaceMember.Role.ADMIN)
                .map(WorkspaceMember::getUserId)
                .collect(Collectors.toSet());

        int gradedSubmissions = 0;
        int pendingSubmissions = 0;
        double scoreSum = 0.0d;

        for (Submission submission : submissions) {
            if (adminUserIds.contains(submission.getUserId())) {
                continue;
            }
            OptionalDouble score = extractPreferredScore(submission.getResult());
            if (score.isPresent()) {
                gradedSubmissions++;
                scoreSum += score.getAsDouble();
            } else {
                pendingSubmissions++;
            }
        }

        double averageScore = gradedSubmissions > 0 ? scoreSum / gradedSubmissions : 0.0d;

        return BasicWorkspaceReportResponse.of(
                workspaceId,
                assignments.size(),
                gradedSubmissions,
                pendingSubmissions,
                averageScore
        );
    }

    public PerformanceDataResponse getPerformanceData(Integer workspaceId, Integer assignmentId) {
        workspaceService.findWorkspaceOrThrow(workspaceId);

        List<Assignment> allWorkspaceAssignments = assignmentRepository.findByWorkspaceId(workspaceId);
        Map<Integer, Assignment> assignmentsById = new HashMap<>();
        for (Assignment assignment : allWorkspaceAssignments) {
            assignmentsById.put(assignment.getId(), assignment);
        }

        List<Assignment> scopedAssignments;
        List<Submission> submissions;
        if (assignmentId != null) {
            Assignment scopedAssignment = assignmentsById.get(assignmentId);
            if (scopedAssignment == null) {
                throw new AssignmentNotFoundException(assignmentId);
            }
            scopedAssignments = List.of(scopedAssignment);
            submissions = submissionRepository.findByAssignmentId(assignmentId);
        } else {
            scopedAssignments = allWorkspaceAssignments;
            submissions = submissionRepository.findByAssignmentWorkspaceId(workspaceId);
        }

        Map<Integer, AssignmentAccumulator> assignmentAccumulators = new HashMap<>();
        for (Assignment assignment : scopedAssignments) {
            assignmentAccumulators.put(
                    assignment.getId(),
                    new AssignmentAccumulator(
                            assignment.getId(),
                            assignment.getName(),
                            calculateAssignmentMaxScore(assignment.getRubric())
                    )
            );
        }

        // Excluir entregas de admins (los admins no son estudiantes)
        Set<UUID> adminUserIds = workspaceMemberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .filter(m -> m.getRole() == WorkspaceMember.Role.ADMIN)
                .map(WorkspaceMember::getUserId)
                .collect(Collectors.toSet());

        Map<UUID, StudentAccumulator> studentAccumulators = new HashMap<>();

        int gradedSubmissions = 0;
        double scoreSum = 0.0d;

        for (Submission submission : submissions) {
            if (adminUserIds.contains(submission.getUserId())) {
                continue;
            }
            AssignmentAccumulator assignmentAccumulator = assignmentAccumulators.get(submission.getAssignment().getId());
            if (assignmentAccumulator == null) {
                continue;
            }

            assignmentAccumulator.totalSubmissions++;

            StudentAccumulator studentAccumulator = studentAccumulators.computeIfAbsent(
                    submission.getUserId(),
                    StudentAccumulator::new
            );
            studentAccumulator.totalSubmissions++;

            OptionalDouble score = extractPreferredScore(submission.getResult());
            if (score.isPresent()) {
                double scoreValue = score.getAsDouble();
                gradedSubmissions++;
                scoreSum += scoreValue;

                assignmentAccumulator.gradedSubmissions++;
                assignmentAccumulator.scoreSum += scoreValue;

                studentAccumulator.gradedSubmissions++;
                studentAccumulator.scoreSum += scoreValue;

                if (assignmentAccumulator.maxScore > 0) {
                    double ratio = scoreValue / assignmentAccumulator.maxScore;
                    if (ratio < FAILURE_THRESHOLD) {
                        assignmentAccumulator.failedSubmissions++;
                    }
                }
            }
        }

        List<PerformanceDataResponse.AssignmentMetric> assignmentMetrics = assignmentAccumulators.values()
                .stream()
                .sorted(Comparator.comparing(AssignmentAccumulator::assignmentId))
                .map(AssignmentAccumulator::toDto)
                .toList();

        List<PerformanceDataResponse.StudentMetric> studentMetrics = studentAccumulators.values()
                .stream()
                .sorted(Comparator.comparing(StudentAccumulator::userId))
                .map(StudentAccumulator::toDto)
                .toList();

        int totalSubmissions = submissions.size();
        int pendingSubmissions = totalSubmissions - gradedSubmissions;
        double averageScore = gradedSubmissions > 0 ? scoreSum / gradedSubmissions : 0.0d;

        return new PerformanceDataResponse(
                workspaceId,
                assignmentId,
                PerformanceDataResponse.Summary.of(
                        scopedAssignments.size(),
                        totalSubmissions,
                        gradedSubmissions,
                        pendingSubmissions,
                        averageScore
                ),
                assignmentMetrics,
                studentMetrics
        );
    }

    private OptionalDouble extractPreferredScore(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return OptionalDouble.empty();
        }

        Double teacherScore = extractScoreFromBlock(result.get("teacher"));
        if (teacherScore != null) {
            return OptionalDouble.of(teacherScore);
        }

        Double aiScore = extractScoreFromBlock(result.get("ai"));
        if (aiScore != null) {
            return OptionalDouble.of(aiScore);
        }

        return OptionalDouble.empty();
    }

    private Double extractScoreFromBlock(Object block) {
        if (!(block instanceof Map<?, ?> map)) {
            return null;
        }

        Object rawScore = map.get("score");
        if (rawScore instanceof Number number) {
            return number.doubleValue();
        }

        if (rawScore instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private double calculateAssignmentMaxScore(Map<String, Object> rubric) {
        if (rubric == null || rubric.isEmpty()) {
            return 0.0d;
        }

        Object criteria = rubric.get("criteria");
        if (criteria instanceof List<?> rawCriteria) {
            double sum = 0.0d;
            for (Object item : rawCriteria) {
                if (!(item instanceof Map<?, ?> criterion)) {
                    continue;
                }

                Object value = criterion.get("value");
                if (!(value instanceof Number)) {
                    value = criterion.get("weight");
                }

                if (value instanceof Number number) {
                    sum += number.doubleValue();
                }
            }
            return sum;
        }

        double sum = 0.0d;
        for (Map.Entry<String, Object> entry : rubric.entrySet()) {
            if (entry.getValue() instanceof Number number) {
                sum += number.doubleValue();
            }
        }
        return sum;
    }

    private static class AssignmentAccumulator {
        private final Integer assignmentId;
        private final String assignmentName;
        private final double maxScore;
        private int totalSubmissions = 0;
        private int gradedSubmissions = 0;
        private int failedSubmissions = 0;
        private double scoreSum = 0.0d;

        private AssignmentAccumulator(Integer assignmentId, String assignmentName, double maxScore) {
            this.assignmentId = assignmentId;
            this.assignmentName = assignmentName;
            this.maxScore = maxScore;
        }

        private Integer assignmentId() {
            return assignmentId;
        }

        private PerformanceDataResponse.AssignmentMetric toDto() {
            int pendingSubmissions = totalSubmissions - gradedSubmissions;
            double averageScore = gradedSubmissions > 0 ? scoreSum / gradedSubmissions : 0.0d;
            double failureRate = gradedSubmissions > 0
                    ? (double) failedSubmissions / gradedSubmissions
                    : 0.0d;

            return PerformanceDataResponse.AssignmentMetric.of(
                    assignmentId,
                    assignmentName,
                    totalSubmissions,
                    gradedSubmissions,
                    pendingSubmissions,
                    averageScore,
                    maxScore,
                    failedSubmissions,
                    failureRate
            );
        }
    }

    private static class StudentAccumulator {
        private final UUID userId;
        private int totalSubmissions = 0;
        private int gradedSubmissions = 0;
        private double scoreSum = 0.0d;

        private StudentAccumulator(UUID userId) {
            this.userId = userId;
        }

        private UUID userId() {
            return userId;
        }

        private PerformanceDataResponse.StudentMetric toDto() {
            int pendingSubmissions = totalSubmissions - gradedSubmissions;
            double averageScore = gradedSubmissions > 0 ? scoreSum / gradedSubmissions : 0.0d;
            return PerformanceDataResponse.StudentMetric.of(
                    userId,
                    totalSubmissions,
                    gradedSubmissions,
                    pendingSubmissions,
                    averageScore
            );
        }
    }
}
