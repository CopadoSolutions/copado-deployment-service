package copado.service.validation;

public interface ValidationResult {

    boolean isSuccess();
    int getCode();
    int getMessage();

}
