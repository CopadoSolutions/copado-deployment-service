package copado.onpremise.connector.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import copado.onpremise.connector.salesforce.data.SalesforceUtilsInfo;
import copado.onpremise.exception.CopadoException;

public interface PartnerConnectionBuilder {
    PartnerConnection createPartnerConnection(SalesforceUtilsInfo info) throws CopadoException;
}
