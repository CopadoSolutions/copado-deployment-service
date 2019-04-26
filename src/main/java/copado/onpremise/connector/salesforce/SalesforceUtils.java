package copado.onpremise.connector.salesforce;

import lombok.extern.flogger.Flogger;
import org.apache.commons.lang.StringUtils;

@Flogger
public class SalesforceUtils {

    public boolean existProxyConfiguration(String proxyUsername, String proxyPassword, String proxyHost) {

        boolean toReturn = false;
        if (StringUtils.isNotBlank(proxyUsername)
                && StringUtils.isNotBlank(proxyPassword)
                && StringUtils.isNotBlank(proxyHost)) {

            toReturn = true;
        }
        return toReturn;
    }
    
}
