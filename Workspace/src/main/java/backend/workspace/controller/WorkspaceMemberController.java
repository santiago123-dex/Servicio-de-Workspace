package backend.workspace.controller;

import backend.workspace.dto.WorkspaceMember.WorkspaceMemberRequest;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberDetailsResponse;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberResponse;
import backend.workspace.entity.WorkspaceMember;
import backend.workspace.service.WorkspaceMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces/member")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    @PostMapping("/addMember")
    public ResponseEntity<WorkspaceMemberResponse> addMember(@RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody WorkspaceMemberRequest request) {
        WorkspaceMemberResponse response = workspaceMemberService.addMember(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<WorkspaceMemberResponse>> getMembersByWorkspace(
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) WorkspaceMember.Role role) {
        List<WorkspaceMemberResponse> response = workspaceMemberService.getMembersByWorkspace(workspaceId, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workspace/{workspaceId}/details")
    public ResponseEntity<List<WorkspaceMemberDetailsResponse>> getMembersDetailsByWorkspace(
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) WorkspaceMember.Role role) {
        List<WorkspaceMemberDetailsResponse> response = workspaceMemberService
                .getMembersDetailsByWorkspace(workspaceId, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workspace/{workspaceId}/count")
    public ResponseEntity<Map<String, Long>> countMembersByWorkspace(
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) WorkspaceMember.Role role) {
        long count = workspaceMemberService.countMembersByWorkspace(workspaceId, role);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/user")
    public ResponseEntity<List<WorkspaceMemberResponse>> getMembersByUser(
            @RequestHeader("X-User-Id") UUID userId) {
        List<WorkspaceMemberResponse> response = workspaceMemberService.getMembersByUser(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * METODO DE CAMBIAR DE ROL POR SI ACASO
     * 
     * @PatchMapping("/{memberId}/role")
     * public ResponseEntity<WorkspaceMemberResponse>
     * updateMemberRoles(@PathVariable Integer memberId, @RequestBody
     * WorkspaceMember.Role role){
     * WorkspaceMemberResponse response =
     * workspaceMemberService.updateMemberRole(memberId, role);
     * return ResponseEntity.ok(response);
     */

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Integer memberId) {
        workspaceMemberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

}
