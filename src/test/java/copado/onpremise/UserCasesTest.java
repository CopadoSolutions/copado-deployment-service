package copado.onpremise;

import copado.onpremise.job.OnPremiseDeploymentJob;
import org.junit.Test;

import static copado.onpremise.UserCasesTestFactory.*;
import static copado.onpremise.configuration.ConfigurationModuleMock.setUpConfig;
import static copado.onpremise.service.git.GitTestFactory.*;
import static copado.onpremise.service.salesforce.SalesforceServiceAssert.salesforceServiceLog;
import static copado.onpremise.service.salesforce.SalesforceServiceAssert.setUpSalesforce;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserCasesTest {

    @Test
    public void useCase_basicSalesforceDeployment() {
        final String testFolder = "acceptanceTest_SalesforceBasicDeployment";

        setUpSalesforce();
        setUpGitWithNewCopyOfRemote(testFolder);
        setUpConfig(currentRemoteDirectoryPath(), currentRemoteArtifactRepositoryPath());

        OnPremiseDeploymentJob job = buildInjector().getInstance(OnPremiseDeploymentJob.class);
        job.setDeployBranchName("deployment/TEST");
        job.execute();

        assertThat(salesforceServiceLog().getZipsBytes().size(), is(equalTo(1)));
        assertThat(salesforceServiceLog().getDeployRequests().size(), is(equalTo(1)));
        assertThat(salesforceServiceLog().getZipsBytes().get(0), is(equalTo(deploymentZipBytesOf(testFolder))));
        assertThat(salesforceServiceLog().getDeployRequests().get(0), is(equalTo(deployRequestOf(testFolder))));

    }
}