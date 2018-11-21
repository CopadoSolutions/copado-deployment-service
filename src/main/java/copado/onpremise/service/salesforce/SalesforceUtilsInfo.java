package copado.onpremise.service.salesforce;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
