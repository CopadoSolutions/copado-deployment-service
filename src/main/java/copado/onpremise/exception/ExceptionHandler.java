package copado.onpremise.exception;

import lombok.extern.flogger.Flogger;

import java.lang.reflect.InvocationTargetException;

@Flogger
public class ExceptionHandler {


    public static <T, E extends CopadoException> T rethrow(Class<E> clazz, String errorMessage, ExceptionHandler.Code<T> codeBlock) throws CopadoException {
        try {
            return codeBlock.execute();
        } catch (Throwable e) {
            log.atSevere().withCause(e).log("Internal error");
            return throwCopadoException(clazz, e);
        }
    }

    private static <T, E extends CopadoException> T throwCopadoException(Class<E> clazz, Throwable e) throws E {
        try {
            throw clazz.getConstructor(String.class, Throwable.class).newInstance("Internal error in git service", e);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException notHandledException) {
            throw new RuntimeException(notHandledException);
        }
    }

    @FunctionalInterface
    public interface Code<R> {
        R execute() throws Throwable; //NOSONAR
    }
}
