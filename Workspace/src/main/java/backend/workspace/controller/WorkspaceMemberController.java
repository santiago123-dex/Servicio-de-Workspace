package backend.workspace.controller;

import backend.workspace.dto.WorkspaceMember.WorkspaceMemberRequest;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberResponse;
import backend.workspace.entity.WorkspaceMember;
import backend.workspace.service.WorkspaceMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspace-member")
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    public WorkspaceMemberController(WorkspaceMemberService workspaceMemberService) {
        this.workspaceMemberService = workspaceMemberService;
    }

   @PostMapping("/addMember")
    public ResponseEntity<WorkspaceMemberResponse> addMember(@RequestBody WorkspaceMemberRequest request){
        WorkspaceMemberResponse response = workspaceMemberService.addMember(request);
        return ResponseEntity.ok(response);
   }

   @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<WorkspaceMemberResponse>> getMembersByWorkspace(@PathVariable Integer workspaceId){
        List<WorkspaceMemberResponse> response = workspaceMemberService.getMembersByWorkspace(workspaceId);
        return ResponseEntity.ok(response);
   }

   @GetMapping("/user/{userId}")
    public ResponseEntity<List<WorkspaceMemberResponse>> getWorkspacesByUser(@PathVariable Integer userId){
        List<WorkspaceMemberResponse> response = workspaceMemberService.getWorkspacesByUser(userId);
        return ResponseEntity.ok(response);
   }

   @PatchMapping("/{memberId}/role")
    public ResponseEntity<WorkspaceMemberResponse> updateMemberRoles(@PathVariable Integer memberId, @RequestBody WorkspaceMember.Role role){
        WorkspaceMemberResponse response = workspaceMemberService.updateMemberRole(memberId, role);
        return ResponseEntity.ok(response);
   }

   @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Integer memberId){
        workspaceMemberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
   }



}
