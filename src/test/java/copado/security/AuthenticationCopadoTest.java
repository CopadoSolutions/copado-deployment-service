package copado.security;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthenticationCopadoTest {

    private AuthenticationCopado auth;

    @Before
    public void beforeEachMethod() {
        auth = new AuthenticationCopado("TOKEN");
    }

    @Test
    public void getCredentials() {
        assertNull(auth.getCredentials());
    }

    @Test
    public void getPrincipal() {
        assertNull(auth.getPrincipal());
    }

    @Test
    public void getToken() {
        assertEquals(auth.getToken(), "TOKEN");
    }

    @Test
    public void setToken() {
        auth.setToken("TOKEN2");
        assertEquals("TOKEN2", auth.getToken());
    }
}