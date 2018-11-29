package copado.onpremise.service.salesforce;

import com.google.inject.Inject;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import copado.onpremise.configuration.ApplicationConfiguration;
import copado.onpremise.service.credential.SalesforceCredentialService;
import copado.onpremise.service.credential.SalesforceCredentials;
import lombok.AllArgsConstructor;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class MetadataConnectionServiceImpl implements MetadataConnectionService {

    private ApplicationConfiguration conf;

    private SalesforceCredentialService salesforceCredentialService;

    public MetadataConnection build(String orgId) throws ConnectionException {

        SalesforceCredentials salesforceCredentials = salesforceCredentialService.getCredentials(orgId);

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

        PartnerConnection partnerConnection = SalesforceUtils.createPartnerConnection(sfLoginInfo);

        return SalesforceUtils.createMetadataConnection(sfLoginInfo, partnerConnection);
    }
}
