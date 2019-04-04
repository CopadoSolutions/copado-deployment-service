package copado.onpremise.connector.git;


import copado.onpremise.service.credential.GitCredentials;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class is a wrapper for git testing.<br/>
 * - Syntactic method naming<br/>
 * - Use {@link #setUp(String)} method before use this class it in your tests<br/>
 * - Use {@link #tearDown()} once each test has finished<br/>
 */
public class GitTestFactory {

    private static GitDataSource dataSource;
    private static GitDataSet dataSet;

    public static void setUp(String currentTestFolder) {
        GitTestFactory.dataSource = new GitDataSource(currentTestFolder);
        GitTestFactory.dataSet = new GitDataSet();
    }

    public static void setUpGitWithNewCopyOfRemote(String currentTestFolder){
        setUp(currentTestFolder);
        Path newOriginalRepositoryPath = createTempDir("newRemoteGit");
        try {
            FileUtils.copyDirectory(currentRemoteDirectoryPath().toFile(), newOriginalRepositoryPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not copy remote git repository into a new directory", e);
        }
        GitTestFactory.dataSource().setCurrentRemoteDir(newOriginalRepositoryPath);
    }

    public static void tearDown() {
        GitTestFactory.dataSource = null;
        GitTestFactory.dataSet = null;
    }

    public static GitCredentials buildCorrectCredentials(String testFolder){
        final GitCredentials givenGitCredentials = mock(GitCredentials.class);
        when(givenGitCredentials.getPassword()).thenReturn("");
        when(givenGitCredentials.getUsername()).thenReturn("");
        when(givenGitCredentials.getUrl()).thenReturn(testFolder);
        return givenGitCredentials;
    }

    public static GitService initGitService(){
        return new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteImpl());
    }

    public static GitDataSource dataSource() {
        return dataSource;
    }

    public static Path currentBaseGitDir() {
        return dataSource.getCurrentBaseGitDir();
    }

    public static String currentRemoteDirectory() {
        return currentRemoteDirectoryPath().toString();
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
        return currentBaseGitDir().resolve(correctFileNameInMaster());
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
        return Paths.get(currentBaseGitDir().toAbsolutePath().toString(), ".git", "refs", "heads", dataSet.getCorrectDeploymentBranchLocalName());
    }

    public static File refsHeadOfDeploymentBranch() {
        return refsHeadPathOfDeploymentBranch().toFile();
    }

    public static String correctFileNameInDeploymentBranch() {
        return dataSet.getCorrectFileNameInDeploymentBranch();
    }

    public static Path correctFilePathInDeploymentBranch() {
        return currentBaseGitDir().resolve(correctFileNameInDeploymentBranch());
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
        return currentLinesInFile(currentBaseGitDir().resolve(".git").resolve("FETCH_HEAD"));
    }

    public static List<String> currentLinesInFileHead() {
        return currentLinesInFile(currentBaseGitDir().resolve(".git").resolve("HEAD"));
    }

    public static String currentFirstLineInFileHead() {
        return currentLinesInFileHead().get(0);
    }

    public static String currentFirstLineInFileCommitEditMsg() {
        return currentLinesInFile(currentBaseGitDir().resolve(".git").resolve("COMMIT_EDITMSG")).get(0);
    }

    public static String currentFirstLineInFileRefsHeadsMaster() {
        return currentFirstLineInFileRefsHeads(currentBaseGitDir().resolve(".git"), correctMasterBranchLocalName());
    }

    public static String currentFirstLineInFileRefsHeadsDeployment() {
        return currentFirstLineInFileRefsHeads(currentBaseGitDir().resolve(".git"), correctDeploymentBranchLocalName());
    }

    public static String currentFirstLineInRemoteFileRefsHeadsMaster() {
        return currentFirstLineInFileRefsHeads(currentRemoteDirectoryPath(), correctMasterBranchLocalName());
    }

    public static String currentFirstLineInRemoteFileRefsHeadsDeploymentTest() {
        return currentFirstLineInFileRefsHeads(currentRemoteDirectoryPath(), correctDeploymentBranchLocalName());
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
        return createTempDir("git");
    }

    public static Path copyCurrent() {
        return createTempDir("git");
    }

    public static Path createTempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix).toAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporal directory for: " + prefix, e);
        }
    }

    public static Path currentRemoteDirectoryPath() {

        Path originalRepository = dataSource.getCurrentRemoteDir();

        if (!originalRepository.toFile().exists() || !originalRepository.toFile().isDirectory()) {
            throw new RuntimeException("Repository path: " + originalRepository.toString() + ", does not exists or it is not a directory");
        }

        return originalRepository;
    }

    public static Path currentRemoteArtifactRepositoryPath(){
        return currentRemoteDirectoryPath().getParent().resolve("artifact_repository.git");
    }

    private static String currentFirstLineInFileRefsHeads(Path baseRepositoryPath, String branchName) {
        return currentLinesInFile(baseRepositoryPath.resolve("refs").resolve("heads").resolve(branchName)).get(0);
    }
}
