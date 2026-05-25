package backend.workspace.service;

import backend.workspace.dto.Workspace.WorkspaceCodeResponse;
import backend.workspace.dto.Workspace.WorkspaceRequest;
import backend.workspace.dto.Workspace.WorkspaceResponse;
import backend.workspace.dto.Workspace.WorkspaceRoleResponse;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberResponse;
import backend.workspace.entity.Workspace;
import backend.workspace.exception.Workspace.WorkspaceNotFoundException;
import backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkspaceService {

    public static final String ENCODED_CODE_KEY = "encodedCode";

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberService workspaceMemberService;
    private final WorkspaceCodeCodec workspaceCodeCodec;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberService workspaceMemberService,
            WorkspaceCodeCodec workspaceCodeCodec
    ) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberService = workspaceMemberService;
        this.workspaceCodeCodec = workspaceCodeCodec;
    }

    @Transactional
    public WorkspaceResponse createWorkspace(UUID currentUserId, WorkspaceRequest workspaceRequest) {
        Workspace workspace = buildWorkspace(workspaceRequest, currentUserId);
        Workspace saved = workspaceRepository.save(workspace);
        workspaceMemberService.addOwnerAsAdmin(saved, saved.getOwnerUserID());
        return WorkspaceResponse.fromEntity(saved, "Workspace creado correctamente");
    }

    //Obtener todos los workspaces de un usuario
    public List<WorkspaceResponse> getOwnWorkspaces(UUID userId) {
        return workspaceRepository.findByOwnerUserID(userId)
                .stream()
                .map(workspace -> WorkspaceResponse.fromEntity(workspace, "Workspace encontrado"))
                .toList();
    }

    public WorkspaceResponse getWorkspaceById(Integer id) {
        Workspace workspace = findWorkspaceById(id);
        return WorkspaceResponse.fromEntity(workspace, "Workspace obtenido correctamente");
    }

    public List<WorkspaceRoleResponse> getAllWorkspaces(UUID userId) {
        return workspaceRepository.getAllWorkspaces(userId)
                .stream()
                .map(member -> WorkspaceRoleResponse.fromEntity(member, "Espacios totales", userId))
                .toList();
    }

    public WorkspaceResponse updateWorkspace(Integer id, WorkspaceRequest workspaceRequest) {
        Workspace workspace = findWorkspaceById(id);
        updateWorkspaceFields(workspace, workspaceRequest);
        workspaceRepository.save(workspace);
        return WorkspaceResponse.fromEntity(workspace, "Workspace Actulizado");
    }

    public WorkspaceCodeResponse getInvitationCode(Integer workspaceId) {
        Workspace workspace = findWorkspaceById(workspaceId);
        String encodedCode = extractEncodedCode(workspace);
        return new WorkspaceCodeResponse(workspace.getId(), workspaceCodeCodec.decode(encodedCode));
    }

    @Transactional
    public void deleteWorkspace(Integer id) {
        Workspace workspace = findWorkspaceOrThrow(id);
        workspaceRepository.delete(workspace);
    }

    public Workspace findWorkspaceOrThrow(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));
    }

    private Workspace buildWorkspace(WorkspaceRequest request, UUID currenteUserId) {
        return Workspace.builder()
                .name(request.name())
                .description(request.description())
                .status(Workspace.WorkspaceStatus.ACTIVO)
                .data(buildEncodedCodeData(request.data().code()))
                .ownerUserID(currenteUserId)
                .build();
    }

    private Workspace findWorkspaceById(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));
    }

    private void updateWorkspaceFields(Workspace workspace, WorkspaceRequest workspaceRequest) {
        workspace.setName(workspaceRequest.name());
        workspace.setDescription(workspaceRequest.description());
        if (workspaceRequest.status() != null) {
            workspace.setStatus(workspaceRequest.status());
        }
        workspace.setData(buildEncodedCodeData(workspaceRequest.data().code()));
    }

    private Map<String, Object> buildEncodedCodeData(String code) {
        Map<String, Object> data = new HashMap<>();
        data.put(ENCODED_CODE_KEY, workspaceCodeCodec.encode(code));
        return data;
    }

    private String extractEncodedCode(Workspace workspace) {
        Map<String, Object> data = workspace.getData();
        if (data == null) {
            throw new WorkspaceNotFoundException("El workspace no tiene codigo configurado");
        }

        Object encoded = data.get(ENCODED_CODE_KEY);
        if (!(encoded instanceof String encodedCode) || encodedCode.isBlank()) {
            throw new WorkspaceNotFoundException("El workspace no tiene codigo configurado");
        }

        return encodedCode;
    }
}
