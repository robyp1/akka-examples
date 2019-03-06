package sample.hello;

public class ActorRuntimeException extends RuntimeException{
    public ActorRuntimeException() {
    }

    public ActorRuntimeException(String message) {
        super(message);
    }

    public ActorRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActorRuntimeException(Throwable cause) {
        super(cause);
    }

    public ActorRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
