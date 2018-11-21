package copado.onpremise.service.validation;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public interface ValidationService {

    ValidationResult validate(Path zipToBeDeployed, Path gitProjectDirectory);

}
