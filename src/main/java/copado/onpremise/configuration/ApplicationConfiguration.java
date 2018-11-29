package copado.onpremise.configuration;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
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
