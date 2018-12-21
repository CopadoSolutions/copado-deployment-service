package copado.onpremise.service.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public interface PartnerConnectionBuilder {
    PartnerConnection createPartnerConnection(SalesforceUtilsInfo info) throws ConnectionException;
}
