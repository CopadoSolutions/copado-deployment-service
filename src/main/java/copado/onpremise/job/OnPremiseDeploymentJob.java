package copado.onpremise.job;

import com.google.inject.Inject;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;
import com.sforce.ws.ConnectionException;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.service.file.PathService;
import copado.onpremise.service.git.Branch;
import copado.onpremise.service.git.GitService;
import copado.onpremise.service.git.GitSession;
import copado.onpremise.service.salesforce.CopadoService;
import copado.onpremise.service.salesforce.MetadataConnectionService;
import copado.onpremise.service.salesforce.SalesforceService;
import copado.onpremise.service.validation.ValidationResult;
import copado.onpremise.service.validation.ValidationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Flogger
public class OnPremiseDeploymentJob implements Job {

    private static final String TEMP_GIT = "git";
    private static final String TEMP_DEPLOY = "deploy";
    private static final String GIT_DEPLOY_DIR_IN_BRANCH = "deployment";
    private static final String ZIP_EXT = "zip";
    private static final String MASTER = "master";
    private static final String PAYLOAD_JSON = "payload.json";

    @NonNull
    private ValidationService validationService;

    @NonNull
    private SalesforceService salesforceService;

    @NonNull
    private CopadoService copadoService;

    @NonNull
    private GitService gitService;

    @NonNull
    private PathService pathService;

    @NonNull
    private MetadataConnectionService metadataConnectionService;

    @Setter
    private String deployBranchName;

    public void execute() {

        log.atInfo().log("Starting job. Deploy branch name: %s", deployBranchName);

        Path gitTMP = null;
        Path deployZipFileTMPDir = null;
        String deploymentJobId = null;

        try {

            gitTMP = createTemporalGitPath();
            deployZipFileTMPDir = createTemporalDeployZipPath();

            GitSession git = gitService.cloneRepo(gitTMP);
            DeployRequest request = downloadAllBranchesAndReadDeploymentRequest(git);
            deploymentJobId = request.getDeploymentJobId();
            Path deployZipFileTMP = copyDeployZipToTemporalDir(gitTMP, deployZipFileTMPDir, git);
            log.atInfo().log("Deploy zip file tmp: %s", deployZipFileTMP);
            validatePromoteBranch(request, gitTMP, git, deployZipFileTMP);

            deployZip(request, deployZipFileTMP);

            mergeAndPushDeployment(request, git);

            copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Success");


        } catch (CopadoException e) {
            log.atSevere().withCause(e).log("On premise deployment failed");
            copadoService.updateDeploymentJobStatus(deploymentJobId, buildErrorStatus(e));
        } catch (UnexpectedErrorFault e) {
            log.atSevere().withCause(e).log("On premise deployment failed");
            copadoService.updateDeploymentJobStatus(deploymentJobId, buildErrorStatus(e));
        } catch (Exception e) {
            log.atSevere().withCause(e).log("On premise deployment failed");
            copadoService.updateDeploymentJobStatus(deploymentJobId, buildErrorStatus(e));
        } finally {
            pathService.safeDelete(gitTMP);
            pathService.safeDelete(deployZipFileTMPDir);
        }
    }

    private void mergeAndPushDeployment(DeployRequest request, GitSession git) throws CopadoException {

        // Commit changes on git
        Branch promoteBranchRef = gitService.getBranch(git, request.getPromoteBranch());
        gitService.mergeWithBranch(git, promoteBranchRef, request.getTargetBranch());
        gitService.push(git);
    }

    private void deployZip(DeployRequest request, Path deployZipFileTMP) throws ConnectionException, IOException, CopadoException, InterruptedException {
        MetadataConnection destinationOrgMetadata = metadataConnectionService.build(request.getOrgDestId());
        salesforceService.deployZip(destinationOrgMetadata, deployZipFileTMP.toAbsolutePath().toString());

        copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Salesforce deployment step success");
    }

    private Path createTemporalDeployZipPath() throws IOException {
        return Files.createTempDirectory(TEMP_DEPLOY);
    }

    private Path copyDeployZipToTemporalDir(Path gitTMP, Path deployZipFileTMPDir, GitSession git) throws IOException, CopadoException {
        Path deployZipFileTMP = deployZipFileTMPDir.resolve("deploy.zip");
        copyDeployZipToTemporalDir(git, deployBranchName, gitTMP, deployZipFileTMP);
        return deployZipFileTMP;
    }

    private void validatePromoteBranch(DeployRequest request, Path gitTMP, GitSession git, Path deployZipFileTMP) throws CopadoException {
        gitService.checkout(git, request.getPromoteBranch());
        copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Validating deployment zip with promotion branch.");

        ValidationResult validationResult = validationService.validate(deployZipFileTMP, gitTMP);
        log.atInfo().log("Finished validation. Success: %s, Code: %s, Message: %s", validationResult.isSuccess(), validationResult.getCode(), validationResult.getMessage());

        if (!validationResult.isSuccess()) {
            throw new CopadoException("Invalid deployment zip. Code: " + validationResult.getCode() + ". Message: " + validationResult.getMessage());
        }
    }

    private DeployRequest downloadAllBranchesAndReadDeploymentRequest(GitSession git) throws CopadoException, IOException {
        gitService.cloneBranchFromRepo(git, deployBranchName);
        gitService.checkout(git, deployBranchName);
        DeployRequest request = new ObjectMapper().readValue(git.getBaseDir().resolve(GIT_DEPLOY_DIR_IN_BRANCH).resolve(PAYLOAD_JSON).toFile(), DeployRequest.class);
        gitService.checkout(git, MASTER);

        gitService.cloneBranchFromRepo(git, request.getPromoteBranch());
        gitService.cloneBranchFromRepo(git, request.getTargetBranch());
        return request;
    }

    private Path createTemporalGitPath() throws IOException {
        log.atInfo().log("Creating git temporal dir.");
        Path gitTMP = Files.createTempDirectory(TEMP_GIT);
        log.atInfo().log("Created temporal dir:'%s'", gitTMP);
        return gitTMP;
    }

    private String buildErrorStatus(Throwable e) {
        return "On premise deployment failed:" + e.getMessage();
    }


    private void copyDeployZipToTemporalDir(GitSession gitSession, String deploymentBranch, Path deployBranchPath, Path deployZipDest) throws IOException, CopadoException {
        gitService.checkout(gitSession, deploymentBranch);
        Path deployZip = findDeployZipPath(deployBranchPath);
        FileUtils.copyFile(new File(deployZip.toAbsolutePath().toString()), new File(deployZipDest.toAbsolutePath().toString()));
    }


    private static Path findDeployZipPath(Path deployBranchPath) throws CopadoException, IOException {
        List<Path> zipFiles;
        try (Stream<Path> s = Files.walk(deployBranchPath.resolve(GIT_DEPLOY_DIR_IN_BRANCH))) {
            zipFiles = s.filter(Files::isRegularFile)
                    .filter(f -> ZIP_EXT.equalsIgnoreCase(FilenameUtils.getExtension(f.toString())))
                    .collect(Collectors.toList());
        }

        if (zipFiles != null && zipFiles.size() == 1) {
            return zipFiles.get(0);
        }

        throw new CopadoException("Could not find zip file to be deployed in deployment branch.");
    }
}
