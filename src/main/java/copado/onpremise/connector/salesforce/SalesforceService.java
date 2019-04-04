package copado.onpremise.connector.salesforce;


import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.sobject.SObject;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;

import java.util.List;


public interface SalesforceService {

    /**
     * Deploy a zip file of metadata components.
     * Prerequisite: Have a deploy.zip file that includes a package.xml manifest file that
     * details the contents of the zip file.
     */
    DeploymentResult deployZip(MetadataConnection metadataConnection, String zipFileAbsolutePath, DeployRequest deployRequest, SalesforceDeployerDelegate delegate) throws CopadoException;

    List<SObject> query(PartnerConnection partnerConnection, String query) throws CopadoException;

    void updateStringField(PartnerConnection partnerConnection, String id, String objectType, String fieldName, String value) throws CopadoException;

    String createTxtAttachment(PartnerConnection partnerConnection, String parentId, String attachmentName, String attachmentContent) throws CopadoException ;


}