package backend.workspace.dto.Submission;

import backend.workspace.dto.Assignment.AssignmentResponse;

import java.util.List;

public record InternalGradingContextResponse(
        AssignmentResponse assignment,
        List<SubmissionResponse> submissions
) {
}
