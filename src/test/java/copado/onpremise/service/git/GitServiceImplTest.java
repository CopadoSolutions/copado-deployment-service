package copado.onpremise.service.git;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GitServiceImplTest {

    private final static String TEST_FOLDER = "gitService";
    private Path baseDirGit;

    @Before
    public void setUp() throws IOException {
        baseDirGit = Files.createTempDirectory("git").toAbsolutePath();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(baseDirGit.toFile());
    }


    @Test
    public void test_clone() throws GitServiceException {

        final String expectedFile = "README.md";
        final GitService gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());

        gitService.cloneRepo(baseDirGit, GitCredentialTestFactory.buildCorrectCredentials(TEST_FOLDER));

        assertTrue(baseDirGit.resolve(expectedFile).toFile().isFile());
        assertTrue(baseDirGit.resolve(expectedFile).toFile().exists());
    }

    @Test
    public void test_cloneBranchFromRepo() throws GitServiceException {

        final Path expectedRefsPath = Paths.get(baseDirGit.toAbsolutePath().toString(), ".git", "refs", "heads", "deployment", "TEST");
        final String givenDeploymentBranch = "deployment/TEST";
        final GitService gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());

        GitSession gitSession = gitService.cloneRepo(baseDirGit, GitCredentialTestFactory.buildCorrectCredentials(TEST_FOLDER));
        gitService.cloneBranchFromRepo(gitSession, givenDeploymentBranch);

        assertTrue(baseDirGit.resolve(expectedRefsPath).toFile().isFile());
        assertTrue(baseDirGit.resolve(expectedRefsPath).toFile().exists());
    }

    @Test
    public void test_checkout() throws GitServiceException {

        final String expectedFile = "new_file.md";
        final String givenDeploymentBranch = "deployment/TEST";
        final GitService gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());

        GitSession gitSession = gitService.cloneRepo(baseDirGit, GitCredentialTestFactory.buildCorrectCredentials(TEST_FOLDER));
        gitService.cloneBranchFromRepo(gitSession, givenDeploymentBranch);
        gitService.checkout(gitSession, givenDeploymentBranch);

        assertTrue(baseDirGit.resolve(expectedFile).toFile().isFile());
        assertTrue(baseDirGit.resolve(expectedFile).toFile().exists());
    }

    @Test
    public void test_getBranch() throws GitServiceException, IOException {

        final String expectedDeploymentBranchHead = "54365d7e99a4f1465bf04e7afff4107510ff404d";
        final String givenBranchName = "deployment/TEST";
        final String expectedRemoteBranchName = "refs/remotes/origin/" + givenBranchName;
        final GitService gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());

        GitSession gitSession = gitService.cloneRepo(baseDirGit, GitCredentialTestFactory.buildCorrectCredentials(TEST_FOLDER));
        Branch givenBranch = gitService.getBranch(gitSession, givenBranchName);

        final String currentHead = Files.readAllLines(baseDirGit.resolve(".git").resolve("FETCH_HEAD"), Charset.defaultCharset()).stream().collect(joining());

        assertThat(currentHead, containsString(expectedDeploymentBranchHead));
        assertThat(currentHead, containsString(givenBranchName));
        assertThat(givenBranch.getIdentifier(), is(equalTo(expectedDeploymentBranchHead)));
        assertThat(givenBranch.getName(), is(equalTo(expectedRemoteBranchName)));

    }

    @Test
    public void test_merge() throws GitServiceException, IOException {
        final String expectedHead = "ref: refs/heads/master";
        final String expectedFile = "new_file.md";
        final String givenBranchName = "deployment/TEST";
        final GitService gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());

        GitSession gitSession = gitService.cloneRepo(baseDirGit, GitCredentialTestFactory.buildCorrectCredentials(TEST_FOLDER));
        gitService.cloneBranchFromRepo(gitSession, givenBranchName);
        Branch deploymentBranch = gitService.getBranch(gitSession, givenBranchName);
        gitService.mergeWithNoFastForward(gitSession, deploymentBranch, "master");

        final String currentHead = Files.readAllLines(baseDirGit.resolve(".git").resolve("HEAD"), Charset.defaultCharset()).get(0);

        assertThat(expectedHead, is(equalTo(currentHead)));
        assertTrue(baseDirGit.resolve(expectedFile).toFile().isFile());
        assertTrue(baseDirGit.resolve(expectedFile).toFile().exists());
    }


    @Test
    public void test_commit_after_Merge() throws GitServiceException, IOException {
        final String givenBranchName = "deployment/TEST";
        final String expectedHead = "ref: refs/heads/master";
        final String expectedFile = "new_file.md";
        final String author = "TEST_AUTHOR";
        final String authorEmail = "TEST_AUTHOR@email.com";
        final String expectedMessage = "NEW MESSAGE ADDED IN TEST COMMIT";
        final GitService gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());

        GitSession gitSession = gitService.cloneRepo(baseDirGit, GitCredentialTestFactory.buildCorrectCredentials(TEST_FOLDER));
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