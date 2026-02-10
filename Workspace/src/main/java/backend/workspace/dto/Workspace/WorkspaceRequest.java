package backend.workspace.dto.Workspace;

import backend.workspace.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRequest {

    private String name;
    private String description;
    private Workspace.WorkspaceStatus status;
    private Map<String, Object> data;
}
