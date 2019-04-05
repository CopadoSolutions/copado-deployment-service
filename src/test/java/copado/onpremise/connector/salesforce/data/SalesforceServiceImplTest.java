package copado.onpremise.connector.salesforce.data;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.TestLevel;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import copado.onpremise.connector.salesforce.metadata.DeploymentResult;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Flogger
public class SalesforceServiceImplTest {

    @Test
    public void integration_deployCorrectZip() throws ConnectionException, CopadoException {

        final String destinationOrgUsername = System.getProperty("destinationOrgUsername");
        final String destinationOrgPassword = System.getProperty("destinationOrgPassword");
        final String destinationOrgToken = System.getProperty("destinationOrgToken");

        if (StringUtils.isBlank(destinationOrgUsername) || StringUtils.isBlank(destinationOrgPassword) || StringUtils.isBlank(destinationOrgToken)) {
            throw new RuntimeException("Please, set up your test environment properties before run this test");
        }

        final String zipToBeDeployedPath = Paths.get("src", "test", "resources", "connector", "salesforce", "deployment_TEST.zip").toAbsolutePath().toString();
        final MetadataConnectionBuilder metadataConnectionBuilder = new MetadataConnectionBuilder();
        final SalesforceServiceImpl service = new SalesforceServiceImpl(DeploymentResultChecker::new);

        final MetadataConnection metadataConnection = metadataConnectionBuilder
                .username(destinationOrgUsername)
                .password(destinationOrgPassword)
                .securityToken(destinationOrgToken)
                .build();

        final SalesforceDeployerDelegate deployerDelegate = (asyncId) -> log.atInfo().log("New event notify-async-id with id: %s", asyncId);
        final DeployRequest deployRequest = DeployRequest.builder()
                .testLevel(TestLevel.NoTestRun.toString())
                .isCheckOnly(true)
                .build();

        DeploymentResult currentResult = service.deployZip(metadataConnection, zipToBeDeployedPath, deployRequest, deployerDelegate);

        assertTrue(currentResult.isSuccess());
        assertThat(currentResult.getTips().size(), is(equalTo(0)));
        assertNotNull(currentResult.getAsyncId());

    }

    @Test
    public void integration_query_whenCorrect() throws ConnectionException, CopadoException {
        final String currentQuery = "TEST_QUERY";
        final String currentQueryLocator = "1";
        final PartnerConnection partnerConnection = mock(PartnerConnection.class);
        final SalesforceServiceImpl service = new SalesforceServiceImpl(DeploymentResultChecker::new);
        QueryResult currentQueryResult = mock(QueryResult.class);
        SObject expectedFirstSObject = mock(SObject.class);
        SObject expectedSecondSObject = mock(SObject.class);

        when(partnerConnection.query(currentQuery)).thenReturn(currentQueryResult);
        when(currentQueryResult.getSize()).thenReturn(2);
        when(currentQueryResult.getRecords()).thenReturn(new SObject[]{expectedFirstSObject}).thenReturn(new SObject[]{expectedSecondSObject});
        when(currentQueryResult.isDone()).thenReturn(false).thenReturn(true);
        when(currentQueryResult.getQueryLocator()).thenReturn(currentQueryLocator);
        when(partnerConnection.queryMore(currentQueryLocator)).thenReturn(currentQueryResult);

        List<SObject> currentResult = service.query(partnerConnection, currentQuery);

        assertThat(currentResult.size(), is(equalTo(2)));
        assertThat(currentResult.get(0), is(equalTo(expectedFirstSObject)));
        assertThat(currentResult.get(1), is(equalTo(expectedSecondSObject)));


    }

}