package copado.service.credential;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class SalesforceCredentialsImpl implements SalesforceCredentials{
    private String username;
    private String password;
    private String url;
    private String token;
}
