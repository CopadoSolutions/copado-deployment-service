package copado.service.validation;

public interface ValidationResult {

    boolean isSuccess();
    int getCode();
    String getMessage();

}
