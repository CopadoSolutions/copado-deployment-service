package copado.onpremise.service.validation;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service("validationService")
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
