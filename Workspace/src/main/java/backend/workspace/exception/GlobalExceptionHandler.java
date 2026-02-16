package backend.workspace.exception;


import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

// Lugar donde vamos a procesar todos los errores
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWorkspaceNotFound(WorkspaceNotFoundException ex){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);

    }

    @ExceptionHandler(InvalidWorkspaceException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidWorspace(InvalidWorkspaceException ex){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
     }

     @ExceptionHandler(MemberAlreadyExistException.class)
     public ResponseEntity<Map<String, Object>> handleMemberAlreadyExists(MemberAlreadyExistException ex){
        Map<String, Object> errorResponse = new HashMap<>();
         errorResponse.put("timestamp", LocalDateTime.now());
         errorResponse.put("message", ex.getMessage());
         errorResponse.put("status", HttpStatus.CONFLICT.value());
         errorResponse.put("error", "Conflict");

         return ResponseEntity
                 .status(HttpStatus.CONFLICT)
                 .body(errorResponse);
     }

     @ExceptionHandler(MemberNotFoundException.class)
     public ResponseEntity<Map<String, Object>> handleMemberNotFound(MemberNotFoundException ex){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

     @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericExecption(Exception ox){
        Map<String, Object> errorResponse = new HashMap<>();
         errorResponse.put("timestamp", LocalDateTime.now());
         errorResponse.put("message", "Ocurrio un errro inesperado");
         errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
         errorResponse.put("error", "Internal Server Error");

         return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);

     }


}
