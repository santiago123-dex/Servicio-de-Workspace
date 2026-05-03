package backend.workspace.controller;

import backend.workspace.dto.Assignment.AssignmentResponse;
import backend.workspace.dto.Submission.AiGradeRequest;
import backend.workspace.dto.Submission.InternalGradingContextResponse;
import backend.workspace.dto.Submission.SubmissionResponse;
import backend.workspace.service.AssignmentService;
import backend.workspace.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalGradingController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;

    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @GetMapping("/submissions/assignment/{assignmentId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByAssignment(@PathVariable Integer assignmentId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByAssignment(assignmentId));
    }

    @GetMapping("/grading-context/assignments/{assignmentId}")
    public ResponseEntity<InternalGradingContextResponse> getGradingContext(@PathVariable Integer assignmentId) {
        AssignmentResponse assignment = assignmentService.getAssignmentById(assignmentId);
        List<SubmissionResponse> submissions = submissionService.getSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(new InternalGradingContextResponse(assignment, submissions));
    }

    @PutMapping("/submissions/grade/{id}")
    public ResponseEntity<SubmissionResponse> gradeAi(
            @PathVariable Integer id,
            @Valid @RequestBody AiGradeRequest request
    ) {
        return ResponseEntity.ok(submissionService.gradeAi(id, request));
    }

}