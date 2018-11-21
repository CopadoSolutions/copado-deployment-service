package copado.onpremise.service.validation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ValidationResultImpl implements ValidationResult {
    boolean success;
    int code;
    String message;
}
