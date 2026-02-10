package backend.workspace.exception;

public class Exceptions  extends RuntimeException{

    public Exceptions(Integer id){
        super("Workspace no encontrado con id" + id);
    }

}
