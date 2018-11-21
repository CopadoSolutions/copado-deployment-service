package copado.onpremise.security;

import copado.onpremise.ApplicationConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class AuthenticationProviderCopadoTest {

    private AuthenticationProviderCopado auth = new AuthenticationProviderCopado();

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Test
    public void test_authenticate() {
        AuthenticationCopado authCopado = new AuthenticationCopado(tokenGenerator.generateToken().get());

        assertNotNull(auth.authenticate(authCopado));

        try {
            auth.authenticate(null);
            fail("Should throw a 'BadCredentialsException'");
        } catch (BadCredentialsException e) {
            assertEquals("Invalid credentials", e.getMessage());
        }
    }

    @Test
    public void supports() {
        assertTrue(auth.supports(AuthenticationCopado.class));
        assertFalse(auth.supports(Authentication.class));
    }
}