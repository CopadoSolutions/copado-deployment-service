package copado.onpremise;

import com.google.inject.Guice;
import com.google.inject.Injector;
import copado.onpremise.configuration.ConfigurationModuleMock;
import copado.onpremise.job.JobModule;
import copado.onpremise.job.OnPremiseDeploymentJob;
import copado.onpremise.service.credential.CredentialModule;
import copado.onpremise.service.file.FileModule;
import copado.onpremise.service.git.GitModule;
import copado.onpremise.service.git.GitTestFactory;
import copado.onpremise.service.salesforce.SalesforceModuleMock;
import copado.onpremise.service.salesforce.dx.DxModule;
import copado.onpremise.service.validation.ValidationModule;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import java.nio.file.Path;

public class ApplicationTest {


    @Test
    public void useCase_basicSalesforceDeployment() throws ConfigurationException {
        GitTestFactory.setUpWithNewCopyOfRemote("acceptanceTest_SalesforceBasicDeployment");
        Path artifactRepository = GitTestFactory.currentRemoteDirectoryPath().getParent().resolve("artifact_repository.git");
        ConfigurationModuleMock.setUp(GitTestFactory.currentRemoteDirectoryPath(), artifactRepository);

        Injector injector = Guice.createInjector(new ConfigurationModuleMock(), new JobModule(), new CredentialModule(), new FileModule(), new GitModule(), new SalesforceModuleMock(), new ValidationModule(), new DxModule());
        OnPremiseDeploymentJob job = injector.getInstance(OnPremiseDeploymentJob.class);
        job.setDeployBranchName("deployment/TEST");
        job.execute();
    }
}