package backend.workspace.dto.WorkspaceMember;

import backend.workspace.entity.WorkspaceMember;
import lombok.Data;

@Data
public class WorkspaceMemberRequest {

    private Integer workspaceId;
    private Integer userId;
    private WorkspaceMember.Role role;


}
