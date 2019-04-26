package copado.onpremise.connector.salesforce;

import com.sforce.ws.ConnectorConfig;
import copado.onpremise.connector.salesforce.data.SalesforceUtilsInfo;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PartnerConnectionBuilderImplTest {


    @Test
    public void createConfigWithCorrectInfo() {
        final String correctUser = "USER";
        final String correctPassword = "PASSWORD";
        final String correctToken = "TOKEN";
        final String correctUrl = "http://URL.com";
        final PartnerConnectionBuilderImpl builder = new PartnerConnectionBuilderImpl(new SalesforceUtils());


        final SalesforceUtilsInfo info = SalesforceUtilsInfo.builder()
                .username(correctUser)
                .password(correctPassword)
                .token(correctToken)
                .loginUrl(correctUrl)
                .build();


        ConnectorConfig currentConfig = builder.createConfig(info);

        assertThat(currentConfig.getUsername(), is(equalTo(correctUser)));
        assertThat(currentConfig.getPassword(), is(equalTo(correctPassword + correctToken)));
        assertThat(currentConfig.getAuthEndpoint(), is(equalTo(correctUrl)));
    }

}