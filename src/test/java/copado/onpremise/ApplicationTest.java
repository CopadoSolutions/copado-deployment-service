package copado.onpremise;

import com.google.inject.Guice;
import com.google.inject.Injector;
import copado.onpremise.configuration.ConfigurationModule;
import copado.onpremise.job.JobModule;
import copado.onpremise.job.OnPremiseDeploymentJob;
import copado.onpremise.service.credential.CredentialModule;
import copado.onpremise.service.file.FileModule;
import copado.onpremise.service.git.GitModule;
import copado.onpremise.service.salesforce.CopadoService;
import copado.onpremise.service.salesforce.dx.DxModule;
import copado.onpremise.service.validation.ValidationModule;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ApplicationTest {




    @Ignore
    @Test
    public void useCase_basicSalesforceDeployment() throws ConfigurationException {
        CopadoService copadoService = mock(CopadoService.class);
        Application.main(new String[]{"-deployBranchName","deployment/TEST"});

        Injector injector = Guice.createInjector(new ConfigurationModule(), new JobModule(), new CredentialModule(), new FileModule(), new GitModule(), new SalesforceModuleMock(), new ValidationModule(), new DxModule());
        OnPremiseDeploymentJob job = injector.getInstance(OnPremiseDeploymentJob.class);
        job.setDeployBranchName("deployment/TEST");
       //TODO:  job.execute();
    }
}