package copado.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Arrays;

/**
 * Defines base authentication object for spring context
 */
@EqualsAndHashCode
public class AuthenticationCopado extends AbstractAuthenticationToken {

    @Getter
    @Setter
    private String token;

    public AuthenticationCopado(String token) {
        super(Arrays.asList());
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}