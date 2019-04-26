package copado.onpremise.connector.salesforce.metadata;

import com.sforce.soap.metadata.MetadataConnection;
import copado.onpremise.exception.CopadoException;


public interface MetadataConnectionService {

    MetadataConnection build(String orgId) throws CopadoException;
}
