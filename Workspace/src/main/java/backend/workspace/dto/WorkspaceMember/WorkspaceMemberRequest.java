package backend.workspace.dto.WorkspaceMember;

import backend.workspace.entity.WorkspaceMember;
import jakarta.validation.constraints.NotNull;


import java.util.UUID;

// Se usa en vez de lombok para que los datos sean inmutables

public record WorkspaceMemberRequest (

        @NotNull(message = "El workspaceId es obligatorio")
        Integer workspaceId,
        UUID userId,
        WorkspaceMember.Role role

){}
