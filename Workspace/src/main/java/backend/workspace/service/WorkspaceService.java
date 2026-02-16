package backend.workspace.service;

import backend.workspace.dto.Workspace.WorkspaceRequest;
import backend.workspace.dto.Workspace.WorkspaceResponse;
import backend.workspace.entity.Workspace;
import backend.workspace.exception.InvalidWorkspaceException;
import backend.workspace.exception.WorkspaceNotFoundException;
import backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberService workspaceMemberService;

    public WorkspaceService(WorkspaceRepository workspaceRepository, WorkspaceMemberService workspaceMemberService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberService = workspaceMemberService;

    }

    public WorkspaceResponse createWorkspace(WorkspaceRequest workspaceRequest) {

        if(workspaceRequest.getStatus() == Workspace.WorkspaceStatus.ARCHIVADO){
            throw new InvalidWorkspaceException("No se puede crear el workspace en estado ARCHIVADO");
        }

        Workspace workspace = Workspace.builder()
                .name(workspaceRequest.getName())
                .description(workspaceRequest.getDescription())
                .status(workspaceRequest.getStatus())
                .data(workspaceRequest.getData())
                .ownerUserID(1)
                .build();
        Workspace saved = workspaceRepository.save(workspace);

        workspaceMemberService.addOwnerAsAdmin(saved.getId(), saved.getOwnerUserID());

        return new WorkspaceResponse(workspace.getName(), workspace.getDescription(), workspace.getStatus(), "Workspace creado", workspace.getData());
    }

    public List<WorkspaceResponse> getAllWorkspaces() {
        List<Workspace> workspaces = workspaceRepository.findAll();
        return workspaces.stream()
                .map(workspace -> new WorkspaceResponse(workspace.getName(), workspace.getDescription(), workspace.getStatus(), "Workspaces encontrados", workspace.getData()))
                .toList();
    }

    public WorkspaceResponse getWorkspaceById(Integer id){
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));

        return new WorkspaceResponse(workspace.getName(), workspace.getDescription(), workspace.getStatus(), "Workspace encontrado", workspace.getData());
    }

    public WorkspaceResponse updateWorkspace(Integer id, WorkspaceRequest workspaceRequest){
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));

        workspace.setName(workspaceRequest.getName());
        workspace.setDescription(workspaceRequest.getDescription());
        workspace.setStatus(workspaceRequest.getStatus());
        workspace.setData(workspaceRequest.getData());

        workspaceRepository.save(workspace);

        return new WorkspaceResponse(workspace.getName(), workspace.getDescription(), workspace.getStatus(), "Workspace actualizado", workspace.getData());
    }

    public void deleteWorkspace(Integer id){
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));
        workspaceRepository.delete(workspace);
    }


}
