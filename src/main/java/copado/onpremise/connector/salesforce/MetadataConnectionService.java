package copado.onpremise.connector.salesforce;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;


public interface MetadataConnectionService {

    MetadataConnection build(String orgId) throws ConnectionException;
}
