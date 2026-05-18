package backend.workspace.dto.User;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String avatarUrl
) {
}
