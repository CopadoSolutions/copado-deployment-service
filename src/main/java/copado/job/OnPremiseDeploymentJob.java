package copado.job;

import copado.controller.DeployRequest;
import copado.exception.CopadoException;
import copado.service.validation.ValidationResult;
import copado.service.validation.ValidationService;
import copado.service.salesforce.CopadoService;
import copado.service.salesforce.SalesforceService;
import copado.util.GitClientUtils;
import copado.util.PathUtils;
import copado.validator.Validator;
import copado.validator.onpremisedeployment.Info;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Scope("prototype")
public class OnPremiseDeploymentJob {

    private static final String TEMP_GIT = "git";
    private static final String TEMP_DEPLOY = "deploy";
    private static final String GIT_DEPLOY_DIR_IN_BRANCH = "deployment";
    private static final String ZIP_EXT = "zip";

    @Autowired
    private Validator<Info> validator;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private SalesforceService salesforceService;

    @Autowired
    private CopadoService copadoService;

    @Async
    public void doJob(DeployRequest request) {
        log.info("Starting job");

        Path gitTMP = null;
        Path deployZipFileTMPDir = null;

        try {

            copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Starting on premise deployment job task");

            log.info("Creating git temporal dir.");
            gitTMP = Files.createTempDirectory(TEMP_GIT);
            log.info("Created temporal dir:'{}'", gitTMP);

            Optional<Git> gitOpt = GitClientUtils.cloneRepo(gitTMP);
            if (gitOpt.isPresent()) {

                // ············································
                // Retrieving GIT info
                // ············································
                log.info("Repository correctly cloned.");
                Git git = gitOpt.get();

                // Donwload all branch changes needed
                GitClientUtils.cloneBranchFromRepo(git, request.getPromoteBranch());
                GitClientUtils.cloneBranchFromRepo(git, request.getTargetBranch());
                GitClientUtils.cloneBranchFromRepo(git, request.getDeploymentBranch());

                // Copy deploy zip to temporal dir
                deployZipFileTMPDir = Files.createTempDirectory(TEMP_DEPLOY);
                Path deployZipFileTMP = deployZipFileTMPDir.resolve("deploy.zip");
                copyDeployZipToTemporalDir(git, request.getDeploymentBranch(), gitTMP, deployZipFileTMP);


                // ············································
                // Validate promote-branch with deploy zip
                // ············································
                GitClientUtils.checkout(git, request.getPromoteBranch());
                copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Validating deployment zip with promotion branch.");

                ValidationResult validationResult = validationService.validate(deployZipFileTMP,gitTMP);
                log.info("Finished validation. Success: {}, Code: {}, Message: {}", validationResult.isSuccess(), validationResult.getCode(),validationResult.getMessage());

                if (!validationResult.isSuccess()) {
                    throw new CopadoException("Invalid deployment zip. Code: " + validationResult.getCode() + ". Message: " + validationResult.getMessage());
                }

                // ············································
                // Merge to target branch, commit and push
                // ············································

                // Deploy with salesforce
                salesforceService.deployZip(deployZipFileTMP.toAbsolutePath().toString());

                copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Salesforce deployment step success");

                // Checkout target_branch
                GitClientUtils.checkout(git, request.getTargetBranch());

                // Commit changes on git
                Ref promoteBranchRef = GitClientUtils.getBranch(git, request.getPromoteBranch());
                GitClientUtils.mergeWithBranch(git, promoteBranchRef, request.getTargetBranch());
                GitClientUtils.push(git);

                copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "Success");

            }
        } catch (CopadoException e) {
            log.error("On premise deployment failed:", e.getMessage());
            copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "On premise deployment failed:" + e.getMessage());
        } catch (Exception e) {
            log.error("On premise deployment failed:", e);
            copadoService.updateDeploymentJobStatus(request.getDeploymentJobId(), "On premise deployment failed:" + e.getMessage());
        } finally {
            PathUtils.safeDelete(gitTMP);
            PathUtils.safeDelete(deployZipFileTMPDir);
        }
    }


    private static void copyDeployZipToTemporalDir(Git git, String deploymentBranch, Path deployBranchPath, Path deployZipDest) throws GitAPIException, IOException, CopadoException {
        GitClientUtils.checkout(git, deploymentBranch);
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
