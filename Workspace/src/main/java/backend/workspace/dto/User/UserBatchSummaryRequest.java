package backend.workspace.dto.User;

import java.util.List;
import java.util.UUID;

public record UserBatchSummaryRequest(
        List<UUID> userIds
) {
}
