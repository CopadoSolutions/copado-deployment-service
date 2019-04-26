package copado.onpremise.connector.salesforce.data;

import com.sforce.soap.metadata.DeployDetails;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.StatusCode;
import copado.onpremise.connector.copado.CopadoTip;
import copado.onpremise.connector.salesforce.metadata.DeploymentResult;
import copado.onpremise.exception.CopadoException;
import org.junit.Before;
import org.junit.Test;

import static copado.onpremise.connector.salesforce.TipLevel.ERROR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeploymentResultCheckerTest {

    private final DeploymentResultChecker deploymentResultChecker = new DeploymentResultChecker();
    private DeploymentResult deploymentResult;
    private DeployResult deployResult;
    private DeployDetails deployDetails;

    @Before
    public void setUp() {
        deployDetails = mock(DeployDetails.class);
        deploymentResult = new DeploymentResult();
        deployResult = new DeployResult();
        deployResult.setDetails(deployDetails);
    }

    @Test(expected = CopadoException.class)
    public void fillWithErrorInformation_whenInvalid() throws CopadoException {
        deployResult.setDetails(null);
        deploymentResultChecker.fillWithErrorInformation(deploymentResult, deployResult);
    }

    @Test
    public void fillWithErrorInformation_whenErrorMessage() throws CopadoException {
        final String currentErrorMessage = "TEST_ERROR_MESSAGE";

        final CopadoTip expectedFirstCopadoTip =
                new CopadoTip(ERROR,
                        currentErrorMessage,
                        "");

        final CopadoTip expectedSecondCopadoTip =
                new CopadoTip(ERROR,
                        "[Copado] Fatal Deployment. See on-premise file for debugging, reproduce error, and share with customer support",
                        "");
        deployResult.setErrorMessage(currentErrorMessage);
        deployResult.setErrorStatusCode(StatusCode.CUSTOM_APEX_ERROR);

        when(deployDetails.getComponentFailures()).thenReturn(new DeployMessage[]{});
        deploymentResultChecker.fillWithErrorInformation(deploymentResult, deployResult);

        assertThat(deploymentResult.getTips().size(), is(equalTo(2)));
        assertThat(deploymentResult.getTips().get(0), is(equalTo(expectedFirstCopadoTip)));
        assertThat(deploymentResult.getTips().get(1), is(equalTo(expectedSecondCopadoTip)));
    }

    @Test
    public void fillWithErrorInformation_whenComponentFailures() throws CopadoException {
        final String currentComponentType = "TEST_COMPONENT_TYPE";
        final String currentFailureFullName = "TEST_FAILURE_NAME";
        final String currentProblem = "TEST_PROBLEM";

        final DeployMessage currentDeployMessage = new DeployMessage();
        currentDeployMessage.setComponentType(currentComponentType);
        currentDeployMessage.setFullName(currentFailureFullName);
        currentDeployMessage.setProblem(currentProblem);

        final CopadoTip expectedCopadoTip =
                new CopadoTip(ERROR, String.format("[%s %s] %s", currentComponentType, currentFailureFullName, currentProblem), "");

        when(deployDetails.getComponentFailures()).thenReturn(new DeployMessage[]{currentDeployMessage});
        deploymentResultChecker.fillWithErrorInformation(deploymentResult, deployResult);

        assertThat(deploymentResult.getTips().size(), is(equalTo(1)));
        assertThat(deploymentResult.getTips().get(0), is(equalTo(expectedCopadoTip)));

    }
}