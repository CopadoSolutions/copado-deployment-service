package copado.onpremise.connector.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import copado.onpremise.connector.salesforce.data.SalesforceUtilsInfo;

public interface PartnerConnectionBuilder {
    PartnerConnection createPartnerConnection(SalesforceUtilsInfo info) throws ConnectionException;
}
