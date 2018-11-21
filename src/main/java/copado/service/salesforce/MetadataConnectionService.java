package copado.service.salesforce;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import copado.ApplicationConfiguration;
import copado.service.credential.SalesforceCredentialService;
import copado.service.credential.SalesforceCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetadataConnectionService {

    @Autowired
    private ApplicationConfiguration conf;

    @Autowired
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
