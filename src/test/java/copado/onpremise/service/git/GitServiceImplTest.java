package copado.onpremise.service.git;

import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Flogger
public class GitServiceImplTest {

    private final static String TEST_FOLDER = "gitService";
    private Path baseDirGit;
    private GitService gitService;
    private GitSession gitSession;
    private GitTestFactory gitTestContext = new GitTestFactory();

    @Before
    public void setUp() throws IOException, GitServiceException {
        baseDirGit = Files.createTempDirectory("git").toAbsolutePath();
        gitTestContext.setBaseDirGit(baseDirGit);
        gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());
        gitSession = gitService.cloneRepo(baseDirGit, GitCredentialTestFactory.buildCorrectCredentials(TEST_FOLDER));
    }

    @After
    public void tearDown() throws Exception {
        gitService.close(gitSession);
        gitService = null;
        FileUtils.deleteDirectory(baseDirGit.toFile());
    }


    @Test
    public void clone_repositoryWithCorrectFile() {
        assertTrue(gitTestContext.correctFileInMaster().isFile());
        assertTrue(gitTestContext.correctFileInMaster().exists());
    }

    @Test
    public void test_cloneBranchFromRepo() throws Exception {

        gitService.cloneBranchFromRepo(gitSession, gitTestContext.correctDeploymentBranchLocalName());

        assertTrue(gitTestContext.correctGitRefsHeadFilehOfDeploymentBranch().isFile());
        assertTrue(gitTestContext.correctGitRefsHeadFilehOfDeploymentBranch().exists());
    }

    @Test
    public void test_checkout() throws Exception {

        gitService.cloneBranchFromRepo(gitSession, gitTestContext.correctDeploymentBranchLocalName());
        gitService.checkout(gitSession, gitTestContext.correctDeploymentBranchLocalName());

        assertTrue(gitTestContext.correctFileInDeploymentBranch().isFile());
        assertTrue(gitTestContext.correctFileInDeploymentBranch().exists());
    }

    @Test
    public void test_getBranch() throws Exception {

        Branch givenBranch = gitService.getBranch(gitSession, gitTestContext.correctDeploymentBranchLocalName());

        assertThat(gitTestContext.currentFetchHeadFileContent(), containsString(gitTestContext.correctHeadInDeploymentBranch()));
        assertThat(gitTestContext.currentFetchHeadFileContent(), containsString(gitTestContext.correctDeploymentBranchLocalName()));
        assertThat(givenBranch.getIdentifier(), is(equalTo(gitTestContext.correctHeadInDeploymentBranch())));
        assertThat(givenBranch.getName(), is(equalTo(gitTestContext.correctDeploymentBranchRefsRemoteName())));

    }

    @Test
    public void test_merge() throws Exception {


        final String expectedHead = "ref: refs/heads/master";
        final String expectedFile = "new_file.md";
        final String givenBranchName = "deployment/TEST";

        gitService.cloneBranchFromRepo(gitSession, givenBranchName);
        Branch deploymentBranch = gitService.getBranch(gitSession, givenBranchName);
        gitService.mergeWithNoFastForward(gitSession, deploymentBranch, "master");

        final String currentHead = Files.readAllLines(baseDirGit.resolve(".git").resolve("HEAD"), Charset.defaultCharset()).get(0);

        assertThat(expectedHead, is(equalTo(currentHead)));
        assertTrue(baseDirGit.resolve(expectedFile).toFile().isFile());
        assertTrue(baseDirGit.resolve(expectedFile).toFile().exists());
    }


    @Test
    public void test_commit_after_Merge() throws Exception {
        final String givenBranchName = "deployment/TEST";
        final String expectedHead = "ref: refs/heads/master";
        final String expectedFile = "new_file.md";
        final String author = "TEST_AUTHOR";
        final String authorEmail = "TEST_AUTHOR@email.com";
        final String expectedMessage = "NEW MESSAGE ADDED IN TEST COMMIT";

        gitService.cloneBranchFromRepo(gitSession, givenBranchName);
        Branch deploymentBranch = gitService.getBranch(gitSession, givenBranchName);
        gitService.mergeWithNoFastForward(gitSession, deploymentBranch, "master");
        gitService.commit(gitSession, expectedMessage, author, authorEmail);

        final String currentHead = Files.readAllLines(baseDirGit.resolve(".git").resolve("HEAD"), Charset.defaultCharset()).get(0);
        final String commitEditMsg = Files.readAllLines(baseDirGit.resolve(".git").resolve("COMMIT_EDITMSG"), Charset.defaultCharset()).get(0);
        final String latestCommit = Files.readAllLines(baseDirGit.resolve(".git").resolve("refs").resolve("heads").resolve("master"), Charset.defaultCharset()).get(0);
        final String fetchHeadMaster = Files.readAllLines(baseDirGit.resolve(".git").resolve("FETCH_HEAD"), Charset.defaultCharset()).stream().filter(line -> line.contains("'master'")).collect(joining());

        assertThat(currentHead, is(equalTo(expectedHead)));
        assertThat(commitEditMsg, is(equalTo(expectedMessage)));
        assertTrue(baseDirGit.resolve(expectedFile).toFile().isFile());
        assertTrue(baseDirGit.resolve(expectedFile).toFile().exists());
        // You did not push yet, remote is behind local
        assertThat(fetchHeadMaster, not(startsWith(latestCommit)));
    }
}