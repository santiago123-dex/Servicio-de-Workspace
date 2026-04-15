package backend.workspace.exception.WorkspaceMember;

import java.util.UUID;

public class AdminRequiredException extends RuntimeException {
    public AdminRequiredException(UUID userId, Integer workspaceId) {
        super("El usuario " + userId + " no es ADMIN del workspace " + workspaceId);
    }
}
