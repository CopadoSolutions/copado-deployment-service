package copado.onpremise.connector.salesforce.metadata;

import com.google.inject.Inject;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import copado.onpremise.configuration.ApplicationConfiguration;
import copado.onpremise.connector.salesforce.PartnerConnectionBuilder;
import copado.onpremise.connector.salesforce.SalesforceUtils;
import copado.onpremise.connector.salesforce.data.SalesforceUtilsInfo;
import copado.onpremise.service.credential.SalesforceCredentialService;
import copado.onpremise.service.credential.SalesforceCredentials;
import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang.StringUtils;

@Flogger
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class MetadataConnectionServiceImpl implements MetadataConnectionService {

    private ApplicationConfiguration conf;

    private SalesforceCredentialService salesforceCredentialService;

    private PartnerConnectionBuilder partnerConnectionBuilder;

    private SalesforceUtils salesforceUtils;

    public MetadataConnection build(String orgId) throws ConnectionException {

        SalesforceCredentials salesforceCredentials = salesforceCredentialService.getCredentials(orgId);

        if(StringUtils.isEmpty(salesforceCredentials.getUrl()) || StringUtils.isEmpty(salesforceCredentials.getUsername())){
            throw new RuntimeException("Salesforce credentials not found for id: " + orgId);
        }

        SalesforceUtilsInfo sfLoginInfo = SalesforceUtilsInfo.builder()
                .username(salesforceCredentials.getUsername())
                .password(salesforceCredentials.getPassword())
                .token(salesforceCredentials.getToken())
                .loginUrl(salesforceCredentials.getUrl())
                .proxyHost(conf.getProxyHost())
                .proxyPort(conf.getProxyPort())
                .proxyUsername(conf.getProxyUsername())
                .proxyPassword(conf.getProxyPassword())
                .build();

        PartnerConnection partnerConnection = partnerConnectionBuilder.createPartnerConnection(sfLoginInfo);

        return createMetadataConnection(sfLoginInfo, partnerConnection);
    }

    private MetadataConnection createMetadataConnection(SalesforceUtilsInfo info, PartnerConnection pc) throws ConnectionException {
        return new MetadataConnection(createMetadataConnectorConfig(info, pc.getConfig().getSessionId(), pc.getConfig().getServiceEndpoint()));
    }

    private ConnectorConfig createMetadataConnectorConfig(SalesforceUtilsInfo info, String sessionId, String serviceEndpint) {

        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(serviceEndpint);
        config.setServiceEndpoint(serviceEndpint.replace("/services/Soap/u/", "/services/Soap/m/"));
        config.setSessionId(sessionId);
        config.setManualLogin(true);
        if (salesforceUtils.existProxyConfiguration(info.getProxyUsername(), info.getProxyPassword(), info.getProxyHost())) {
            log.atInfo().log("Using Proxy for SFDC connection.");
            config.setProxy(info.getProxyHost(), Integer.valueOf(info.getProxyPort()));
            config.setProxyUsername(info.getProxyUsername());
            config.setProxyPassword(info.getProxyPassword());
        }
        return config;
    }
}
