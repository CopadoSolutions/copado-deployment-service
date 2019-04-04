package copado.onpremise;

import com.google.inject.Guice;
import com.google.inject.Injector;
import copado.onpremise.configuration.ConfigurationModuleMock;
import copado.onpremise.job.DeployRequest;
import copado.onpremise.job.JobModule;
import copado.onpremise.service.credential.CredentialModule;
import copado.onpremise.service.file.FileModule;
import copado.onpremise.service.git.GitModule;
import copado.onpremise.service.salesforce.SalesforceModuleAsserts;
import copado.onpremise.service.salesforce.dx.DxModule;
import copado.onpremise.service.validation.ValidationModule;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static copado.onpremise.service.FileTestFactory.bytesOf;

public class UserCasesTestFactory {

    public static Injector buildInjector() {
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

    public static byte[] deploymentZipBytesOf(String testFolder) {
        return bytesOf(dataSourceFolder(testFolder).resolve("deployment_TEST.zip"));
    }

    public static DeployRequest deployRequestOf(String testFolder) {
        File dataSourcePayLoad = dataSourceFolder(testFolder).resolve("payload.json").toFile();
        try {
            return new ObjectMapper().readValue(FileUtils.readFileToString(dataSourcePayLoad), DeployRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + dataSourcePayLoad);
        }
    }

    public static Path dataSourceFolder(String testFolder) {
        return Paths.get("src", "test", "resources", "repositories", testFolder, "dataSource");
    }
}