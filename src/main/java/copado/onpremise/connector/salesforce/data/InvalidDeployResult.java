package copado.onpremise.connector.salesforce.data;

import copado.onpremise.exception.CopadoException;

class InvalidDeployResult extends CopadoException {

    public InvalidDeployResult(String message) {
        super(message);
    }
}
