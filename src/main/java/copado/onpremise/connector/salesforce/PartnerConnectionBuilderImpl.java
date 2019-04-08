package copado.onpremise.connector.salesforce;

import com.google.inject.Inject;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import copado.onpremise.connector.salesforce.data.SalesforceUtilsInfo;
import copado.onpremise.exception.CopadoException;
import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;

@Flogger
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class PartnerConnectionBuilderImpl implements PartnerConnectionBuilder {

    private SalesforceUtils salesforceUtils;

    @Override
    public PartnerConnection createPartnerConnection(SalesforceUtilsInfo info) throws CopadoException {
        try {
            return new PartnerConnection(createConfig(info));
        } catch (ConnectionException e) {
            String errorMessage = "Could not create partner connection";
            log.atSevere().withCause(e).log(errorMessage);
            throw new CopadoException(errorMessage, e);
        }

    }

    ConnectorConfig createConfig(SalesforceUtilsInfo info) {
        final ConnectorConfig config = new ConnectorConfig();
        config.setUsername(info.getUsername());
        config.setPassword(info.getPassword() + info.getToken());
        config.setAuthEndpoint(info.getLoginUrl());
        if (salesforceUtils.existProxyConfiguration(info.getProxyUsername(), info.getProxyPassword(), info.getProxyHost())) {
            log.atInfo().log("Using Proxy for SFDC connection.");
            config.setProxy(info.getProxyHost(), Integer.valueOf(info.getProxyPort()));
            config.setProxyUsername(info.getProxyUsername());
            config.setProxyPassword(info.getProxyPassword());
        }
        return config;
    }
}
