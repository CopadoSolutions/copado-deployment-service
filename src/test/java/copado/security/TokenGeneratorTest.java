package copado.security;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TokenGeneratorTest {

    @Test
    public void generateToken() {
        assertNotNull(TokenGenerator.generateToken());
    }
}