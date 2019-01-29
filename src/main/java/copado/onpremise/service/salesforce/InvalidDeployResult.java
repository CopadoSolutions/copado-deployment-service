package copado.onpremise.service.salesforce;

import copado.onpremise.exception.CopadoException;

class InvalidDeployResult extends CopadoException {

    public InvalidDeployResult(String message) {
        super(message);
    }
}
