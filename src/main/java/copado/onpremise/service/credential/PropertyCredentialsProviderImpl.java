package copado.onpremise.service.credential;


import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.configuration.CompositeConfiguration;

@AllArgsConstructor(onConstructor = @__({ @Inject}))
class PropertyCredentialsProviderImpl implements PropertyCredentialsProvider{

    private CompositeConfiguration compositeConfiguration;

    @Override
    public String getCredentialField(String orgId, String prefix, String field) {
        return buildProperty(orgId, prefix, field);
    }

    @Override
    public String buildProperty(String orgId, String prefix, String propertyName) {
        return compositeConfiguration.getString(String.format("%s.%s.%s", prefix, orgId, propertyName));
    }
}
