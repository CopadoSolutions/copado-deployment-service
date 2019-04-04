package copado.onpremise;

import com.google.inject.Guice;
import com.google.inject.Injector;
import copado.onpremise.configuration.ConfigurationModuleMock;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;
import copado.onpremise.job.JobModule;
import copado.onpremise.job.OnPremiseDeploymentJob;
import copado.onpremise.service.credential.CredentialModule;
import copado.onpremise.service.credential.GitCredentials;
import copado.onpremise.service.file.FileModule;
import copado.onpremise.connector.git.GitModule;
import copado.onpremise.connector.git.GitService;
import copado.onpremise.connector.git.GitSession;
import copado.onpremise.connector.salesforce.SalesforceModuleMock;
import copado.onpremise.connector.salesforce.dx.DxModule;
import copado.onpremise.service.validation.ValidationModule;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static copado.onpremise.configuration.ConfigurationModuleMock.setUpConfig;
import static copado.onpremise.connector.FileTestFactory.bytesOf;
import static copado.onpremise.connector.git.GitTestFactory.*;
import static copado.onpremise.connector.salesforce.SalesforceServiceMock.setUpSalesforce;

public class UserCasesTestFactory {

    public static Injector buildInjector() {
        return Guice.createInjector(
                new ConfigurationModuleMock(),
                new JobModule(),
                new CredentialModule(),
                new FileModule(),
                new GitModule(),
                new SalesforceModuleMock(),
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

    public static void setUpBasicUseCase(String testFolder) {
        setUpSalesforce();
        setUpGitWithNewCopyOfRemote(testFolder);
        setUpConfig(currentRemoteDirectoryPath(), currentRemoteArtifactRepositoryPath());
    }

    public static void executeJob() {
        OnPremiseDeploymentJob job = buildInjector().getInstance(OnPremiseDeploymentJob.class);
        job.setDeployBranchName("deployment/TEST");
        job.execute();
    }

    public static Path cloneRemoteEnvUatBranch() throws CopadoException {
        final Path remoteRepository = currentRemoteDirectoryPath();
        final GitCredentials gitCredentials = buildCorrectCredentials(remoteRepository.toAbsolutePath().toString());
        final GitService gitService = initGitService();
        final GitSession gitSession = gitService.cloneRepo(createTempDir("assertGitRepository"), gitCredentials);
        gitService.cloneBranchFromRepo(gitSession, "env/UAT");
        gitService.checkout(gitSession, "env/UAT");
        return gitSession.getBaseDir();
    }

    public static String readAccountXmlFromLocalGit(Path path) throws IOException {
        Path accountXml = path.resolve("objects").resolve("Account.object");
        return FileUtils.readFileToString(accountXml.toFile());
    }

}