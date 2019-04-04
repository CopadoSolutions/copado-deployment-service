package copado.onpremise.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public class ConfigurationModule extends AbstractModule {

    private static final String PROPERTY_PREFIX = "copado.onpremise.deployment.";

    private CompositeConfiguration config = new CompositeConfiguration();
    private final ApplicationConfiguration appConfig;

    public ConfigurationModule() throws ConfigurationException {
        config.addConfiguration(new SystemConfiguration());
        config.addConfiguration(new PropertiesConfiguration("application.properties"));
        appConfig = setUpAppConfig();
    }

    ConfigurationModule(CompositeConfiguration config) {
        this.config = config;
        appConfig = setUpAppConfig();
    }

    @Provides
    CompositeConfiguration compositeConfiguration() {
        return config;
    }

    @Provides
    ApplicationConfiguration applicationConfiguration() {
        return appConfig;
    }

    private ApplicationConfiguration setUpAppConfig() {
        return ApplicationConfiguration.builder()
                .copadoUsername(getStringConfig("copadoUsername"))
                .copadoUrl(getStringConfig("copadoUrl"))
                .copadoPassword(getStringConfig("copadoPassword"))
                .copadoToken(getStringConfig("copadoToken"))
                .proxyHost(getStringConfig("proxyHost"))
                .proxyPort(getStringConfig("proxyPort"))
                .proxyUsername(getStringConfig("proxyUsername"))
                .proxyPassword(getStringConfig("proxyPassword"))
                .gitUrl(getStringConfig("gitUrl"))
                .gitUsername(getStringConfig("gitUsername"))
                .gitPassword(getStringConfig("gitPassword"))
                .renameNamespace(getStringConfig("renameNamespace"))
                .build();
    }

    String getStringConfig(String configName) {
        return config.getString(PROPERTY_PREFIX + configName);
    }

}
