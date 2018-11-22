package copado.onpremise.security;

import copado.onpremise.Application;
import copado.onpremise.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "copado.onpremise")
@SpringBootTest(classes = {Application.class})
public class AuthenticationProviderCopadoTest {

    @Autowired
    private AuthenticationProviderCopado auth;

    @Autowired
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