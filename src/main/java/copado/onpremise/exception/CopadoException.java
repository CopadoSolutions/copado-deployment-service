package copado.onpremise.exception;

public class CopadoException extends Exception {

    public CopadoException(String message) {
        super(message);
    }

    public CopadoException(String message, Throwable e) {
        super(message, e);
    }
}
