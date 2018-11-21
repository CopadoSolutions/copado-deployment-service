package copado.service.salesforce;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class SalesforceUtils {

    private SalesforceUtils() {
    }

    public static MetadataConnection createMetadataConnection(SalesforceUtilsInfo info, PartnerConnection pc) throws ConnectionException {
        return new MetadataConnection(createMetadataConnectorConfig(info, pc.getConfig().getSessionId()));
    }


    public static PartnerConnection createPartnerConnection(SalesforceUtilsInfo info) throws ConnectionException {
        return new PartnerConnection(createConfig(info));

    }


    private static ConnectorConfig createConfig(SalesforceUtilsInfo info) {
        final ConnectorConfig config = new ConnectorConfig();
        config.setUsername(info.getUsername());
        config.setPassword(info.getPassword() + info.getToken());
        config.setAuthEndpoint(info.getLoginUrl());
        if (existProxyConfiguration(info.getProxyUsername(), info.getProxyPassword(), info.getProxyHost())) {
            log.info("Using Proxy for SFDC connection.");
            config.setProxy(info.getProxyHost(), Integer.valueOf(info.getProxyPort()));
            config.setProxyUsername(info.getProxyUsername());
            config.setProxyPassword(info.getProxyPassword());
        }
        return config;
    }

    private static ConnectorConfig createMetadataConnectorConfig(SalesforceUtilsInfo info, String sessionId) {

        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(info.getLoginUrl());
        config.setServiceEndpoint(info.getLoginUrl().replace("/services/Soap/u/", "/services/Soap/m/"));
        config.setSessionId(sessionId);
        config.setManualLogin(true);
        if (existProxyConfiguration(info.getProxyUsername(), info.getProxyPassword(), info.getProxyHost())) {
            log.info("Using Proxy for SFDC connection.");
            config.setProxy(info.getProxyHost(), Integer.valueOf(info.getProxyPort()));
            config.setProxyUsername(info.getProxyUsername());
            config.setProxyPassword(info.getProxyPassword());
        }
        return config;
    }

    public static boolean existProxyConfiguration(String proxyUsername, String proxyPassword, String proxyHost) {

        boolean toReturn = false;
        if (StringUtils.isNotBlank(proxyUsername)
                && StringUtils.isNotBlank(proxyPassword)
                && StringUtils.isNotBlank(proxyHost)) {

            toReturn = true;
        }
        return toReturn;
    }


}
