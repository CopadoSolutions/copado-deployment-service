package copado.onpremise.service.credential;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class GitCredentialsImpl implements GitCredentials{
    private String username;
    private String password;
    private String url;
}
