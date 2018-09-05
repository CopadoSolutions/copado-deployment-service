package copado.service.salesforce;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceUtils {

    public static MetadataConnection createMetadataConnection(final String username, final String password, final String token, String loginUrl) throws ConnectionException {

        LoginResult loginResult = createConnectorConfig(username, password, token, loginUrl);

        final ConnectorConfig configMetadata = new ConnectorConfig();
        configMetadata.setServiceEndpoint(loginResult.getMetadataServerUrl());
        configMetadata.setSessionId(loginResult.getSessionId());
        return new MetadataConnection(configMetadata);

    }


    public static PartnerConnection createPartnerConnection(final String username, final String password, final String token, String loginUrl) throws ConnectionException {

        LoginResult loginResult = createConnectorConfig(username, password, token, loginUrl);

        final ConnectorConfig configMetadata = new ConnectorConfig();
        configMetadata.setServiceEndpoint(loginResult.getMetadataServerUrl());
        configMetadata.setSessionId(loginResult.getSessionId());
        return new PartnerConnection(configMetadata);

    }

    private static LoginResult createConnectorConfig(final String username, final String password, final String token, String loginUrl) throws ConnectionException {

        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(loginUrl);
        config.setServiceEndpoint(loginUrl);
        config.setManualLogin(true);
        LoginResult loginResult = (new EnterpriseConnection(config)).login(username, password + token);
        return loginResult;
    }
}
