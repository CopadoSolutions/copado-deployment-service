package copado.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * <b>Description</b>
 * <p>
 * Allows authorize and authenticate user.
 * </p>
 */
@Component
@Slf4j
public class AuthenticationProviderCopado implements AuthenticationProvider {

    /**
     * <b>Description</b>
     * <p>
     * Checks credentials
     * </p>
     * <p>
     * If  is correct authenticated, sets session roles.
     * </p>
     *
     * @param authentication User authentication retrieved from the Http Login Request.
     * @return User session authentication with roles.
     * @throws BadCredentialsException <code>AuthenticationException</code> when:<br>
     *                                 (1) Wrong credentials <br/>
     */
    @Override
    public Authentication authenticate(Authentication authentication) {

        AuthenticationCopado auth = (AuthenticationCopado) authentication;

        if (auth != null) {
            if (StringUtils.isNotBlank(auth.getToken()) && isValidToken(auth.getToken())) {
                return createValidUserWithGrants(auth.getToken());
            } else {
                log.error("Authentication error for token {}", auth.getToken());
                throw new BadCredentialsException("Invalid credentials");
            }
        } else {
            log.error("Authentication error for token 'null'");
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    /**
     * <b>Description</b>
     * <p>
     * Returns true if this AuthenticationProvider supports the indicated Authentication object.
     * </p>
     *
     * @param auth authentication
     * @return true
     */
    @Override
    public boolean supports(Class<?> auth) {
        return AuthenticationCopado.class.isAssignableFrom(auth);
    }


    /**
     * Create a spring authentication
     *
     * @param token The given token
     * @return new authentication user
     */
    private Authentication createValidUserWithGrants(String token) {
        log.info("User has grants to request API.", token);
        return new UsernamePasswordAuthenticationToken(token, null, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    /**
     * Check the token with the internal generator.
     *
     * @param token
     * @return true if token and internal-generated token are the same string with <code>equals</code> method
     */
    private boolean isValidToken(String token) {
        Optional<String> generatedToken = TokenGenerator.generateToken();
        return generatedToken.isPresent() && generatedToken.get().equals(token);
    }


}