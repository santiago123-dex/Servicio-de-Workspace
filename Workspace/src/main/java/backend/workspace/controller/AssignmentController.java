package backend.workspace.controller;

import backend.workspace.dto.Assignment.AssignmentRequest;
import backend.workspace.dto.Assignment.AssignmentResponse;
import backend.workspace.entity.Assignment;
import backend.workspace.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

        private final AssignmentService assignmentService;

        @PostMapping()
        public ResponseEntity<AssignmentResponse> createAssignment(@Valid @RequestBody AssignmentRequest request){

                AssignmentResponse response = assignmentService.createAssignment(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);

        }

        @GetMapping("/workspace/{workspaceId}")
        public ResponseEntity<List<AssignmentResponse>> getAssignmentsByWorkspace(@PathVariable Integer workspaceId){
                List<AssignmentResponse> assignments = assignmentService.getAssignmentsByWorkspace(workspaceId);
                return ResponseEntity.ok(assignments);
        }

        @GetMapping("/workspace/{workspaceId}/status/{status}")
        public ResponseEntity<List<AssignmentResponse>> getAssignmentsByWorkspaceAndStatus
                (@PathVariable Integer workspaceId,
                 @PathVariable Assignment.AssignmentStatus status){
                List<AssignmentResponse> assignments = assignmentService.getAssignmentsByWorkspaceAndStatus(workspaceId, status);
                return ResponseEntity.ok(assignments);
        }

        @GetMapping("/{id}")
        public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Integer id){
                AssignmentResponse response = assignmentService.getAssignmentById(id);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{id}")
        public ResponseEntity<AssignmentResponse> updateAssignment(@PathVariable Integer id, @Valid @RequestBody AssignmentRequest request){
                AssignmentResponse response = assignmentService.updateAssignment(id, request);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAssignment(@PathVariable Integer id) {
                assignmentService.deleteAssignment(id);
                return ResponseEntity.noContent().build();
        }
}
