package copado.onpremise.service.git;

import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class GitTestFactory {

    @Setter
    private Path baseDirGit;
    @Setter
    private String correctFileNameInMaster = "README.md";
    @Setter
    private String correctDeploymentBranchLocalName = "deployment/TEST";
    @Setter
    private String correctFileNameInDeploymentBranch = "new_file.md";
    @Setter
    private String correctHeadInDeploymentBranch = "54365d7e99a4f1465bf04e7afff4107510ff404d";

    public String correctFileNameInMaster() {
        return correctFileNameInMaster;
    }

    public Path correctFilePathInMaster() {
        return baseDirGit.resolve(correctFileNameInMaster());
    }

    public File correctFileInMaster() {
        return correctFilePathInMaster().toFile();
    }

    public String correctDeploymentBranchLocalName() {
        return correctDeploymentBranchLocalName;
    }

    public Path correctGitRefsHeadPathOfDeploymentBranch() {
        return Paths.get(baseDirGit.toAbsolutePath().toString(), ".git", "refs", "heads", correctDeploymentBranchLocalName);
    }

    public File correctGitRefsHeadFilehOfDeploymentBranch() {
        return correctGitRefsHeadPathOfDeploymentBranch().toFile();
    }

    public String correctFileNameInDeploymentBranch() {
        return correctFileNameInDeploymentBranch;
    }

    public Path correctFilePathInDeploymentBranch() {
        return baseDirGit.resolve(correctFileNameInDeploymentBranch());
    }

    public File correctFileInDeploymentBranch() {
        return correctFilePathInDeploymentBranch().toFile();
    }

    public String correctHeadInDeploymentBranch() {
        return correctHeadInDeploymentBranch;
    }

    public String correctDeploymentBranchRefsRemoteName() {
        return "refs/remotes/origin/" + correctDeploymentBranchLocalName();
    }

    public String currentFetchHeadFileContent() {
        try {
            return String.join("\n", Files.readAllLines(baseDirGit.resolve(".git").resolve("FETCH_HEAD"), Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException("Could not read FETCH_HEAD file in local git repository", e);
        }
    }

}
