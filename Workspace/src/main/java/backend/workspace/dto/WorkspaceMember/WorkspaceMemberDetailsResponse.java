package backend.workspace.dto.WorkspaceMember;

import backend.workspace.entity.WorkspaceMember;

import java.util.UUID;

public record WorkspaceMemberDetailsResponse(
        Integer id,
        Integer workspaceId,
        UUID userId,
        WorkspaceMember.Role role,
        String firstName,
        String lastName,
        String fullName,
        String avatarUrl
) {
}
