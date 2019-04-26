package copado.onpremise.service.credential;


import com.google.inject.Inject;
import lombok.AllArgsConstructor;

@AllArgsConstructor(onConstructor = @__({ @Inject}))
class SalesforceCredentialServiceImpl implements SalesforceCredentialService {

    private static final String PREFIX = "copado.onpremise.deployment";

    private PropertyCredentialsProvider propertyCredentialsProviderProvider;

    public SalesforceCredentials getCredentials(String orgId) {

        return SalesforceCredentialsImpl.builder()
                .url(propertyCredentialsProviderProvider.getCredentialField(orgId, PREFIX, "url"))
                .username(propertyCredentialsProviderProvider.getCredentialField(orgId, PREFIX, "username"))
                .password(propertyCredentialsProviderProvider.getCredentialField(orgId, PREFIX,"password"))
                .token(propertyCredentialsProviderProvider.getCredentialField(orgId, PREFIX, "token"))
                .build();
    }

}
