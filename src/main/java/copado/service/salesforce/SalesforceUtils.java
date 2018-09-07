package copado.service.salesforce;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class SalesforceUtils {

    public static MetadataConnection createMetadataConnection(final String username, final String password, final String token, String loginUrl,
                                                              String proxyHost, String proxyPort, String proxyUsername, String proxyPassword)
            throws ConnectionException {

        return new MetadataConnection(metadata_CreateMetadataConfig(username, password, token, loginUrl, proxyHost, proxyPort, proxyUsername, proxyPassword));//, proxyHost, proxyPort, proxyUsername, proxyPassword));
    }


    public static PartnerConnection createPartnerConnection(final String username, final String password, final String token, String loginUrl,
                                                            String proxyHost, String proxyPort, String proxyUsername, String proxyPassword)
            throws ConnectionException {
        return new PartnerConnection(createConfig(username, password, token, loginUrl, proxyHost, proxyPort, proxyUsername, proxyPassword));

    }


    private static ConnectorConfig createConfig(final String username, final String password, final String token, String loginUrl,
                                                String proxyHost, String proxyPort, String proxyUsername, String proxyPassword) {
        final ConnectorConfig config = new ConnectorConfig();
        config.setUsername(username);
        config.setPassword(password + token);
        config.setAuthEndpoint(loginUrl);
        if (existProxyConfiguration(proxyUsername, proxyPassword, proxyHost)) {
            log.info("Using Proxy for SFDC connection.");
            config.setProxy(proxyHost, Integer.valueOf(proxyPort));
            config.setProxyUsername(proxyUsername);
            config.setProxyPassword(proxyPassword);
        }
        return config;
    }

    private static ConnectorConfig metadata_CreateMetadataConfig(final String username, final String password, final String token, String loginUrl,
                                                                 String proxyHost, String proxyPort, String proxyUsername, String proxyPassword) throws ConnectionException {

        LoginResult loginResult = metadata_CreateConnectorConfig(username, password, token, loginUrl, proxyHost, proxyPort, proxyUsername, proxyPassword);

        final ConnectorConfig configMetadata = new ConnectorConfig();
        configMetadata.setServiceEndpoint(loginResult.getMetadataServerUrl());
        configMetadata.setSessionId(loginResult.getSessionId());

        return configMetadata;
    }

    private static LoginResult metadata_CreateConnectorConfig(final String username, final String password, final String token, String loginUrl,
                                                                String proxyHost, String proxyPort, String proxyUsername, String proxyPassword) throws ConnectionException {

        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(loginUrl);
        config.setServiceEndpoint(loginUrl);
        config.setManualLogin(true);
        if (existProxyConfiguration(proxyUsername, proxyPassword, proxyHost)) {
            log.info("Using Proxy for SFDC connection.");
            config.setProxy(proxyHost, Integer.valueOf(proxyPort));
            config.setProxyUsername(proxyUsername);
            config.setProxyPassword(proxyPassword);
        }
        LoginResult loginResult = (new EnterpriseConnection(config)).login(username, password + token);
        return loginResult;
    }


    public static boolean existProxyConfiguration(String proxyUsername, String proxyPassword, String proxyHost) {

        if (StringUtils.isNotBlank(proxyUsername)
                && StringUtils.isNotBlank(proxyPassword)
                && StringUtils.isNotBlank(proxyHost)) {

            return true;
        }
        return false;
    }


}
