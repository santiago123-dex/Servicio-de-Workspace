package backend.workspace.service;

import backend.workspace.dto.WorkspaceMember.WorkspaceMemberRequest;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberResponse;
import backend.workspace.entity.WorkspaceMember;
import backend.workspace.exception.WorkspaceMember.MemberAlreadyExistException;
import backend.workspace.exception.WorkspaceMember.MemberNotFoundException;
import backend.workspace.repository.WorkspaceMemberRepository;
import backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;

    public WorkspaceMemberService(WorkspaceMemberRepository workspaceMemberRepository, WorkspaceRepository workspaceRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceRepository = workspaceRepository;
    }

    //Agregar como ADMIN al creador de Workspace automaticamente
    public void addOwnerAsAdmin(Integer workspaceId, UUID ownerId){
        WorkspaceMember owner = WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(ownerId)
                .role(WorkspaceMember.Role.ADMIN)
                .build();
        workspaceMemberRepository.save(owner);
    }

    // Invitar a un usuario a un workspace
    public WorkspaceMemberResponse addMember(WorkspaceMemberRequest request){
        existById(request.workspaceId());
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(request.workspaceId(), request.userId())){
            throw new MemberAlreadyExistException(request.userId(), request.workspaceId());
        }

        // Si tiene role, lo toma, sino es miembro
        WorkspaceMember.Role role = request.role() != null ? request.role() : WorkspaceMember.Role.MEMBER;

        WorkspaceMember workspaceMember = builderWorkspaceMember(request);
        workspaceMemberRepository.save(workspaceMember);
        return WorkspaceMemberResponse.fromEntity(workspaceMember, "Miembro agregado correctamente");
    }

    // Obtener todos los miembros del workspace
    public List<WorkspaceMemberResponse> getMembersByWorkspace(Integer workspaceId){
        // Comprueba en la tabla de workspace si existe el workspace
        existById(workspaceId);

        return workspaceMemberRepository.findByWorkspaceId(workspaceId)
                // Transforma la lista de WorkspaceMember a WorkspaceMemberResponse
                // Stream convierte la lista en un stream para que pueda ejecutar diferentes procesos
                .stream()
                // Recorre la lista de WorkspaceMember y por cada miembro lo convierte en un WorkspaceMemberResponse
                .map( member -> WorkspaceMemberResponse.fromEntity(member, "Miembro encontrado") )
                // toList convierte el stream en una lista
                .toList();
    }

    //Obtener todos los workspaces de un usuario
    public List<WorkspaceMemberResponse> getWorkspacesByUser(UUID userId) {
        return workspaceMemberRepository.findByUserId(userId)
                .stream()
                .map(member -> WorkspaceMemberResponse.fromEntity(member, "Workspace encontrado"))
                .toList();
    }

    /*
    //Actualizar el role de un miembro
    public WorkspaceMemberResponse updateMemberRole(Integer memberId, WorkspaceMember.Role newRole) {
        WorkspaceMember workspaceMember = findWorkspaceMemberById(memberId);
        workspaceMember.setRole(newRole);
        workspaceMemberRepository.save(workspaceMember);

        return WorkspaceMemberResponse.fromEntity(workspaceMember,  "Rol actualizado correctamente");
    }
    */

    //Eliminar un miembro del workspace
    public void deleteMember(Integer memberId){
        existById(memberId);
        workspaceMemberRepository.deleteById(memberId);
    }

    //Eliminar todos los miembros de un workspace, para poder borrar el workspace
    @Transactional
    public void deleteAllMembersByWorkspace(Integer workspaceId){
        existById(workspaceId);
        workspaceMemberRepository.deleteMembersByWorkspaceId(workspaceId);
    }

    // Metodo privados

    private WorkspaceMember findWorkspaceMemberById(Integer id){
        return workspaceMemberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    private WorkspaceMember builderWorkspaceMember(WorkspaceMemberRequest request){
        return WorkspaceMember.builder()
                .workspaceId(request.workspaceId())
                .userId(request.userId())
                .role(request.role())
                .build();
    }

    private void existById(Integer id){
        if (!workspaceMemberRepository.existsById(id)){
            throw new MemberNotFoundException(id);
        }
    }

}
