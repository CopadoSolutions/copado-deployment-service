package copado.onpremise.service.salesforce;

import com.google.common.flogger.FluentLogger;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.apache.commons.lang.StringUtils;


class SalesforceUtils {

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private SalesforceUtils() {
    }

    public static MetadataConnection createMetadataConnection(SalesforceUtilsInfo info, PartnerConnection pc) throws ConnectionException {
        return new MetadataConnection(createMetadataConnectorConfig(info, pc.getConfig().getSessionId(), pc.getConfig().getServiceEndpoint()));
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
            log.atInfo().log("Using Proxy for SFDC connection.");
            config.setProxy(info.getProxyHost(), Integer.valueOf(info.getProxyPort()));
            config.setProxyUsername(info.getProxyUsername());
            config.setProxyPassword(info.getProxyPassword());
        }
        return config;
    }

    private static ConnectorConfig createMetadataConnectorConfig(SalesforceUtilsInfo info, String sessionId, String serviceEndpint) {

        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(serviceEndpint);
        config.setServiceEndpoint(serviceEndpint.replace("/services/Soap/u/", "/services/Soap/m/"));
        config.setSessionId(sessionId);
        config.setManualLogin(true);
        if (existProxyConfiguration(info.getProxyUsername(), info.getProxyPassword(), info.getProxyHost())) {
            log.atInfo().log("Using Proxy for SFDC connection.");
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
