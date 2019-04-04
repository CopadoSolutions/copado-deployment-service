package copado.onpremise.connector.salesforce;

import copado.onpremise.exception.CopadoException;

public interface SalesforceDeployerDelegate {

    void notifyAsyncId(String asyncId) throws CopadoException;

}
