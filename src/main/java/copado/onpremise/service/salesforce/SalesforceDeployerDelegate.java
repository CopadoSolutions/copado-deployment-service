package copado.onpremise.service.salesforce;

import copado.onpremise.exception.CopadoException;

public interface SalesforceDeployerDelegate {

    void notifyAsyncId(String asyncId) throws CopadoException;

}
