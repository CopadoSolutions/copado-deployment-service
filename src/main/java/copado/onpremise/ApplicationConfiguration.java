package copado.onpremise;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "copado.onpremise.deployment")
@Data
public class ApplicationConfiguration {

    private String copadoUsername;
    private String copadoUrl;
    private String copadoPassword;
    private String copadoToken;

    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    private String gitUrl;
    private String gitUsername;
    private String gitPassword;

    private String renameNamespace;


}
