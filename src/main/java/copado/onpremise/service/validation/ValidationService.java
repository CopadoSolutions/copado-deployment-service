package copado.onpremise.service.validation;

import java.nio.file.Path;

/**
 * Service that will allow or deny the deployment
 */
public interface ValidationService {

    /**
     * Validate the .zip given in the <code>zipToBeDeployed</code> path, against the git project under the given  <code>gitProjectDirectory</code> directory
     * @param zipToBeDeployed
     * @param gitProjectDirectory
     * @return
     */
    ValidationResult validate(Path zipToBeDeployed, Path gitProjectDirectory);

}
