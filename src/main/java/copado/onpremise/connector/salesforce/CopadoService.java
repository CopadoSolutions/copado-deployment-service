package copado.onpremise.connector.salesforce;


import copado.onpremise.exception.CopadoException;

public interface CopadoService {

    void updateDeploymentJobStatus(String deploymentJobId, String status);

    void updateDeploymentJobValidationId(String deploymentJobId, String validationId) throws CopadoException;

    void updateDeploymentJobAsyncId(String deploymentJobId, String asyncId) throws CopadoException;

    String getSourceOrgId(String deploymentJobId) throws CopadoException;

    String getDeploymentId(String deploymentJobId) throws CopadoException;

    String createTxtAttachment(String parentId, String attachmentName, String attachmentContent) throws CopadoException;


}
