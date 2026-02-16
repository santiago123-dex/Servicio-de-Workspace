package backend.workspace.exception;

public class MemberAlreadyExistException extends RuntimeException{

    public MemberAlreadyExistException(Integer userId, Integer WorkspaceId){
        super("El usuario " + userId + " ya pertenece al workspace " + WorkspaceId);
    }

    public MemberAlreadyExistException(String message){
        super(message);
    }

}
