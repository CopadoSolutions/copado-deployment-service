package copado.onpremise.service.salesforce;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class SalesforceUtilsInfo {

    private String username;
    private String password;
    private String token;
    private String loginUrl;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;

}
