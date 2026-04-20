package backend.workspace.controller;

import backend.workspace.dto.Submission.AiGradeRequest;
import backend.workspace.dto.Submission.SubmissionResponse;
import backend.workspace.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/workspaces/submission")
public class InternalSubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/{id}/grade-ai")
    public ResponseEntity<SubmissionResponse> gradeAi(
            @PathVariable Integer id,
            @Valid @RequestBody AiGradeRequest request
    ) {
        SubmissionResponse response = submissionService.gradeAi(id, request);
        return ResponseEntity.ok(response);
    }
}
