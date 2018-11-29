package copado.onpremise.service.credential;


import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.configuration.CompositeConfiguration;

@AllArgsConstructor(onConstructor = @__({ @Inject}))
class SalesforceCredentialServiceImpl implements SalesforceCredentialService {

    private static final String PREFIX = "copado.onpremise.deployment";

    private CompositeConfiguration compositeConfiguration;


    public SalesforceCredentials getCredentials(String orgId) {

        return SalesforceCredentialsImpl.builder()
                .url(buildProperty(orgId, "url"))
                .username(buildProperty(orgId, "username"))
                .password(buildProperty(orgId, "password"))
                .token(buildProperty(orgId, "token"))
                .build();
    }

    private String buildProperty(String orgId, String propertyName) {
        return compositeConfiguration.getString(String.format("%s.%s.%s", PREFIX, orgId, propertyName));
    }
}
