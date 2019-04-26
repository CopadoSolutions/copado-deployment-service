package copado.onpremise.connector.copado;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.sobject.SObject;
import copado.onpremise.configuration.ApplicationConfiguration;
import copado.onpremise.connector.salesforce.PartnerConnectionBuilder;
import copado.onpremise.connector.salesforce.data.SalesforceService;
import copado.onpremise.exception.CopadoException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CopadoServiceImplTest {

    private CopadoServiceImpl copadoService;
    private ApplicationConfiguration appConf;
    private PartnerConnectionBuilder partnerConnectionBuilder;
    private SalesforceService salesforceService;
    private PartnerConnection partnerConnection;

    @Before
    public void setUp() throws CopadoException {
        appConf = mock(ApplicationConfiguration.class);
        partnerConnectionBuilder = mock(PartnerConnectionBuilder.class);
        salesforceService = mock(SalesforceService.class);
        partnerConnection = mock(PartnerConnection.class);

        when(partnerConnectionBuilder.createPartnerConnection(any())).thenReturn(partnerConnection);

        copadoService = new CopadoServiceImpl(appConf, partnerConnectionBuilder, salesforceService);
    }


    @Test
    public void getDeploymentId_withCorrectOne() throws CopadoException {
        final String expectedDeploymentId = "EXPECTED_DEPLOYMENT_ID";
        final String correctDeploymentId = "TEST_DEPLOYMENT_ID";
        final String correctDeploymentIdQuery = String.format("SELECT copado__Step__r.copado__Deployment__r.Id FROM copado__Deployment_Job__c WHERE Id = '%s'", correctDeploymentId);
        final SObject currentFirstResult = mock(SObject.class);
        final List<SObject> currentResult = singletonList(currentFirstResult);

        when(salesforceService.query(partnerConnection, correctDeploymentIdQuery))
                .thenReturn(currentResult);
        when(currentFirstResult.getChild(any()))
                .thenReturn(currentFirstResult)
                .thenReturn(currentFirstResult);
        when(currentFirstResult.getField("Id"))
                .thenReturn(expectedDeploymentId);
        String currentDeploymentId = copadoService.getDeploymentId(correctDeploymentId);

        assertThat(currentDeploymentId, is(equalTo(expectedDeploymentId)));
    }

    @Test(expected = CopadoException.class)
    public void getDeploymentId_withEmptyList() throws CopadoException {
        final String correctDeploymentId = "TEST_DEPLOYMENT_ID";
        final String correctDeploymentIdQuery = String.format("SELECT copado__Step__r.copado__Deployment__r.Id FROM copado__Deployment_Job__c WHERE Id = '%s'", correctDeploymentId);
        final List<SObject> currentResult = emptyList();

        when(salesforceService.query(partnerConnection, correctDeploymentIdQuery))
                .thenReturn(currentResult);

        copadoService.getDeploymentId(correctDeploymentId);
    }

}