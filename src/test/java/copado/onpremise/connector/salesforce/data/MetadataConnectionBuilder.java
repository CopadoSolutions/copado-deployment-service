package copado.onpremise.connector.salesforce.data;


import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetadataConnectionBuilder {


    private String username;
    private String password;
    private String securityToken;
    private String baseUrl = "https://login.salesforce.com";

    public MetadataConnectionBuilder username(String value) {
        username = value;
        return this;
    }

    public MetadataConnectionBuilder password(String value) {
        password = value;
        return this;
    }

    public MetadataConnectionBuilder securityToken(String value) {
        securityToken = value;
        return this;
    }

    public MetadataConnection build() throws ConnectionException {

        final ConnectorConfig partnerConnectionConfig = new ConnectorConfig();
        partnerConnectionConfig.setUsername(username);
        partnerConnectionConfig.setPassword(password + securityToken);
        partnerConnectionConfig.setAuthEndpoint(buildEndpoint());
        PartnerConnection partnerConnection = new PartnerConnection(partnerConnectionConfig);

        final ConnectorConfig metadataConnectionConfig = new ConnectorConfig();
        metadataConnectionConfig.setAuthEndpoint(partnerConnection.getConfig().getServiceEndpoint());
        metadataConnectionConfig.setServiceEndpoint(partnerConnection.getConfig().getServiceEndpoint().replace("/services/Soap/u/", "/services/Soap/m/"));
        metadataConnectionConfig.setSessionId(partnerConnection.getConfig().getSessionId());
        metadataConnectionConfig.setManualLogin(true);

        return new MetadataConnection(metadataConnectionConfig);
    }

    private String buildEndpoint() {
        return baseUrl + "/services/Soap/u/45.0" ;
    }

}
