package copado.service.git;

import copado.exception.CopadoException;

class GitServiceException extends CopadoException {

    GitServiceException(String message) {
        super(message);
    }

    GitServiceException(String message, Throwable e) {
        super(message, e);
    }
}
