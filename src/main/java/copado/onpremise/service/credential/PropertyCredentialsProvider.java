package copado.onpremise.service.credential;


import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.configuration.CompositeConfiguration;

@AllArgsConstructor(onConstructor = @__({ @Inject}))
class PropertyCredentialsProvider {

    private CompositeConfiguration compositeConfiguration;

    public String getCredentialField(String orgId, String prefix, String field) {
        return buildProperty(orgId, prefix, field);
    }

    private String buildProperty(String orgId, String prefix, String propertyName) {
        return compositeConfiguration.getString(String.format("%s.%s.%s", prefix, orgId, propertyName));
    }
}
