package copado.onpremise;

import com.google.inject.Guice;
import com.google.inject.Injector;
import copado.onpremise.configuration.ConfigurationModuleMock;
import copado.onpremise.connector.copado.CopadoModule;
import copado.onpremise.connector.copadodx.DxModule;
import copado.onpremise.connector.file.FileModule;
import copado.onpremise.connector.git.GitModule;
import copado.onpremise.connector.git.GitService;
import copado.onpremise.connector.git.GitSession;
import copado.onpremise.connector.salesforce.SalesforceDataModuleMock;
import copado.onpremise.connector.salesforce.SalesforceMetadataModuleMock;
import copado.onpremise.connector.salesforce.SalesforceModuleMock;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;
import copado.onpremise.job.JobModule;
import copado.onpremise.job.OnPremiseDeploymentJob;
import copado.onpremise.service.credential.CredentialModule;
import copado.onpremise.service.credential.GitCredentials;
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
                new SalesforceDataModuleMock(),
                new SalesforceMetadataModuleMock(),
                new CopadoModule(),
                new ValidationModule(),
                new DxModule());
    }

    public static byte[] deploymentZipBytesOf(String testFolder) {
        return bytesOf(dataSourceFolder(testFolder).resolve("deployment_TEST.zip"));
    }

    public static DeployRequest deployRequestOf(String testFolder) {
        Path dataSourcePayLoadPath = dataSourceFolder(testFolder).resolve("payload.json");
        File dataSourcePayLoad = dataSourcePayLoadPath.toFile();
        try {
            return new ObjectMapper().readValue(FileUtils.readFileToString(dataSourcePayLoad), DeployRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + dataSourcePayLoadPath.toAbsolutePath().toString(), e);
        }
    }

    public static Path dataSourceFolder(String testFolder) {
        return Paths.get("src", "test", "resources", "repositories", testFolder, "dataSource");
    }

    public static void setUpBasicUseCase(String testFolder) {
        setUpSalesforce();
        setUpGitWithNewCopyOfRemote(testFolder);
        setUpConfig(currentRemoteDirectoryPath().toAbsolutePath().toString(), "", "");
    }

    public static void setUpBasicUseCaseWithArtifacts(String testFolder) {
        setUpSalesforce();
        setUpGitWithNewCopyOfRemote(testFolder);

        Path firstArtifactRemote = setUpGitWithFirstArtifactRemote(testFolder);
        Path secondArtifactRemote = setUpGitWithSecondArtifactRemote(testFolder);

        setUpConfig(
                currentRemoteDirectoryPath().toAbsolutePath().toString(),
                firstArtifactRemote.toAbsolutePath().toString(),
                secondArtifactRemote.toAbsolutePath().toString());
    }

    public static void executeJob() {
        OnPremiseDeploymentJob job = buildInjector().getInstance(OnPremiseDeploymentJob.class);
        job.setDeployBranchName("deployment/TEST");
        job.execute();
    }

    public static Path cloneRemoteEnvUatBranch() throws CopadoException {
        return cloneRemoteBranch("env/UAT", currentRemoteDirectoryPath());
    }

    public static Path cloneMasterFromFirstAritactRepository() throws CopadoException {
        return cloneRemoteBranch("master", dataSource().getCurrentFirstArtifactRemote());
    }

    public static Path cloneMasterFromSecondAritactRepository() throws CopadoException {
        return cloneRemoteBranch("master", dataSource().getCurrentSecondArtifactRemote());
    }

    public static Path cloneRemoteBranch(String branchName, Path remoteRepository) throws CopadoException {
        final GitCredentials gitCredentials = buildCorrectCredentials(remoteRepository.toAbsolutePath().toString());
        final GitService gitService = initGitService();
        final GitSession gitSession = gitService.cloneRepo(createTempDir("assertGitRepository"), gitCredentials);
        gitService.cloneBranchFromRepo(gitSession, branchName);
        gitService.checkout(gitSession, branchName);
        return gitSession.getBaseDir();
    }

    public static String readAccountXmlFromLocalGit(Path path) throws IOException {
        Path accountXml = path.resolve("objects").resolve("Account.object");
        return FileUtils.readFileToString(accountXml.toFile());
    }

    public static String readActiveFieldFromAccountXmlInGit(Path path) throws IOException {
        Path accountXml = path
                .resolve("MainArtifact")
                .resolve("main")
                .resolve("default")
                .resolve("objects")
                .resolve("Account")
                .resolve("fields")
                .resolve("Active__c.field-meta.xml");

        return FileUtils.readFileToString(accountXml.toFile());
    }

}