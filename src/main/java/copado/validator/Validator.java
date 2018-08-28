package copado.validator;

public interface Validator<E> {
    public boolean validate(E validationInfo);
}
