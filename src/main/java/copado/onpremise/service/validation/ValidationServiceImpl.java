package copado.onpremise.service.validation;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor(onConstructor = @__({ @Inject}))
class ValidationServiceImpl implements ValidationService {
    @Override
    public ValidationResult validate(Path zipToBeDeployed, Path gitProjectDirectory) {
        return ValidationResultImpl.builder()
                .code(0)
                .message("Success")
                .success(true)
                .build();
    }
}
