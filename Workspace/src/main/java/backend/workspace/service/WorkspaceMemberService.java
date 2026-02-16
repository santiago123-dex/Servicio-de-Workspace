package backend.workspace.service;

import backend.workspace.dto.WorkspaceMember.WorkspaceMemberRequest;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberResponse;
import backend.workspace.entity.WorkspaceMember;
import backend.workspace.exception.MemberAlreadyExistException;
import backend.workspace.exception.MemberNotFoundException;
import backend.workspace.exception.WorkspaceNotFoundException;
import backend.workspace.repository.WorkspaceMemberRepository;
import backend.workspace.repository.WorkspaceRepository;

import java.util.List;

public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;

    public WorkspaceMemberService(WorkspaceMemberRepository workspaceMemberRepository, WorkspaceRepository workspaceRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceRepository = workspaceRepository;
    }

    //Agregar como ADMIN al creador de Workspace automaticamente
    public void addOwnerAsAdmin(Integer workspaceId, Integer ownerId){
        WorkspaceMember owner = WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(ownerId)
                .role(WorkspaceMember.Role.ADMIN)
                .build();
        workspaceMemberRepository.save(owner);
    }

    // Invitar a un usuario a un workspace
    public WorkspaceMemberResponse addMember(WorkspaceMemberRequest request){
        if(!workspaceMemberRepository.existsById(request.getWorkspaceId())){
            throw new WorkspaceNotFoundException(request.getWorkspaceId());
        }
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(request.getWorkspaceId(), request.getUserId())){
            throw new MemberAlreadyExistException(request.getUserId(), request.getWorkspaceId());
        }

        // Si tiene role, lo toma, sino es miembro
        WorkspaceMember.Role role = request.getRole() != null ? request.getRole() : WorkspaceMember.Role.MEMBER;

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspaceId(request.getWorkspaceId())
                .userId(request.getUserId())
                .role(role)
                .build();
        workspaceMemberRepository.save(workspaceMember);
        return WorkspaceMemberResponse.fromEntity(workspaceMember, "Miembro agregado correctamente");
    }

    // Obtener todos los miembros del workspace
    public List<WorkspaceMemberResponse> getMembersByWorkspace(Integer workspaceId){
        // Comprueba en la tabla de workspace si existe el workspace
        if (!workspaceRepository.existsById(workspaceId)){
            throw new WorkspaceNotFoundException(workspaceId);
        }

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
    public List<WorkspaceMemberResponse> getWorkspacesByUser(Integer userId) {
        return workspaceMemberRepository.findByUserId(userId)
                .stream()
                .map(member -> WorkspaceMemberResponse.fromEntity(member, "Workspace encontrado"))
                .toList();
    }

    //Actualizar el role de un miembro
    public WorkspaceMemberResponse updateMemberRole(Integer memberId, WorkspaceMember.Role newRole){
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        workspaceMember.setRole(newRole);
        workspaceMemberRepository.save(workspaceMember);

        return WorkspaceMemberResponse.fromEntity(workspaceMember,  "Rol actualizado correctamente");
    }

    //Eliminar un miembro del workspace
    public void deleteMember(Integer memberId){
        if (!workspaceMemberRepository.existsById(memberId)){
            throw new MemberNotFoundException(memberId);
        }
        workspaceMemberRepository.deleteById(memberId);
    }

}
