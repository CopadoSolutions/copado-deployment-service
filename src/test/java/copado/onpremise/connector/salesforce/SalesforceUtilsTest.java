package copado.onpremise.connector.salesforce;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SalesforceUtilsTest {

    private SalesforceUtils salesforceUtils = new SalesforceUtils();

    @Test
    public void existProxyConfiguration_whenAllFilled() {
        boolean currentResult = salesforceUtils.existProxyConfiguration("USER", "PASSWORD","HOST");
        assertTrue(currentResult);
    }

    @Test
    public void existProxyConfiguration_whenUserIsMissing() {
        boolean currentResult = salesforceUtils.existProxyConfiguration("", "PASSWORD","HOST");
        assertFalse(currentResult);
    }


    @Test
    public void existProxyConfiguration_whenPasswordIsMissing() {
        boolean currentResult = salesforceUtils.existProxyConfiguration("USER", null,"HOST");
        assertFalse(currentResult);
    }


    @Test
    public void existProxyConfiguration_whenHostIsMissing() {
        boolean currentResult = salesforceUtils.existProxyConfiguration("USER", "PASSWORD","");
        assertFalse(currentResult);
    }
}