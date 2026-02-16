package backend.workspace.dto.WorkspaceMember;

import backend.workspace.entity.WorkspaceMember;

public record WorkspaceMemberResponse(
    Integer id,
    Integer workspaceId,
    Integer userId,
    WorkspaceMember.Role role,
    String message
) {

    public static WorkspaceMemberResponse fromEntity(WorkspaceMember member, String message){
        return new WorkspaceMemberResponse(
                member.getId(),
                member.getWorkspaceId(),
                member.getUserId(),
                member.getRole(),
                message
        );
    }
}
