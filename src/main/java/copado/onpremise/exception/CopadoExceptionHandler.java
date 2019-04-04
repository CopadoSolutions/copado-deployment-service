package copado.onpremise.exception;

import lombok.extern.flogger.Flogger;

@Flogger
public class CopadoExceptionHandler {

    public static <T, E extends CopadoException> T rethrow(String errorMessage, CopadoExceptionHandler.Code<T> codeBlock) throws CopadoException {
        try {
            return codeBlock.execute();
        } catch (Throwable e) {
            log.atSevere().withCause(e).log(errorMessage);
            throw new CopadoException(errorMessage, e);
        }
    }

    @FunctionalInterface
    public interface Code<R> {
        R execute() throws Throwable; //NOSONAR
    }
}
