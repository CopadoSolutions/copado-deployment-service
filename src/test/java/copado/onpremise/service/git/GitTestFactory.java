package copado.onpremise.service.git;

import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;


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
    @Setter
    private String correctMasterBranchLocalName = "master";
    @Setter
    private String correctAuthor = "TEST_AUTHOR";
    @Setter
    private String correctAuthorEmail = "TEST_AUTHOR@email.com";


    public String correctAuthor() {
        return correctAuthor;
    }

    public String correctAuthorEmail() {
        return correctAuthorEmail;
    }

    public String correctMasterBranchLocalName() {
        return correctMasterBranchLocalName;
    }

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

    public List<String> currentFetchHeadLinesInFile() {
        return currentLinesInFile(baseDirGit.resolve(".git").resolve("FETCH_HEAD"));
    }

    public List<String> currentHeadLinesInFile() {
        return currentLinesInFile(baseDirGit.resolve(".git").resolve("HEAD"));
    }

    public String currentHeadFirstLineInFile() {
        return currentHeadLinesInFile().get(0);
    }

    public String currentCommitEditMsgFistLineInFile() {
        return currentLinesInFile(baseDirGit.resolve(".git").resolve("COMMIT_EDITMSG")).get(0);
    }

    public String currentRefsHeadsMasterFirstLineInFile() {
        return currentLinesInFile(baseDirGit.resolve(".git").resolve("refs").resolve("heads").resolve(correctMasterBranchLocalName())).get(0);
    }

    private List<String> currentLinesInFile(Path path) {
        try {
            return Files.readAllLines(path, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Could not read FETCH_HEAD file in local git repository", e);
        }
    }

    public String currentFetchHeadFileContent() {
        return String.join("\n", currentFetchHeadLinesInFile());
    }

    public Predicate<String> isFetchHeadOfMaster() {
        return line -> line.contains("'" + correctMasterBranchLocalName() + "'");
    }

    public String correctHeadFirstLineAsMaster(){
        return "ref: refs/heads/" + correctMasterBranchLocalName();
    }
}
