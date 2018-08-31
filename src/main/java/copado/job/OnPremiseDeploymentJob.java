package copado.job;

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


    @Async
    public void doJob(String promoteBranch, String targetBranch, String deploymentBranch) {
        log.info("Starting job");

        Path gitTMP = null;
        Path deployZipFileTMP = null;

        try {
            gitTMP = Files.createTempDirectory(TEMP_GIT);
            log.info("Created temporal dir:'{}'", gitTMP);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            Optional<Git> gitOpt = GitClientUtils.cloneRepo(gitTMP);
            if (gitOpt.isPresent()) {

                log.info("Repository correctly cloned.");
                Git git = gitOpt.get();

                // Donwload all branch changes needed
                GitClientUtils.cloneBranchFromRepo(git,promoteBranch);
                GitClientUtils.cloneBranchFromRepo(git,targetBranch);
                GitClientUtils.cloneBranchFromRepo(git,deploymentBranch);

                // Copy deploy zip to temporal dir
                deployZipFileTMP = Files.createTempDirectory(TEMP_DEPLOY).resolve("deploy.zip");
                copyDeployZipToTemporalDir(git,deploymentBranch,gitTMP,deployZipFileTMP);

                //Validate promote-branch with deploy zip
                GitClientUtils.checkout(git,promoteBranch);
                boolean isValid = validator.validate(new Info(deployZipFileTMP,gitTMP));
                log.info("Is valid deployment:{}",isValid);

                // Merge to target branch, commit and push
                if(isValid) {
                    // Checkout target_branch
                    GitClientUtils.checkout(git, targetBranch);

                    // Retrieve promoteBranch branch
                    Ref promoteBranchRef = GitClientUtils.getBranch(git, promoteBranch);
                    GitClientUtils.mergeWithBranch(git, promoteBranchRef, targetBranch);

                    GitClientUtils.push(git);
                }

            }
        } catch (Exception e) {
            log.error("On premise deployment failed:",e);
        } finally {
            PathUtils.safeDelete(gitTMP);
            PathUtils.safeDelete(deployZipFileTMP);
        }
    }



    private static void copyDeployZipToTemporalDir(Git git, String deploymentBranch, Path deployBranchPath, Path deployZipDest) throws Exception {
        GitClientUtils.checkout(git,deploymentBranch);
        Path deployZip = findDeployZipPath(deployBranchPath);
        FileUtils.copyFile(new File(deployZip.toAbsolutePath().toString()),new File(deployZipDest.toAbsolutePath().toString()));
    }


    private static Path findDeployZipPath(Path deployBranchPath) throws Exception {
        List<Path> zipFiles = Files
                .walk(deployBranchPath.resolve(GIT_DEPLOY_DIR_IN_BRANCH))
                .filter(Files::isRegularFile)
                .filter(f -> ZIP_EXT.equalsIgnoreCase(FilenameUtils.getExtension(f.toString())))
                .collect(Collectors.toList());

        if ( zipFiles != null && zipFiles.size() == 1 ){
            return zipFiles.get(0);
        }

        throw new Exception("Could not find zip file to be deployed in deployment branch.");
    }
}
