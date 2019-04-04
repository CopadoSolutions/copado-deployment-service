package copado.onpremise;

import copado.onpremise.service.git.GitServiceException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static copado.onpremise.UserCasesTestFactory.*;
import static copado.onpremise.service.salesforce.SalesforceServiceMock.salesforceServiceLog;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UserCasesTest {

    @Test
    public void useCase_basicDeployment_checkSalesforce() {
        final String testFolder = "useCase_basicDeployment_checkSalesforce";

        setUpBasicUseCase(testFolder);
        assertThat(salesforceServiceLog().getZipsBytes().size(), is(equalTo(0)));
        assertThat(salesforceServiceLog().getDeployRequests().size(), is(equalTo(0)));

        executeJob();

        assertThat(salesforceServiceLog().getZipsBytes().size(), is(equalTo(1)));
        assertThat(salesforceServiceLog().getDeployRequests().size(), is(equalTo(1)));
        assertThat(salesforceServiceLog().getZipsBytes().get(0), is(equalTo(deploymentZipBytesOf(testFolder))));
        assertThat(salesforceServiceLog().getDeployRequests().get(0), is(equalTo(deployRequestOf(testFolder))));

    }

    @Test
    public void useCase_basicDeployment_checkGit() throws IOException, GitServiceException {
        final String testFolder = "useCase_basicDeployment_checkGit";
        setUpBasicUseCase(testFolder);

        executeJob();

        final Path clonedRemotePath = cloneRemoteEnvUatBranch();
        assertThat(readAccountXmlFromLocalGit(clonedRemotePath), containsString("<label>Active Edited</label>"));
    }

    @Test
    public void useCase_basicDeployment_withCheckOnly_checkGit() throws IOException, GitServiceException {
        final String testFolder = "useCase_basicDeployment_withCheckOnly_checkGit";
        setUpBasicUseCase(testFolder);

        executeJob();

        final Path clonedRemotePath = cloneRemoteEnvUatBranch();
        assertThat(readAccountXmlFromLocalGit(clonedRemotePath), containsString("<label>Active</label>"));
    }


    @Test
    public void useCase_basicDeployment_withTestClasses_checkGit() {
        final String testFolder = "useCase_basicDeployment_withTestClasses_checkGit";

        setUpBasicUseCase(testFolder);
        assertThat(salesforceServiceLog().getZipsBytes().size(), is(equalTo(0)));
        assertThat(salesforceServiceLog().getDeployRequests().size(), is(equalTo(0)));

        executeJob();

        assertThat(salesforceServiceLog().getZipsBytes().size(), is(equalTo(1)));
        assertThat(salesforceServiceLog().getDeployRequests().size(), is(equalTo(1)));
        assertThat(salesforceServiceLog().getZipsBytes().get(0), is(equalTo(deploymentZipBytesOf(testFolder))));
        assertThat(salesforceServiceLog().getDeployRequests().get(0), is(equalTo(deployRequestOf(testFolder))));
    }

}