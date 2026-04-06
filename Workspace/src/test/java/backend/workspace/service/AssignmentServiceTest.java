package backend.workspace.service;

import backend.workspace.dto.Assignment.AssignmentRequest;
import backend.workspace.dto.Assignment.AssignmentResponse;
import backend.workspace.entity.Assignment;
import backend.workspace.entity.Workspace;
import backend.workspace.exception.Assignment.AssignmentNotFoundException;
import backend.workspace.exception.Workspace.WorkspaceNotFoundException;
import backend.workspace.repository.AssignmentRepository;
import backend.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceService workspaceService;

    @InjectMocks
    private AssignmentService assignmentService;

    private Workspace workspace;
    private Assignment assignment;
    private AssignmentRequest request;

    @BeforeEach
    void setUp() {
        workspace = Workspace.builder()
                .id(1)
                .name("Workspace")
                .description("Desc")
                .status(Workspace.WorkspaceStatus.ACTIVO)
                .assignments(new ArrayList<>())
                .build();

        assignment = Assignment.builder()
                .id(2)
                .name("Tarea")
                .description("Descripcion")
                .dueDate(OffsetDateTime.now().plusDays(2))
                .status(Assignment.AssignmentStatus.PUBLICADO)
                .workspace(workspace)
                .rubric(Map.of("puntaje", 100))
                .settings(Map.of("late", false))
                .build();

        request = new AssignmentRequest(
                1,
                "Tarea",
                "Descripcion",
                OffsetDateTime.now().plusDays(2),
                Map.of("puntaje", 100),
                Map.of("late", false)
        );
    }

    @Test
    void shouldCreateAssignment() {
        when(workspaceService.findWorkspaceOrThrow(1)).thenReturn(workspace);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

        AssignmentResponse response = assignmentService.createAssignment(request);

        assertEquals(2, response.id());
        assertEquals(1, response.workspaceId());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void shouldThrowWhenWorkspaceDoesNotExist() {
        when(workspaceRepository.existsById(99)).thenReturn(false);

        assertThrows(WorkspaceNotFoundException.class, () -> assignmentService.getAssignmentsByWorkspace(99));
        verify(assignmentRepository, never()).findByWorkspaceId(99);
    }

    @Test
    void shouldReturnAssignmentsByWorkspace() {
        when(workspaceRepository.existsById(1)).thenReturn(true);
        when(assignmentRepository.findByWorkspaceId(1)).thenReturn(List.of(assignment));

        List<AssignmentResponse> response = assignmentService.getAssignmentsByWorkspace(1);

        assertEquals(1, response.size());
        assertEquals(2, response.getFirst().id());
        assertEquals("Tarea", response.getFirst().name());
    }

    @Test
    void shouldCloseExpiredAssignment() {
        Assignment expired = Assignment.builder()
                .id(9)
                .name("Vencida")
                .dueDate(OffsetDateTime.now().minusDays(1))
                .status(Assignment.AssignmentStatus.PUBLICADO)
                .workspace(workspace)
                .build();
        when(assignmentRepository.save(expired)).thenReturn(expired);

        Assignment updated = assignmentService.updateStatusIfExpired(expired);

        assertEquals(Assignment.AssignmentStatus.CERRADO, updated.getStatus());
        verify(assignmentRepository).save(expired);
    }

    @Test
    void shouldThrowWhenAssignmentNotFound() {
        when(assignmentRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(AssignmentNotFoundException.class, () -> assignmentService.getAssignmentById(404));
    }

    @Test
    void shouldDeleteAssignmentFromWorkspaceAndRepository() {
        workspace.getAssignments().add(assignment);
        when(assignmentRepository.findById(2)).thenReturn(Optional.of(assignment));

        assignmentService.deleteAssignment(2);

        assertEquals(0, workspace.getAssignments().size());
        verify(assignmentRepository).delete(assignment);
    }
}
