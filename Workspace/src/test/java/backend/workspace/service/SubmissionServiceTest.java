package backend.workspace.service;

import backend.workspace.dto.Submission.SubmissionRequest;
import backend.workspace.dto.Submission.SubmissionResponse;
import backend.workspace.dto.Submission.TeacherRequest;
import backend.workspace.entity.Assignment;
import backend.workspace.entity.Submission;
import backend.workspace.entity.Workspace;
import backend.workspace.entity.WorkspaceMember;
import backend.workspace.exception.Assignment.AssignmentExpiredException;
import backend.workspace.exception.Submission.SubmissionAlreadyExistException;
import backend.workspace.exception.Submission.SubmissionNotFoundException;
import backend.workspace.exception.WorkspaceMember.AdminRequiredException;
import backend.workspace.repository.SubmissionRepository;
import backend.workspace.repository.WorkspaceMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private Workspace workspace;
    private Assignment assignment;
    private Submission submission;
    private UUID userId;
    private SubmissionRequest request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        workspace = Workspace.builder()
                .id(1)
                .name("Workspace")
                .description("Desc")
                .status(Workspace.WorkspaceStatus.ACTIVO)
                .build();

        assignment = Assignment.builder()
                .id(8)
                .name("Tarea")
                .dueDate(OffsetDateTime.now().plusDays(1))
                .status(Assignment.AssignmentStatus.PUBLICADO)
                .workspace(workspace)
                .submissions(new ArrayList<>())
                .build();

        submission = Submission.builder()
                .id(15)
                .userId(userId)
                .assignment(assignment)
                .createdAt(OffsetDateTime.now())
                .content(Map.of("text", "respuesta"))
                .files(Map.of("url", "file.pdf"))
                .build();

        request = new SubmissionRequest(
                8,
                userId,
                Map.of("text", "respuesta"),
                Map.of("url", "file.pdf")
        );
    }

    @Test
    void shouldSubmitAssignment() {
        when(assignmentService.findAssignmentOrThrow(8)).thenReturn(assignment);
        when(submissionRepository.existsByAssignmentIdAndUserId(8, userId)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);

        SubmissionResponse response = submissionService.submitAssignment(request);

        assertEquals(15, response.id());
        assertEquals(8, response.assignmentId());
        assertEquals(userId, response.userId());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void shouldThrowWhenAssignmentIsExpiredByDate() {
        Assignment expired = Assignment.builder()
                .id(8)
                .dueDate(OffsetDateTime.now().minusMinutes(1))
                .status(Assignment.AssignmentStatus.PUBLICADO)
                .workspace(workspace)
                .submissions(new ArrayList<>())
                .build();
        when(assignmentService.findAssignmentOrThrow(8)).thenReturn(expired);

        assertThrows(AssignmentExpiredException.class, () -> submissionService.submitAssignment(request));
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void shouldThrowWhenSubmissionAlreadyExists() {
        when(assignmentService.findAssignmentOrThrow(8)).thenReturn(assignment);
        when(submissionRepository.existsByAssignmentIdAndUserId(8, userId)).thenReturn(true);

        assertThrows(SubmissionAlreadyExistException.class, () -> submissionService.submitAssignment(request));
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void shouldUpdateSubmission() {
        when(submissionRepository.findById(15)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(submission)).thenReturn(submission);

        SubmissionRequest updateRequest = new SubmissionRequest(
                8,
                userId,
                Map.of("text", "actualizada"),
                Map.of("url", "new.pdf")
        );

        SubmissionResponse response = submissionService.updateSubmission(15, updateRequest);

        assertEquals(Map.of("text", "actualizada"), response.content());
        assertEquals(Map.of("url", "new.pdf"), response.files());
        verify(submissionRepository).save(submission);
    }

    @Test
    void shouldThrowWhenUpdatingClosedAssignmentSubmission() {
        assignment.setStatus(Assignment.AssignmentStatus.CERRADO);
        when(submissionRepository.findById(15)).thenReturn(Optional.of(submission));

        assertThrows(AssignmentExpiredException.class, () -> submissionService.updateSubmission(15, request));
        verify(submissionRepository, never()).save(submission);
    }

    @Test
    void shouldDeleteSubmissionFromAssignmentAndRepository() {
        assignment.getSubmissions().add(submission);
        when(submissionRepository.findById(15)).thenReturn(Optional.of(submission));

        submissionService.deleteSubmission(15);

        assertEquals(0, assignment.getSubmissions().size());
        verify(submissionRepository).delete(submission);
    }

    @Test
    void shouldThrowWhenSubmissionNotFound() {
        when(submissionRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(SubmissionNotFoundException.class, () -> submissionService.getSubmissionById(404));
    }

    @Test
    void shouldGradeTeacherWhenAdmin() {
        UUID adminId = UUID.randomUUID();
        TeacherRequest teacherRequest = new TeacherRequest(adminId, new java.math.BigDecimal("4.5"), "Buen trabajo");
        WorkspaceMember adminMember = WorkspaceMember.builder()
                .userId(adminId)
                .role(WorkspaceMember.Role.ADMIN)
                .workspace(workspace)
                .build();

        when(submissionRepository.findById(15)).thenReturn(Optional.of(submission));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1, adminId)).thenReturn(Optional.of(adminMember));
        when(submissionRepository.save(submission)).thenReturn(submission);

        SubmissionResponse response = submissionService.gradeTeacher(15, teacherRequest);

        Map<String, Object> teacher = (Map<String, Object>) response.result().get("teacher");
        assertEquals(new java.math.BigDecimal("4.5"), teacher.get("score"));
        assertEquals("Buen trabajo", teacher.get("feedback"));
        verify(submissionRepository).save(submission);
    }

    @Test
    void shouldThrowWhenTeacherGraderIsNotAdmin() {
        UUID memberId = UUID.randomUUID();
        TeacherRequest teacherRequest = new TeacherRequest(memberId, new java.math.BigDecimal("4.2"), "Feedback");
        WorkspaceMember member = WorkspaceMember.builder()
                .userId(memberId)
                .role(WorkspaceMember.Role.MEMBER)
                .workspace(workspace)
                .build();

        when(submissionRepository.findById(15)).thenReturn(Optional.of(submission));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1, memberId)).thenReturn(Optional.of(member));

        assertThrows(AdminRequiredException.class, () -> submissionService.gradeTeacher(15, teacherRequest));
        verify(submissionRepository, never()).save(any(Submission.class));
    }
}
