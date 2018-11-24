package copado.onpremise.service.validation;

import java.nio.file.Path;


public interface ValidationService {

    ValidationResult validate(Path zipToBeDeployed, Path gitProjectDirectory);

}
