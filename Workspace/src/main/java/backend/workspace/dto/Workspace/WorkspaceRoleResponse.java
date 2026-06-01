package backend.workspace.dto.Workspace;

import backend.workspace.entity.Workspace;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record WorkspaceRoleResponse(
        Integer id,
        String name,
        String description,
        Workspace.WorkspaceStatus status,
        String message,
        Map<String, Object> data,
        String role
) {
    public static WorkspaceRoleResponse fromEntity(Workspace workspace, String message, UUID userId){
        Map<String, Object> safeData = sanitizeData(workspace.getData());
        return new WorkspaceRoleResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getStatus(),
                message,
                safeData,
                userId.equals(workspace.getOwnerUserID()) ? "ADMIN" : "MEMBER"
        );
    }

    private static Map<String, Object> sanitizeData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        Map<String, Object> safeData = new HashMap<>(data);
        safeData.remove("encodedCode");
        safeData.remove("code");
        return safeData.isEmpty() ? null : safeData;
    }
}
