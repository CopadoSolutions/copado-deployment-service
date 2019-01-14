package copado.onpremise.service.salesforce;


import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;

import java.io.IOException;


public interface SalesforceService {

    /**
     * Deploy a zip file of metadata components.
     * Prerequisite: Have a deploy.zip file that includes a package.xml manifest file that
     * details the contents of the zip file.
     */
    void deployZip(MetadataConnection metadataConnection, String zipFileAbsolutePath, DeployRequest deployRequest) throws IOException, CopadoException, ConnectionException, InterruptedException;

}