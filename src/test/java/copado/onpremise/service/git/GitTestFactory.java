package copado.onpremise.service.git;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;


public class GitTestFactory {


    private static GitDataSource dataSource;
    private static GitDataSet dataSet;

    public static void setUp() {

        GitTestFactory.dataSource = new GitDataSource();
        GitTestFactory.dataSet = new GitDataSet();
    }

    public static void tearDown() {
        GitTestFactory.dataSource = null;
        GitTestFactory.dataSet = null;
    }

    public static Path currentBaseGitDir() {
        return dataSource.getCurrentBaseGitDir();
    }

    public static String currentTestFolder() {
        return dataSource.getCurrentTestFolder();
    }

    public static String correctAuthor() {
        return dataSet.getCorrectAuthor();
    }

    public static String correctAuthorEmail() {
        return dataSet.getCorrectAuthorEmail();
    }

    public static String correctMasterBranchLocalName() {
        return dataSet.getCorrectMasterBranchLocalName();
    }

    public static String correctFileNameInMaster() {
        return dataSet.getCorrectFileNameInMaster();
    }

    public static Path correctFilePathInMaster() {
        return dataSource.getCurrentBaseGitDir().resolve(correctFileNameInMaster());
    }

    public static File correctFileInMaster() {
        return correctFilePathInMaster().toFile();
    }

    public static String correctDeploymentBranchLocalName() {
        return dataSet.getCorrectDeploymentBranchLocalName();
    }

    public static String invalidBranchLocalName() {
        return dataSet.getInvalidBranchLocalName();
    }

    public static Path refsHeadPathOfDeploymentBranch() {
        return Paths.get(dataSource.getCurrentBaseGitDir().toAbsolutePath().toString(), ".git", "refs", "heads", dataSet.getCorrectDeploymentBranchLocalName());
    }

    public static File refsHeadOfDeploymentBranch() {
        return refsHeadPathOfDeploymentBranch().toFile();
    }

    public static String correctFileNameInDeploymentBranch() {
        return dataSet.getCorrectFileNameInDeploymentBranch();
    }

    public static Path correctFilePathInDeploymentBranch() {
        return dataSource.getCurrentBaseGitDir().resolve(correctFileNameInDeploymentBranch());
    }

    public static File correctFileInDeploymentBranch() {
        return correctFilePathInDeploymentBranch().toFile();
    }

    public static String correctHeadInDeploymentBranch() {
        return dataSet.getCorrectHeadInDeploymentBranch();
    }

    public static String correctDeploymentBranchRefsRemoteName() {
        return "refs/remotes/origin/" + correctDeploymentBranchLocalName();
    }

    public static List<String> currentLinesInFileFetchHead() {
        return currentLinesInFile(dataSource.getCurrentBaseGitDir().resolve(".git").resolve("FETCH_HEAD"));
    }

    public static List<String> currentLinesInFileHead() {
        return currentLinesInFile(dataSource.getCurrentBaseGitDir().resolve(".git").resolve("HEAD"));
    }

    public static String currentFirstLineInFileHead() {
        return currentLinesInFileHead().get(0);
    }

    public static String currentFirstLineInFileCommitEditMsg() {
        return currentLinesInFile(dataSource.getCurrentBaseGitDir().resolve(".git").resolve("COMMIT_EDITMSG")).get(0);
    }

    public static String currentFirstLineInFileRefsHeadsMaster() {
        return currentLinesInFile(dataSource.getCurrentBaseGitDir().resolve(".git").resolve("refs").resolve("heads").resolve(correctMasterBranchLocalName())).get(0);
    }

    private static List<String> currentLinesInFile(Path path) {
        try {
            return Files.readAllLines(path, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Could not read FETCH_HEAD file in local git repository", e);
        }
    }

    public static String currentContentInFileFetchHead() {
        return String.join("\n", currentLinesInFileFetchHead());
    }

    public static Predicate<String> isFetchHeadOfMaster() {
        return line -> line.contains("'" + correctMasterBranchLocalName() + "'");
    }

    public static String correctMasterRefHead() {
        return "ref: refs/heads/" + correctMasterBranchLocalName();
    }

    public static String currentLinesInFileFetchHeadFilteredByMaster() {
        return currentLinesInFileFetchHead().stream()
                .filter(isFetchHeadOfMaster())
                .collect(joining());
    }

    public static Path createGitTempDir() {
        try {
            return Files.createTempDirectory("git").toAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporal directory for git", e);
        }
    }
}
