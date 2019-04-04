package copado.onpremise;

import com.google.inject.Guice;
import com.google.inject.Injector;
import copado.onpremise.configuration.ConfigurationModuleMock;
import copado.onpremise.job.DeployRequest;
import copado.onpremise.job.JobModule;
import copado.onpremise.job.OnPremiseDeploymentJob;
import copado.onpremise.service.credential.CredentialModule;
import copado.onpremise.service.file.FileModule;
import copado.onpremise.service.git.GitModule;
import copado.onpremise.service.salesforce.SalesforceDataSource;
import copado.onpremise.service.salesforce.SalesforceModuleAsserts;
import copado.onpremise.service.salesforce.dx.DxModule;
import copado.onpremise.service.validation.ValidationModule;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static copado.onpremise.configuration.ConfigurationModuleMock.setUpConfig;
import static copado.onpremise.service.FileTestFactory.bytesOf;
import static copado.onpremise.service.git.GitTestFactory.*;
import static copado.onpremise.service.salesforce.SalesforceServiceAssert.setUpSalesforce;

public class ApplicationTest {

    @Test
    public void useCase_basicSalesforceDeployment() {
        final String testFolder = "acceptanceTest_SalesforceBasicDeployment";

        setUpSalesforce(
                SalesforceDataSource.builder()
                        .correctZipBytes(deploymentZipBytesOf(testFolder))
                        .deployRequest(deployRequestOf(testFolder))
                        .build()
        );

        setUpWithNewCopyOfRemote(testFolder);

        setUpConfig(currentRemoteDirectoryPath(), currentRemoteArtifactRepositoryPath());

        OnPremiseDeploymentJob job = buildInjector().getInstance(OnPremiseDeploymentJob.class);
        job.setDeployBranchName("deployment/TEST");
        job.execute();
    }

    private Injector buildInjector() {
        return Guice.createInjector(
                new ConfigurationModuleMock(),
                new JobModule(),
                new CredentialModule(),
                new FileModule(),
                new GitModule(),
                new SalesforceModuleAsserts(),
                new ValidationModule(),
                new DxModule());
    }

    private byte[] deploymentZipBytesOf(String testFolder) {
        return bytesOf(dataSourceFolder(testFolder).resolve("deployment_TEST.zip"));
    }

    private DeployRequest deployRequestOf(String testFolder) {
        File dataSourcePayLoad = dataSourceFolder(testFolder).resolve("payload.json").toFile();
        try {
            return new ObjectMapper().readValue(FileUtils.readFileToString(dataSourcePayLoad), DeployRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + dataSourcePayLoad);
        }
    }

    private Path dataSourceFolder(String testFolder) {
        return Paths.get("src", "test", "resources", "repositories", testFolder, "dataSource");
    }
}