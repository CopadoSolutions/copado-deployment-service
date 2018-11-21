package copado.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SystemProperties.class)
public class AuthenticationProviderCopadoTest {

    private AuthenticationProviderCopado auth = new AuthenticationProviderCopado();

    private SystemProperties systemProperties;

    @Before
    public void setUp() {
        systemProperties = Mockito.mock(SystemProperties.class);
    }


    @Test
    public void test_authenticate() {
        AuthenticationCopado authCopado = new AuthenticationCopado(TokenGenerator.generateToken().get());

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