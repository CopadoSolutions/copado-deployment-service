package copado.onpremise.job;

import com.google.inject.Inject;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.service.credential.GitCredentialService;
import copado.onpremise.service.file.PathService;
import copado.onpremise.service.git.Branch;
import copado.onpremise.service.git.GitService;
import copado.onpremise.service.git.GitSession;
import copado.onpremise.service.salesforce.*;
import copado.onpremise.service.salesforce.dx.CopadoDxService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


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

    @NonNull
    private CopadoDxService copadoDxService;

    @NonNull
    private GitCredentialService gitCredentialService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

            GitSession git = gitService.cloneRepo(gitTMP, gitCredentialService.getCredentialsForMainRepository());
            DeployRequest request = downloadAllBranchesAndReadDeploymentRequest(git);
            deploymentJobId = request.getDeploymentJobId();
            Path deployZipFileTMP = copyDeployZipToTemporalDir(gitTMP, deployZipFileTMPDir, git);
            log.atInfo().log("Deploy zip file tmp: %s", deployZipFileTMP);
            validatePromoteBranch(request, gitTMP, git, deployZipFileTMP);

            DeploymentResult deploymentResult = deployZip(request, deployZipFileTMP);
            processDeploymentResult(git, request, deploymentResult);

        } catch (Exception e) {
            log.atSevere().withCause(e).log("On premise deployment failed");
            copadoService.updateDeploymentJobStatus(deploymentJobId, buildErrorStatus(e));
        } finally {
            pathService.safeDelete(gitTMP);
            pathService.safeDelete(deployZipFileTMPDir);
        }
    }

    private void processDeploymentResult(GitSession git, DeployRequest request, DeploymentResult deploymentResult) throws CopadoException {
        List<CopadoTip> tips = new ArrayList<>(deploymentResult.getTips());

        if (deploymentResult.isSuccess()) {

            if (!request.isCheckOnly()) {
                copadoService.updateDeploymentJobValidationId(request.getDeploymentJobId(), deploymentResult.getAsyncId());
                copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Salesforce deployment step success");
                tips.addAll(mergeAndPushDeployment(request, git));
            }

            copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Success");

        } else {
            copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Failed");
        }

        uploadTipsAttachmentToDeployment(request, tips);
    }

    private void uploadTipsAttachmentToDeployment(DeployRequest request, final List<CopadoTip> tips) throws CopadoException {
        copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Preparing error messages from result.");
        String tipsJson = buildTipListJson(tips);
        String deploymentId = copadoService.getDeploymentId(request.getDeploymentJobId());
        String tipsJsonAttachmentName = request.getDeploymentJobId() + ".json";
        copadoService.createTxtAttachment(deploymentId, tipsJsonAttachmentName, tipsJson);
    }

    private String buildTipListJson(final List<CopadoTip> tips) throws CopadoException {
        String tipsJson;
        try {
            tipsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tips);
        } catch (IOException e) {
            log.atSevere().withCause(e).log("Could not parse tips list");
            throw new CopadoException("Could not parse tips list");
        }
        return tipsJson;
    }

    private List<CopadoTip> mergeAndPushDeployment(DeployRequest request, GitSession git) throws CopadoException {

        checkIfTargetBranchChanged(request, git);
        commitBeforeMerge(request, git);
        mergeIfItIsNotDxSource(request, git);
        return mergeDxIfThereIsAnyArtifact(request);
    }

    private List<CopadoTip> mergeDxIfThereIsAnyArtifact(DeployRequest request) {
        List<String> artifactRepositoryErrors = new ArrayList<>();

        request.getArtifactRepositoryIds().forEach(artifactRepositoryId -> cloneAndMergeArtifact(artifactRepositoryErrors, artifactRepositoryId));

        if(!artifactRepositoryErrors.isEmpty()){

            return artifactRepositoryErrors
                    .stream()
                    .map(stringToErrorTip())
                    .collect(toList());

        }
        return Collections.emptyList();

    }

    private Function<String, CopadoTip> stringToErrorTip() {
        return errorMessage -> new CopadoTip(TipLevel.ERROR, errorMessage, "");
    }

    private void cloneAndMergeArtifact(List<String> artifactRepositoryErrors, String artifactRepositoryId) {
        Optional<Path> artifactRepositoryBasePathOpt = createArtifactRepositoryDir(artifactRepositoryErrors, artifactRepositoryId);
        if (artifactRepositoryBasePathOpt.isPresent()) {
            cloneAndMergeForArtifact(artifactRepositoryErrors, artifactRepositoryId, artifactRepositoryBasePathOpt);
        }
    }

    private void cloneAndMergeForArtifact(List<String> artifactRepositoryErrors, String artifactRepositoryId, Optional<Path> artifactRepositoryBasePathOpt) {
        Path artifactRepositoryBasePath = artifactRepositoryBasePathOpt.get();
        Optional<GitSession> gitSessionOpt = cloneArtifactRepository(artifactRepositoryErrors, artifactRepositoryId, artifactRepositoryBasePath);
        if (gitSessionOpt.isPresent()) {

            getBranchAndMergeForArtifact(artifactRepositoryErrors, artifactRepositoryId, gitSessionOpt);

        }
    }

    private void getBranchAndMergeForArtifact(List<String> artifactRepositoryErrors, String artifactRepositoryId, Optional<GitSession> gitSessionOpt) {
        GitSession gitSession = gitSessionOpt.get();
        Optional<Branch> deploymentBranchOpt = getBranchForArtifact(artifactRepositoryErrors, artifactRepositoryId, gitSession);
        if (deploymentBranchOpt.isPresent()) {
            Branch deploymentBranch = deploymentBranchOpt.get();
            mergeBranchForArtifact(artifactRepositoryErrors, artifactRepositoryId, gitSession, deploymentBranch);
        }
    }

    private Optional<Branch> getBranchForArtifact(List<String> artifactRepositoryErrors, String artifactRepositoryId, GitSession gitSession) {
        try {
            return Optional.of(gitService.getBranch(gitSession, deployBranchName));
        } catch (CopadoException e) {
            String errorMessage = String.format("Could not get branch '%s' for artifact repository '%s'", deployBranchName, artifactRepositoryId);
            log.atSevere().withCause(e).log(errorMessage);
            artifactRepositoryErrors.add(errorMessage);
            return Optional.empty();
        }
    }

    private void mergeBranchForArtifact(List<String> artifactRepositoryErrors, String artifactRepositoryId, GitSession gitSession, Branch deploymentBranch) {
        try {
            gitService.mergeWithNoFastForward(gitSession, deploymentBranch, "master");
        } catch (CopadoException e) {
            String errorMessage = String.format("Could not merge branch '%s' for artifact repository '%s'", deploymentBranch, artifactRepositoryId);
            log.atSevere().withCause(e).log(errorMessage);
            artifactRepositoryErrors.add(errorMessage);
        }
    }

    private Optional<GitSession> cloneArtifactRepository(List<String> artifactRepositoryErrors, String artifactRepositoryId, Path artifactRepositoryBasePath) {
        try {
            return Optional.of(gitService.cloneRepo(artifactRepositoryBasePath, gitCredentialService.getCredentials(artifactRepositoryId)));
        } catch (CopadoException e) {
            String errorMessage = String.format("Could not clone artifact git repository: %s", artifactRepositoryId);
            log.atSevere().withCause(e).log(errorMessage);
            artifactRepositoryErrors.add(errorMessage);
        }
        return Optional.empty();

    }

    private Optional<Path> createArtifactRepositoryDir(List<String> artifactRepositoryErrors, String artifactRepositoryId) {
        try {
            return Optional.of(createTemporalGitPath());
        } catch (IOException e) {
            String errorMessage = String.format("Could not create temporal directory for artifact git repository: %s", artifactRepositoryId);
            log.atSevere().withCause(e).log(errorMessage);
            artifactRepositoryErrors.add(errorMessage);
            return Optional.empty();
        }
    }

    private void mergeIfItIsNotDxSource(DeployRequest request, GitSession git) throws CopadoException {
        if (!copadoDxService.isDxSource(request.getPromoteBranch(), copadoService.getSourceOrgId(request.getDeploymentJobId()))) {
            if (!gitService.hasDifferences(git, request.getPromoteBranch(), request.getTargetBranch())) {
                Branch gitTargetBranch = gitService.getBranch(git, request.getTargetBranch());
                Branch gitPromotionBranch = gitService.getBranch(git, request.getPromoteBranch());

                gitService.mergeWithNoFastForward(git, gitTargetBranch, request.getPromoteBranch());
                gitService.push(git);

                gitService.mergeWithNoFastForward(git, gitPromotionBranch, request.getTargetBranch());
                gitService.push(git);

                gitService.mergeWithNoFastForward(git, gitTargetBranch, request.getPromoteBranch());
                gitService.push(git);

            }
        }
    }

    private void commitBeforeMerge(DeployRequest request, GitSession git) throws CopadoException {
        String commitMessage = String.format("Copado merge %s into %s", request.getPromoteBranch(), request.getTargetBranch());
        gitService.commit(git, commitMessage, request.getGitAuthor(), request.getGitAuthorEmail());
        gitService.push(git);
    }

    private void checkIfTargetBranchChanged(DeployRequest request, GitSession git) throws CopadoException {
        String currentHead = gitService.getHead(git);
        gitService.fetchBranch(git, request.getTargetBranch());
        String newFetchedHead = gitService.getHead(git);
        if (!newFetchedHead.equals(currentHead)) {
            String errorMessage = String.format(
                    "Deployment Job '%s': Changes detected in target branch '%s' during deployment execution, " +
                            "please try to restart the deployment or recreate the promotion branch out of the new target branch state. Promotion Branch = '%s'",
                    request.getDeploymentJobId(), request.getTargetBranch(), request.getPromoteBranch());
            log.atSevere().log(errorMessage);
            throw new CopadoException(errorMessage);
        }

    }

    private DeploymentResult deployZip(DeployRequest request, Path deployZipFileTMP) throws ConnectionException, CopadoException {
        MetadataConnection destinationOrgMetadata = metadataConnectionService.build(request.getOrgDestId());
        SalesforceDeployerDelegate deployerDelegate = buildDeployerDelegate(request);
        return salesforceService.deployZip(destinationOrgMetadata, deployZipFileTMP.toAbsolutePath().toString(), request, deployerDelegate);

    }

    private SalesforceDeployerDelegate buildDeployerDelegate(DeployRequest request) {
        return (String asyncId) -> {
            copadoService.updateDeploymentJobValidationId(request.getDeploymentJobId(), null);
            copadoService.updateDeploymentJobAsyncId(request.getDeploymentJobId(), asyncId);
        };
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
                    .collect(toList());
        }

        if (zipFiles != null && zipFiles.size() == 1) {
            return zipFiles.get(0);
        }

        throw new CopadoException("Could not find zip file to be deployed in deployment branch.");
    }
}
