package xebia.lcre.exceptions;

public class UnableToRunCommand extends RuntimeException {
    public UnableToRunCommand(String message, Throwable cause) {
        super(message, cause);
    }
}
