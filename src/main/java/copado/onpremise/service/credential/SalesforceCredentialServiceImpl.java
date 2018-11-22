package copado.onpremise.service.credential;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service("salesforceCredentialService")
class SalesforceCredentialServiceImpl implements SalesforceCredentialService {

    private static final String PREFIX = "copado.onpremise.deployment";

    @Autowired
    private Environment environment;

    public SalesforceCredentials getCredentials(String orgId) {

        return SalesforceCredentialsImpl.builder()
                .url(buildProperty(orgId, "url"))
                .username(buildProperty(orgId, "username"))
                .password(buildProperty(orgId, "password"))
                .token(buildProperty(orgId, "token"))
                .build();
    }

    private String buildProperty(String orgId, String propertyName) {
        return environment.getProperty(String.format("%s.%s.%s", PREFIX, orgId, propertyName));
    }
}
