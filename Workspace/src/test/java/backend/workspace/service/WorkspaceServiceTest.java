package backend.workspace.service;

import backend.workspace.dto.Workspace.WorkspaceRequest;
import backend.workspace.dto.Workspace.WorkspaceResponse;
import backend.workspace.entity.Workspace;
import backend.workspace.exception.Workspace.WorkspaceNotFoundException;
import backend.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberService workspaceMemberService;

    @InjectMocks
    private WorkspaceService workspaceService;

    private WorkspaceRequest request;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        request = new WorkspaceRequest(
                "Nuevo workspace",
                "Descripcion de prueba",
                Workspace.WorkspaceStatus.ARCHIVADO,
                Map.of("code", "ABC123")
        );

        workspace = Workspace.builder()
                .id(1)
                .name("Nuevo workspace")
                .description("Descripcion de prueba")
                .status(Workspace.WorkspaceStatus.ACTIVO)
                .ownerUserID(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .data(Map.of("code", "ABC123"))
                .build();
    }

    @Test
    void shouldCreateWorkspaceAndAddOwnerAsAdmin() {
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        WorkspaceResponse response = workspaceService.createWorkspace(UUID.fromString("00000000-0000-0000-0000-000000000001"), request);

        assertEquals(1, response.id());
        assertEquals("Nuevo workspace", response.name());
        assertEquals(Workspace.WorkspaceStatus.ACTIVO, response.status());
        verify(workspaceRepository).save(any(Workspace.class));
        verify(workspaceMemberService).addOwnerAsAdmin(workspace, workspace.getOwnerUserID());
    }

    @Test
    void shouldGetAllWorkspaces() {
        when(workspaceRepository.findAll()).thenReturn(List.of(workspace));

        List<WorkspaceResponse> responses = workspaceService.getAllWorkspaces();

        assertEquals(1, responses.size());
        assertEquals("Nuevo workspace", responses.getFirst().name());
        assertEquals("Workspaces obtenidos correctamente", responses.getFirst().message());
    }

    @Test
    void shouldGetWorkspaceById() {
        when(workspaceRepository.findById(1)).thenReturn(Optional.of(workspace));

        WorkspaceResponse response = workspaceService.getWorkspaceById(1);

        assertEquals(1, response.id());
        assertEquals("Workspace obtenido correctamente", response.message());
    }

    @Test
    void shouldThrowWhenWorkspaceByIdNotFound() {
        when(workspaceRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.getWorkspaceById(404));
    }

    @Test
    void shouldUpdateWorkspaceFields() {
        WorkspaceRequest updateRequest = new WorkspaceRequest(
                "Nombre actualizado",
                "Descripcion actualizada",
                Workspace.WorkspaceStatus.ARCHIVADO,
                Map.of("code", "XYZ")
        );
        when(workspaceRepository.findById(1)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        WorkspaceResponse response = workspaceService.updateWorkspace(1, updateRequest);

        assertEquals("Nombre actualizado", response.name());
        assertEquals("Descripcion actualizada", response.description());
        assertEquals(Workspace.WorkspaceStatus.ARCHIVADO, response.status());
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldDeleteWorkspace() {
        when(workspaceRepository.findById(1)).thenReturn(Optional.of(workspace));

        workspaceService.deleteWorkspace(1);

        verify(workspaceRepository).delete(workspace);
    }
}
