package copado.validator;

public interface Validator<E> {
    boolean validate(E validationInfo);
}
