package copado.onpremise.service.git;

import copado.onpremise.exception.CopadoException;

public class GitServiceException extends CopadoException {

    GitServiceException(String message) {
        super(message);
    }

    GitServiceException(String message, Throwable e) {
        super(message, e);
    }
}
