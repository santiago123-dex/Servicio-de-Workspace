package backend.workspace.controller;

import backend.workspace.dto.Workspace.WorkspaceRequest;
import backend.workspace.dto.Workspace.WorkspaceResponse;
import backend.workspace.service.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping("/test-string")
    public ResponseEntity<String> testString(@RequestBody String jsonString) {
        try {
            log.info("String recibido: {}", jsonString);
            log.info("Length: {}", jsonString.length());
            return ResponseEntity.ok("String recibido: " + jsonString);
        } catch (Exception e) {
            log.error("Error al procesar string", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/test-json")
    public ResponseEntity<String> testJson(@RequestBody JsonNode jsonNode) {
        try {
            log.info("JSON recibido: {}", jsonNode.toString());
            return ResponseEntity.ok("JSON recibido correctamente: " + jsonNode.toString());
        } catch (Exception e) {
            log.error("Error al procesar JSON", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<WorkspaceResponse> createWorkspace(@RequestBody WorkspaceRequest workspaceRequest){
            WorkspaceResponse response = workspaceService.createWorkspace(workspaceRequest);
            return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllWorkspaces")
    //Lista de objetos de workspade
    public ResponseEntity<List<WorkspaceResponse>> getAllWorkspaces(){
        List<WorkspaceResponse> response = workspaceService.getAllWorkspaces();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getWorkspaceById/{id}")
    public ResponseEntity<WorkspaceResponse> getWorkspaceById(@PathVariable Integer id){
        return ResponseEntity.ok(workspaceService.getWorkspaceById(id));
    }

    @DeleteMapping("/deleteWorkspace/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Integer id ){
        workspaceService.deleteWorkspace(id);
        //crear una respuesta sin el body
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/updateWorkspace/{id}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(@PathVariable Integer id, @RequestBody WorkspaceRequest request){
        return ResponseEntity.ok(workspaceService.updateWorkspace(id, request));
    }




}
