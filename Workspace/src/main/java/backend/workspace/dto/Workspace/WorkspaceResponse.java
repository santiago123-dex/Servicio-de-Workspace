package backend.workspace.dto.Workspace;

import backend.workspace.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {

    private String name;
    private String description;
    private Workspace.WorkspaceStatus status;
    private String message;
    private Map<String, Object> data;

}
