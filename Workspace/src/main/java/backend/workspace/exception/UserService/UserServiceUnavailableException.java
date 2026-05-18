package backend.workspace.exception.UserService;

public class UserServiceUnavailableException extends RuntimeException {

    public UserServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
