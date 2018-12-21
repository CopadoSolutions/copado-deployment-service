package copado.onpremise.service.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import lombok.extern.flogger.Flogger;

@Flogger
public class PartnerConnectionBuilderImpl implements PartnerConnectionBuilder {

    @Override
    public PartnerConnection createPartnerConnection(SalesforceUtilsInfo info) throws ConnectionException {
        return new PartnerConnection(createConfig(info));

    }

    private ConnectorConfig createConfig(SalesforceUtilsInfo info) {
        final ConnectorConfig config = new ConnectorConfig();
        config.setUsername(info.getUsername());
        config.setPassword(info.getPassword() + info.getToken());
        config.setAuthEndpoint(info.getLoginUrl());
        if (SalesforceUtils.existProxyConfiguration(info.getProxyUsername(), info.getProxyPassword(), info.getProxyHost())) {
            log.atInfo().log("Using Proxy for SFDC connection.");
            config.setProxy(info.getProxyHost(), Integer.valueOf(info.getProxyPort()));
            config.setProxyUsername(info.getProxyUsername());
            config.setProxyPassword(info.getProxyPassword());
        }
        return config;
    }
}
